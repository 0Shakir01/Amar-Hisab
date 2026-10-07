package com.example.data.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

object CurrencyFormatter {
    val DHAKA_ZONE: ZoneId = ZoneId.of("Asia/Dhaka")

    private val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    private val englishDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

    fun toBanglaDigits(numberStr: String): String {
        val sb = StringBuilder(numberStr.length)
        for (ch in numberStr) {
            if (ch in '0'..'9') {
                sb.append(banglaDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toEnglishDigits(numberStr: String): String {
        val sb = StringBuilder(numberStr.length)
        for (ch in numberStr) {
            val idx = banglaDigits.indexOf(ch)
            if (idx != -1) {
                sb.append(englishDigits[idx])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Formats integer paisa to BDT currency string.
     * E.g. 150000L -> "৳ 1,500.00" or "৳ ১,৫০০.০০"
     */
    fun formatBdt(
        paisa: Long,
        isBangla: Boolean = true,
        includeSign: Boolean = false,
        showDecimals: Boolean = true
    ): String {
        val isNegative = paisa < 0
        val absPaisa = abs(paisa)
        val taka = absPaisa / 100
        val remPaisa = absPaisa % 100

        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
        }
        val df = DecimalFormat("#,##0", symbols)
        val takaFormatted = df.format(taka)

        val amountStr = if (showDecimals && remPaisa > 0) {
            String.format(Locale.US, "%s.%02d", takaFormatted, remPaisa)
        } else if (showDecimals) {
            "$takaFormatted.00"
        } else {
            takaFormatted
        }

        val sign = when {
            isNegative -> "-"
            includeSign && paisa > 0 -> "+"
            else -> ""
        }

        val baseFormatted = if (isBangla) {
            "$sign৳ ${toBanglaDigits(amountStr)}"
        } else {
            "$sign৳ $amountStr"
        }

        return baseFormatted
    }

    fun formatBdt(
        paisa: Long,
        useBanglaDigits: Boolean = true,
        showSymbol: Boolean = true
    ): String {
        val isNegative = paisa < 0
        val absPaisa = abs(paisa)
        val taka = absPaisa / 100
        val remPaisa = absPaisa % 100

        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
        }
        val df = DecimalFormat("#,##0", symbols)
        val takaFormatted = df.format(taka)

        val amountStr = if (remPaisa > 0) {
            String.format(Locale.US, "%s.%02d", takaFormatted, remPaisa)
        } else {
            "$takaFormatted.00"
        }

        val sign = if (isNegative) "-" else ""
        val digits = if (useBanglaDigits) toBanglaDigits(amountStr) else amountStr
        return if (showSymbol) "$sign৳ $digits" else "$sign$digits"
    }

    /**
     * Parses an input string (e.g., "1500.50" or "১৫০০.৫০") to paisa integer.
     */
    fun parseBdtToPaisa(input: String): Long? {
        if (input.isBlank()) return null
        val normalized = toEnglishDigits(input.trim().replace(",", "").replace("৳", "").trim())
        return try {
            val parts = normalized.split(".")
            if (parts.size > 2) return null
            val taka = if (parts[0].isEmpty()) 0L else (parts[0].toLongOrNull() ?: return null)
            if (taka < 0) return null
            val paisa = if (parts.size > 1) {
                val pStr = parts[1].padEnd(2, '0').take(2)
                pStr.toLongOrNull() ?: 0L
            } else {
                0L
            }
            if (taka == 0L && paisa == 0L) null else taka * 100L + paisa
        } catch (e: Exception) {
            null
        }
    }

    fun currentDhakaMillis(): Long {
        return Instant.now().toEpochMilli()
    }

    fun currentDhakaMonthKey(): String {
        val localDate = LocalDate.now(DHAKA_ZONE)
        return localDate.format(DateTimeFormatter.ofPattern("yyyy-MM", Locale.US))
    }

    fun getMonthKey(dateMillis: Long): String {
        val date = Instant.ofEpochMilli(dateMillis).atZone(DHAKA_ZONE).toLocalDate()
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM", Locale.US))
    }

    fun toMonthKey(dateMillis: Long): String = getMonthKey(dateMillis)

    fun getMonthLabels(monthKey: String): Pair<String, String> {
        return try {
            val ym = YearMonth.parse(monthKey)
            val monthNum = ym.monthValue
            val year = ym.year
            val bnMonths = arrayOf(
                "", "জানু", "ফেব্রু", "মার্চ", "এপ্রিল", "মে", "জুন",
                "জুলাই", "আগস্ট", "সেপ্টে", "অক্টো", "নভে", "ডিসে"
            )
            val enMonths = arrayOf(
                "", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            )
            Pair(
                "${bnMonths[monthNum]} ${toBanglaDigits(year.toString())}",
                "${enMonths[monthNum]} $year"
            )
        } catch (e: Exception) {
            Pair(monthKey, monthKey)
        }
    }

    fun formatDate(millis: Long, isBangla: Boolean = true): String {
        val date = Instant.ofEpochMilli(millis).atZone(DHAKA_ZONE).toLocalDate()
        val formatted = date.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH))
        if (!isBangla) return formatted

        // Bengali month names
        val monthNamesBn = mapOf(
            "Jan" to "জানু",
            "Feb" to "ফেব্রু",
            "Mar" to "মার্চ",
            "Apr" to "এপ্রিল",
            "May" to "মে",
            "Jun" to "জুন",
            "Jul" to "জুলাই",
            "Aug" to "আগস্ট",
            "Sep" to "সেপ্টে",
            "Oct" to "অক্টো",
            "Nov" to "নভে",
            "Dec" to "ডিসে"
        )
        var result = formatted
        for ((en, bn) in monthNamesBn) {
            if (result.contains(en)) {
                result = result.replace(en, bn)
                break
            }
        }
        return toBanglaDigits(result)
    }

    fun formatMonthYear(monthKey: String, isBangla: Boolean = true): String {
        return try {
            val ym = YearMonth.parse(monthKey)
            val monthNum = ym.monthValue
            val year = ym.year

            val bnMonths = arrayOf(
                "", "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
                "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
            )
            val enMonths = arrayOf(
                "", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )

            if (isBangla) {
                "${bnMonths[monthNum]} ${toBanglaDigits(year.toString())}"
            } else {
                "${enMonths[monthNum]} $year"
            }
        } catch (e: Exception) {
            monthKey
        }
    }
}
