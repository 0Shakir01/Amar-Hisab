package com.example.data.assistant

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.model.AccountType
import com.example.data.model.CurrencyFormatter
import com.example.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class ParsedTransactionResult(
    val type: TransactionType,
    val amountPaisa: Long,
    val categoryId: Long?,
    val categoryName: String,
    val accountId: Long,
    val accountName: String,
    val dateMillis: Long,
    val note: String,
    val tags: String,
    val isUncertain: Boolean,
    val uncertainFields: List<String>,
    val parsedBy: String // "Gemini AI" or "Local Smart Engine"
)

class TransactionAssistantService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {

    suspend fun parseNaturalLanguage(
        input: String,
        categories: List<CategoryEntity>,
        accounts: List<AccountEntity>
    ): ParsedTransactionResult = withContext(Dispatchers.IO) {
        val trimmed = input.trim()
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        // If Gemini API Key is provided and not the starter placeholder, query Gemini
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val geminiResult = callGeminiParser(trimmed, categories, accounts, apiKey)
                if (geminiResult != null) {
                    return@withContext geminiResult
                }
            } catch (e: Exception) {
                Log.w("TransactionAssistant", "Gemini parsing failed, falling back to local engine: ${e.message}")
            }
        }

        // Local Smart Bangla/Banglish Parser
        parseLocally(trimmed, categories, accounts)
    }

    private fun callGeminiParser(
        text: String,
        categories: List<CategoryEntity>,
        accounts: List<AccountEntity>,
        apiKey: String
    ): ParsedTransactionResult? {
        val catNames = categories.map { "${it.nameEn} (${it.nameBn})" }.joinToString(", ")
        val accNames = accounts.map { "${it.name} [ID:${it.id}]" }.joinToString(", ")

        val systemInstruction = """
You are a financial transaction extraction assistant for the Amar Hisab Android app.
Analyze the user's natural language input in Bangla, Banglish, or English.
Extract:
- type: "EXPENSE" or "INCOME" or "TRANSFER"
- amount: integer or decimal number in BDT
- category: closest matching category from: $catNames
- account: closest matching account from: $accNames
- dateOffsetDays: 0 for today/আজ, -1 for yesterday/গতকাল, -2 for গত পরশু, etc.
- note: short description/note of the transaction in Bangla or English
- isUncertain: boolean (true if amount or category is ambiguous)
- uncertainFieldNotes: array of strings explaining what might need user review

Return strictly a valid JSON object with keys: type, amount, category, accountId, dateOffsetDays, note, isUncertain, uncertainFieldNotes.
Do not wrap in markdown quotes if possible, output pure JSON.
        """.trimIndent()

        val promptBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", "$systemInstruction\n\nInput: \"$text\""))
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.1)
            })
        }

        val requestUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
        val request = Request.Builder()
            .url(requestUrl)
            .post(promptBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val responseString = response.body?.string() ?: return null
            val root = JSONObject(responseString)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val content = candidates.getJSONObject(0).optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null
            val jsonText = parts.getJSONObject(0).optString("text")

            val parsedJson = JSONObject(jsonText)
            val typeStr = parsedJson.optString("type", "EXPENSE").uppercase()
            val type = try { TransactionType.valueOf(typeStr) } catch (e: Exception) { TransactionType.EXPENSE }
            val amountDouble = parsedJson.optDouble("amount", 0.0)
            val amountPaisa = (amountDouble * 100).toLong()

            val catName = parsedJson.optString("category", "")
            val matchedCat = categories.find {
                it.nameEn.equals(catName, ignoreCase = true) ||
                        it.nameBn.equals(catName, ignoreCase = true) ||
                        catName.contains(it.nameEn, ignoreCase = true) ||
                        catName.contains(it.nameBn, ignoreCase = true)
            } ?: categories.find { it.type == type.name }

            val accId = parsedJson.optLong("accountId", 0L)
            val matchedAcc = accounts.find { it.id == accId }
                ?: accounts.firstOrNull { it.isActive }
                ?: accounts.firstOrNull()
                ?: AccountEntity(name = "নগদ", type = "CASH")

            val offsetDays = parsedJson.optInt("dateOffsetDays", 0)
            val dateMillis = LocalDate.now(CurrencyFormatter.DHAKA_ZONE)
                .plusDays(offsetDays.toLong())
                .atStartOfDay(CurrencyFormatter.DHAKA_ZONE)
                .toInstant()
                .toEpochMilli()

            val note = parsedJson.optString("note", text)
            val isUncertain = parsedJson.optBoolean("isUncertain", amountPaisa <= 0)
            val uncertainList = mutableListOf<String>()
            val uncertainNotes = parsedJson.optJSONArray("uncertainFieldNotes")
            if (uncertainNotes != null) {
                for (i in 0 until uncertainNotes.length()) {
                    uncertainList.add(uncertainNotes.getString(i))
                }
            }
            if (amountPaisa <= 0) {
                uncertainList.add("টাকার পরিমাণ নিশ্চিত নয় (Amount not detected)")
            }

            return ParsedTransactionResult(
                type = type,
                amountPaisa = amountPaisa.coerceAtLeast(0L),
                categoryId = matchedCat?.id,
                categoryName = matchedCat?.nameBn ?: matchedCat?.nameEn ?: "অন্যান্য",
                accountId = matchedAcc.id,
                accountName = matchedAcc.name,
                dateMillis = dateMillis,
                note = note,
                tags = "AI Assistant",
                isUncertain = isUncertain,
                uncertainFields = uncertainList,
                parsedBy = "Gemini AI"
            )
        }
    }

    /**
     * Highly capable local pattern extractor for Bangla and Banglish transactions.
     * Examples:
     * - “আজ দুপুরে ২৫০ টাকা খাবারে খরচ”
     * - “bKash থেকে ৫০০ টাকা আয়”
     * - “গতকাল বাস ভাড়া ৫০ টাকা”
     * - “salary পেলাম ৩০,০০০ টাকা”
     */
    fun parseLocally(
        input: String,
        categories: List<CategoryEntity>,
        accounts: List<AccountEntity>
    ): ParsedTransactionResult {
        val lower = input.lowercase()
        val uncertainFields = mutableListOf<String>()

        // 1. Transaction Type Detection
        val isIncome = lower.contains("আয়") || lower.contains("আয়") || lower.contains("পেলাম") ||
                lower.contains("জমা") || lower.contains("বেতন") || lower.contains("salary") ||
                lower.contains("income") || lower.contains("received") || lower.contains("credited")

        val isTransfer = lower.contains("ট্রান্সফার") || lower.contains("স্থানান্তর") ||
                lower.contains("transfer") || lower.contains("পাঠালাম") || lower.contains("থেকে পাঠালাম")

        val type = when {
            isTransfer -> TransactionType.TRANSFER
            isIncome -> TransactionType.INCOME
            else -> TransactionType.EXPENSE
        }

        // 2. Amount Extraction
        // Match English or Bangla numbers followed or preceded by টাকা, tk, taka, ৳
        val banglaToEnglishMap = mapOf(
            '০' to '0', '১' to '1', '২' to '2', '৩' to '3', '৪' to '4',
            '৫' to '5', '৬' to '6', '৭' to '7', '৮' to '8', '৯' to '9'
        )
        val normalized = StringBuilder()
        for (ch in input) {
            normalized.append(banglaToEnglishMap[ch] ?: ch)
        }
        val normStr = normalized.toString().replace(",", "")

        // Regex looking for numbers: e.g. "250", "30000", "50.50"
        val numberPattern = Pattern.compile("(\\d+(\\.\\d+)?)")
        val matcher = numberPattern.matcher(normStr)
        var extractedAmountPaisa = 0L

        if (matcher.find()) {
            val numStr = matcher.group(1) ?: "0"
            extractedAmountPaisa = CurrencyFormatter.parseBdtToPaisa(numStr) ?: 0L
        }

        if (extractedAmountPaisa <= 0L) {
            uncertainFields.add("টাকার পরিমাণ পাওয়া যায়নি (Amount missing)")
        }

        // 3. Date Detection
        val now = LocalDate.now(CurrencyFormatter.DHAKA_ZONE)
        val targetDate = when {
            lower.contains("গতকাল") || lower.contains("yesterday") -> now.minusDays(1)
            lower.contains("গত পরশু") || lower.contains("day before yesterday") -> now.minusDays(2)
            else -> now
        }
        val dateMillis = targetDate.atStartOfDay(CurrencyFormatter.DHAKA_ZONE).toInstant().toEpochMilli()

        // 4. Account Detection
        val matchedAccount = when {
            lower.contains("bkash") || lower.contains("বিকাশ") -> accounts.find { it.type == AccountType.BKASH.name }
            lower.contains("nagad") || lower.contains("নগদ অ্যাপ") || lower.contains("নগদে") -> accounts.find { it.type == AccountType.NAGAD.name }
            lower.contains("bank") || lower.contains("ব্যাংক") || lower.contains("কার্ড") || lower.contains("card") -> accounts.find { it.type == AccountType.BANK.name }
            lower.contains("cash") || lower.contains("নগদ") || lower.contains("ক্যাশ") -> accounts.find { it.type == AccountType.CASH.name }
            else -> accounts.firstOrNull { it.isActive } ?: accounts.firstOrNull()
        } ?: accounts.firstOrNull() ?: AccountEntity(name = "নগদ", type = "CASH")

        // 5. Category Detection
        val targetCategories = categories.filter { it.type == type.name }
        var matchedCategory: CategoryEntity? = null

        when {
            // Food
            lower.contains("খাবার") || lower.contains("খাবার") || lower.contains("লাঞ্চ") || lower.contains("lunch") ||
                    lower.contains("dinner") || lower.contains("ডিনার") || lower.contains("নাস্তা") || lower.contains("food") ||
                    lower.contains("বাজার") || lower.contains("রেস্তোরাঁ") || lower.contains("বিরিয়ানি") || lower.contains("চা") -> {
                matchedCategory = targetCategories.find { it.nameEn.equals("Food", ignoreCase = true) || it.nameBn.contains("খাবার") }
            }
            // Transport
            lower.contains("বাস") || lower.contains("ভাড়া") || lower.contains("সিএনজি") || lower.contains("মেট্রোরেল") ||
                    lower.contains("রিকশা") || lower.contains("উবার") || lower.contains("pathao") || lower.contains("transport") ||
                    lower.contains("বাস ভাড়া") || lower.contains("তেল") || lower.contains("অকটেন") -> {
                matchedCategory = targetCategories.find { it.nameEn.equals("Transport", ignoreCase = true) || it.nameBn.contains("যাতায়াত") }
            }
            // Rent
            lower.contains("বাড়ি ভাড়া") || lower.contains("বাসা ভাড়া") || lower.contains("rent") || lower.contains("ফ্ল্যাট") -> {
                matchedCategory = targetCategories.find { it.nameEn.equals("Rent", ignoreCase = true) || it.nameBn.contains("ভাড়া") }
            }
            // Bills
            lower.contains("বিল") || lower.contains("বিদ্যুৎ") || lower.contains("ওয়াইফাই") || lower.contains("ডেসকো") ||
                    lower.contains("পানি") || lower.contains("গ্যাস") || lower.contains("bills") -> {
                matchedCategory = targetCategories.find { it.nameEn.equals("Bills", ignoreCase = true) || it.nameBn.contains("বিল") }
            }
            // Health
            lower.contains("ওষুধ") || lower.contains("ডাক্তার") || lower.contains("হাসপাতাল") || lower.contains("ফার্মেসি") ||
                    lower.contains("টেস্ট") || lower.contains("health") || lower.contains("medical") -> {
                matchedCategory = targetCategories.find { it.nameEn.equals("Health", ignoreCase = true) || it.nameBn.contains("চিকিৎসা") }
            }
            // Shopping
            lower.contains("পোশাক") || lower.contains("শার্ট") || lower.contains("জুতো") || lower.contains("কেনাকাটা") ||
                    lower.contains("shopping") || lower.contains("মার্কেট") || lower.contains("দারাজ") -> {
                matchedCategory = targetCategories.find { it.nameEn.equals("Shopping", ignoreCase = true) || it.nameBn.contains("কেনাকাটা") }
            }
            // Salary
            lower.contains("salary") || lower.contains("বেতন") || lower.contains("মাসিক বেতন") -> {
                matchedCategory = targetCategories.find { it.nameEn.equals("Salary", ignoreCase = true) || it.nameBn.contains("বেতন") }
            }
            // Freelance
            lower.contains("freelance") || lower.contains("ফ্রিল্যান্স") || lower.contains("প্রজেক্ট") || lower.contains("client") -> {
                matchedCategory = targetCategories.find { it.nameEn.equals("Freelance", ignoreCase = true) || it.nameBn.contains("ফ্রিল্যান্সিং") }
            }
            // Business
            lower.contains("ব্যবসা") || lower.contains("লাভ") || lower.contains("কাস্টমার") || lower.contains("বিক্রি") -> {
                matchedCategory = targetCategories.find { it.nameEn.equals("Business", ignoreCase = true) || it.nameBn.contains("ব্যবসা") }
            }
            else -> {
                matchedCategory = targetCategories.find { it.nameEn.equals("Other", ignoreCase = true) } ?: targetCategories.firstOrNull()
                uncertainFields.add("ক্যাটাগরি নিশ্চিত নয় (Category unconfirmed)")
            }
        }

        val isUncertain = uncertainFields.isNotEmpty()

        return ParsedTransactionResult(
            type = type,
            amountPaisa = extractedAmountPaisa,
            categoryId = matchedCategory?.id,
            categoryName = matchedCategory?.nameBn ?: matchedCategory?.nameEn ?: "অন্যান্য",
            accountId = matchedAccount.id,
            accountName = matchedAccount.name,
            dateMillis = dateMillis,
            note = input,
            tags = "সহকারী (Assistant)",
            isUncertain = isUncertain,
            uncertainFields = uncertainFields,
            parsedBy = "Local Smart Engine"
        )
    }
}
