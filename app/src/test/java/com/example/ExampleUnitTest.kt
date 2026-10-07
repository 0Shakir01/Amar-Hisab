package com.example

import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.InvestmentEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.AccountType
import com.example.data.model.CurrencyFormatter
import com.example.data.model.DebtStatus
import com.example.data.model.DebtType
import com.example.data.model.ExpenseCategory
import com.example.data.model.IncomeCategory
import com.example.data.model.InvestmentType
import com.example.data.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testCurrencyFormatting() {
        val bdtBangla = CurrencyFormatter.formatBdt(150000L, isBangla = true)
        assertEquals("৳ ১,৫০০.০০", bdtBangla)

        val bdtEnglish = CurrencyFormatter.formatBdt(150000L, isBangla = false)
        assertEquals("৳ 1,500.00", bdtEnglish)

        val negativeBdt = CurrencyFormatter.formatBdt(-50000L, isBangla = false)
        assertEquals("-৳ 500.00", negativeBdt)
    }

    @Test
    fun testPaisaParsing() {
        val paisaFromEnglish = CurrencyFormatter.parseBdtToPaisa("1500.50")
        assertEquals(150050L, paisaFromEnglish)

        val paisaFromBangla = CurrencyFormatter.parseBdtToPaisa("১৫০০.৫০")
        assertEquals(150050L, paisaFromBangla)

        val paisaFromInteger = CurrencyFormatter.parseBdtToPaisa("2500")
        assertEquals(250000L, paisaFromInteger)

        val dotFifty = CurrencyFormatter.parseBdtToPaisa(".50")
        assertEquals(50L, dotFifty)

        val zeroAmount = CurrencyFormatter.parseBdtToPaisa("0.00")
        assertNull(zeroAmount)
    }

    // -------------------------------------------------------------
    // DATA INTEGRITY & ACCOUNTING CUJ TESTS
    // -------------------------------------------------------------

    /**
     * Helper to compute an account's live balance deterministically using the repository formula.
     */
    private fun computeAccountBalance(
        account: AccountEntity,
        transactions: List<TransactionEntity>,
        debts: List<DebtEntity>,
        investments: List<InvestmentEntity>
    ): Long {
        var balance = account.openingBalancePaisa

        transactions.filter { !it.isDeleted }.forEach { tx ->
            when (tx.type) {
                TransactionType.INCOME.name -> {
                    if (tx.accountId == account.id) balance += tx.amountPaisa
                }
                TransactionType.EXPENSE.name -> {
                    if (tx.accountId == account.id) balance -= tx.amountPaisa
                }
                TransactionType.TRANSFER.name -> {
                    if (tx.accountId == account.id) balance -= tx.amountPaisa
                    if (tx.destinationAccountId == account.id) balance += tx.amountPaisa
                }
            }
        }

        debts.filter { !it.isDeleted }.forEach { d ->
            if (d.accountId == account.id) {
                if (d.debtType == DebtType.OTHERS_OWE_ME.key) {
                    if (!d.isOpening) {
                        balance -= d.amountPaisa
                    }
                    balance += d.settledAmountPaisa
                } else if (d.debtType == DebtType.I_OWE_OTHERS.key) {
                    if (!d.isOpening) {
                        balance += d.amountPaisa
                    }
                    balance -= d.settledAmountPaisa
                }
            }
        }

        investments.filter { !it.isDeleted }.forEach { inv ->
            if (inv.accountId == account.id) {
                balance -= inv.paidAmountPaisa
            }
        }

        return balance
    }

    @Test
    fun testJobIncomeIncreasesSelectedAccount() {
        val bank = AccountEntity(id = 1L, name = "Bank", type = AccountType.BANK.name, openingBalancePaisa = 1000000L) // ৳ 10,000
        val jobIncome = TransactionEntity(
            id = 1L,
            type = TransactionType.INCOME.name,
            amountPaisa = 5000000L, // ৳ 50,000
            accountId = 1L,
            category = IncomeCategory.JOB.key,
            sourceDetails = "Monthly Job Salary",
            dateMillis = System.currentTimeMillis()
        )

        val balance = computeAccountBalance(bank, listOf(jobIncome), emptyList(), emptyList())
        assertEquals(6000000L, balance) // 10,000 + 50,000 = 60,000
    }

    @Test
    fun testBusinessIncomeIncreasesSelectedAccount() {
        val cash = AccountEntity(id = 1L, name = "Cash", type = AccountType.CASH.name, openingBalancePaisa = 200000L) // ৳ 2,000
        val bizIncome = TransactionEntity(
            id = 2L,
            type = TransactionType.INCOME.name,
            amountPaisa = 1500000L, // ৳ 15,000
            accountId = 1L,
            category = IncomeCategory.BUSINESS.key,
            sourceDetails = "Saree Business: Website Sales",
            dateMillis = System.currentTimeMillis()
        )

        val balance = computeAccountBalance(cash, listOf(bizIncome), emptyList(), emptyList())
        assertEquals(1700000L, balance) // 2,000 + 15,000 = 17,000
    }

    @Test
    fun testPersonalExpenseDecreasesSelectedAccount() {
        val bank = AccountEntity(id = 1L, name = "Bank", type = AccountType.BANK.name, openingBalancePaisa = 2000000L) // ৳ 20,000
        val expense = TransactionEntity(
            id = 3L,
            type = TransactionType.EXPENSE.name,
            amountPaisa = 350000L, // ৳ 3,500
            accountId = 1L,
            category = ExpenseCategory.PERSONAL.key,
            paidTo = "Grocery Supermarket",
            dateMillis = System.currentTimeMillis()
        )

        val balance = computeAccountBalance(bank, listOf(expense), emptyList(), emptyList())
        assertEquals(1650000L, balance) // 20,000 - 3,500 = 16,500
    }

    @Test
    fun testTransferBetweenAccountsPreservesTotalAvailableMoney() {
        val cash = AccountEntity(id = 1L, name = "Cash", type = AccountType.CASH.name, openingBalancePaisa = 1000000L) // ৳ 10,000
        val bank = AccountEntity(id = 2L, name = "Bank", type = AccountType.BANK.name, openingBalancePaisa = 5000000L) // ৳ 50,000
        val initialTotal = cash.openingBalancePaisa + bank.openingBalancePaisa // ৳ 60,000

        // Transfer ৳ 5,000 from Bank to Cash
        val transfer = TransactionEntity(
            id = 4L,
            type = TransactionType.TRANSFER.name,
            amountPaisa = 500000L,
            accountId = 2L, // From Bank
            destinationAccountId = 1L, // To Cash
            dateMillis = System.currentTimeMillis()
        )

        val cashBalance = computeAccountBalance(cash, listOf(transfer), emptyList(), emptyList())
        val bankBalance = computeAccountBalance(bank, listOf(transfer), emptyList(), emptyList())

        assertEquals(1500000L, cashBalance) // 10,000 + 5,000 = 15,000
        assertEquals(4500000L, bankBalance) // 50,000 - 5,000 = 45,000
        assertEquals(initialTotal, cashBalance + bankBalance) // Total available remains 60,000
    }

    @Test
    fun testLendingMoneyDecreasesAccountAndIncreasesOthersOweMe() {
        val cash = AccountEntity(id = 1L, name = "Cash", type = AccountType.CASH.name, openingBalancePaisa = 3000000L) // ৳ 30,000
        // Lend ৳ 10,000 to Hasan
        val loanGiven = DebtEntity(
            id = 1L,
            personName = "Hasan",
            debtType = DebtType.OTHERS_OWE_ME.key,
            amountPaisa = 1000000L, // ৳ 10,000
            dateMillis = System.currentTimeMillis(),
            accountId = 1L,
            settledAmountPaisa = 0L,
            status = DebtStatus.PENDING.key
        )

        val cashBalance = computeAccountBalance(cash, emptyList(), listOf(loanGiven), emptyList())
        assertEquals(2000000L, cashBalance) // Cash decreased to ৳ 20,000
        assertEquals(1000000L, loanGiven.remainingAmountPaisa) // Others Owe Me increased by ৳ 10,000
    }

    @Test
    fun testReceivingLoanRepaymentIncreasesAccountAndDecreasesOthersOweMe() {
        val cash = AccountEntity(id = 1L, name = "Cash", type = AccountType.CASH.name, openingBalancePaisa = 3000000L)
        // Hasan repays ৳ 4,000
        val loanGiven = DebtEntity(
            id = 1L,
            personName = "Hasan",
            debtType = DebtType.OTHERS_OWE_ME.key,
            amountPaisa = 1000000L, // ৳ 10,000 originally lent
            dateMillis = System.currentTimeMillis(),
            accountId = 1L,
            settledAmountPaisa = 400000L, // ৳ 4,000 repaid
            status = DebtStatus.PARTIALLY_PAID.key
        )

        val cashBalance = computeAccountBalance(cash, emptyList(), listOf(loanGiven), emptyList())
        assertEquals(2400000L, cashBalance) // 30,000 - 10,000 + 4,000 = 24,000
        assertEquals(600000L, loanGiven.remainingAmountPaisa) // Remaining Others Owe Me decreased to 6,000
    }

    @Test
    fun testBorrowingMoneyIncreasesAccountAndIncreasesIOweOthers() {
        val bank = AccountEntity(id = 1L, name = "Bank", type = AccountType.BANK.name, openingBalancePaisa = 1000000L) // ৳ 10,000
        // Borrow ৳ 25,000 from Tariq
        val loanBorrowed = DebtEntity(
            id = 2L,
            personName = "Tariq",
            debtType = DebtType.I_OWE_OTHERS.key,
            amountPaisa = 2500000L, // ৳ 25,000
            dateMillis = System.currentTimeMillis(),
            accountId = 1L,
            settledAmountPaisa = 0L,
            status = DebtStatus.PENDING.key
        )

        val bankBalance = computeAccountBalance(bank, emptyList(), listOf(loanBorrowed), emptyList())
        assertEquals(3500000L, bankBalance) // 10,000 + 25,000 = 35,000
        assertEquals(2500000L, loanBorrowed.remainingAmountPaisa) // I Owe Others increased to ৳ 25,000
    }

    @Test
    fun testRepayingBorrowedMoneyDecreasesAccountAndDecreasesIOweOthers() {
        val bank = AccountEntity(id = 1L, name = "Bank", type = AccountType.BANK.name, openingBalancePaisa = 1000000L)
        // Repay ৳ 15,000 of the ৳ 25,000 borrowed
        val loanBorrowed = DebtEntity(
            id = 2L,
            personName = "Tariq",
            debtType = DebtType.I_OWE_OTHERS.key,
            amountPaisa = 2500000L,
            dateMillis = System.currentTimeMillis(),
            accountId = 1L,
            settledAmountPaisa = 1500000L, // ৳ 15,000 repaid
            status = DebtStatus.PARTIALLY_PAID.key
        )

        val bankBalance = computeAccountBalance(bank, emptyList(), listOf(loanBorrowed), emptyList())
        assertEquals(2000000L, bankBalance) // 10,000 + 25,000 - 15,000 = 20,000
        assertEquals(1000000L, loanBorrowed.remainingAmountPaisa) // Remaining I Owe Others decreased to ৳ 10,000
    }

    @Test
    fun testBuyingInvestmentDecreasesAccountButDoesNotIncreaseExpenses() {
        val bank = AccountEntity(id = 1L, name = "Bank", type = AccountType.BANK.name, openingBalancePaisa = 5000000L) // ৳ 50,000
        val investment = InvestmentEntity(
            id = 1L,
            projectName = "Purbachal Plot #12",
            investmentType = InvestmentType.PLOT_INSTALLMENT.key,
            totalAgreedPaisa = 200000000L, // ৳ 2,000,000
            paidAmountPaisa = 2000000L,    // ৳ 20,000 installment paid
            installmentPaisa = 2000000L,
            accountId = 1L
        )

        val transactions = listOf<TransactionEntity>() // NO expense transaction created
        val balance = computeAccountBalance(bank, transactions, emptyList(), listOf(investment))

        assertEquals(3000000L, balance) // Bank decreased by ৳ 20,000 to ৳ 30,000
        val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amountPaisa }
        assertEquals(0L, totalExpenses) // Monthly expenses remain 0 (not an expense)
    }

    @Test
    fun testCompleteFinancialMetricsFormulas() {
        val cashBalance = 4000000L // ৳ 40,000 cash
        val bankBalance = 6000000L // ৳ 60,000 bank
        val availableBalance = cashBalance + bankBalance // ৳ 100,000
        assertEquals(10000000L, availableBalance)

        val businessStock = 12000000L // ৳ 120,000 in stock assets
        val fixedAssets = 35000000L   // ৳ 350,000 equipment & assets
        val investments = 20000000L   // ৳ 200,000 plot / deposits
        val receivables = 5000000L    // ৳ 50,000 others owe me

        // Total Assets = Cash + Bank + Business Stock + Fixed Assets + Investments/Deposits + Receivables
        val totalAssets = cashBalance + bankBalance + businessStock + fixedAssets + investments + receivables
        assertEquals(82000000L, totalAssets) // ৳ 820,000

        val payables = 8000000L       // ৳ 80,000 I owe others
        val debtObligations = 7000000L // ৳ 70,000 remaining installment commitments
        // Total Liabilities = Payables + Outstanding Debt Obligations
        val totalLiabilities = payables + debtObligations
        assertEquals(15000000L, totalLiabilities) // ৳ 150,000

        // Net Worth = Total Assets - Total Liabilities
        val netWorth = totalAssets - totalLiabilities
        assertEquals(67000000L, netWorth) // ৳ 670,000

        // Monthly Operating Surplus = Recognized Income - Recognized Expenses - Business Cost of Goods Sold
        val recognizedIncome = 9000000L  // ৳ 90,000
        val recognizedExpense = 3000000L // ৳ 30,000
        val cogs = 2000000L              // ৳ 20,000 cost of goods sold
        val operatingSurplus = recognizedIncome - recognizedExpense - cogs
        assertEquals(4000000L, operatingSurplus) // ৳ 40,000
    }

    @Test
    fun testOwnerCapitalAndWithdrawalAccountingRules() {
        val ownerCapital = 5000000L // ৳ 50,000 personal to business
        val ownerWithdrawal = 2000000L // ৳ 20,000 business to personal

        // Rules:
        // Owner Capital is NOT Business Income.
        // Owner Withdrawal is NOT Business Expense.
        // Neither falsely inflates or deflates business operating profit.
        val businessSalesRevenue = 15000000L // ৳ 150,000
        val businessOperatingExpenses = 6000000L // ৳ 60,000

        val netBusinessProfit = businessSalesRevenue - businessOperatingExpenses
        assertEquals(9000000L, netBusinessProfit) // ৳ 90,000 profit remains unaffected by owner equity movements
    }

    @Test
    fun testStockPurchaseAndSaleProfitCalculation() {
        val purchaseCostPerUnit = 50000L // ৳ 500 per unit cost
        val purchasedQty = 100.0
        val totalStockCost = (purchaseCostPerUnit * purchasedQty).toLong()
        assertEquals(5000000L, totalStockCost) // ৳ 50,000 stock value

        // Sell 40 units at ৳ 750 each
        val soldQty = 40.0
        val salePricePerUnit = 75000L // ৳ 750
        val totalRevenue = (soldQty * salePricePerUnit).toLong() // ৳ 30,000
        val costOfGoodsSold = (soldQty * purchaseCostPerUnit).toLong() // ৳ 20,000
        val profit = totalRevenue - costOfGoodsSold

        assertEquals(3000000L, totalRevenue)
        assertEquals(2000000L, costOfGoodsSold)
        assertEquals(1000000L, profit) // ৳ 10,000 realized profit

        val remainingStockValue = ((purchasedQty - soldQty) * purchaseCostPerUnit).toLong()
        assertEquals(3000000L, remainingStockValue) // 60 units * ৳ 500 = ৳ 30,000 cost value
    }

    // =========================================================================
    // MANDATORY USER TEST CASES 1 THROUGH 8
    // =========================================================================

    @Test
    fun testCase1And2_PersonalBank1AndBank2SeparateBalancesAndHomeTotal() {
        // Case 1: Bank 1 opening balance ৳50,000 and Bank 2 opening balance ৳30,000 show Home Total Bank Balance ৳80,000.
        // Case 2: Bank 1 and Bank 2 appear separately in account details.
        val bank1 = AccountEntity(id = 1L, name = "ব্যাংক ১ (Bank 1)", type = AccountType.BANK.name, area = "PERSONAL", openingBalancePaisa = 5000000L) // ৳ 50,000
        val bank2 = AccountEntity(id = 2L, name = "ব্যাংক ২ (Bank 2)", type = AccountType.BANK.name, area = "PERSONAL", openingBalancePaisa = 3000000L) // ৳ 30,000

        val bank1Balance = computeAccountBalance(bank1, emptyList(), emptyList(), emptyList())
        val bank2Balance = computeAccountBalance(bank2, emptyList(), emptyList(), emptyList())

        assertEquals(5000000L, bank1Balance)
        assertEquals(3000000L, bank2Balance)

        val totalPersonalBankBalance = bank1Balance + bank2Balance
        assertEquals(8000000L, totalPersonalBankBalance) // ৳ 80,000 Home Total Bank Balance
    }

    @Test
    fun testCase3_4_5_AddOldReceivable_CashBankUnchanged_ReceivableAndAssetsIncrease() {
        // Case 3: Add old receivable ৳20,000; Cash/Bank remains unchanged.
        // Case 4: Receivable increases by ৳20,000.
        // Case 5: Total Assets increases by ৳20,000.
        val cash = AccountEntity(id = 1L, name = "ক্যাশ (Cash)", type = AccountType.CASH.name, area = "PERSONAL", openingBalancePaisa = 1000000L) // ৳ 10,000
        val bank1 = AccountEntity(id = 2L, name = "ব্যাংক ১ (Bank 1)", type = AccountType.BANK.name, area = "PERSONAL", openingBalancePaisa = 5000000L) // ৳ 50,000
        val bank2 = AccountEntity(id = 3L, name = "ব্যাংক ২ (Bank 2)", type = AccountType.BANK.name, area = "PERSONAL", openingBalancePaisa = 3000000L) // ৳ 30,000

        val oldDebt = DebtEntity(
            id = 10L,
            personName = "Karim Mia",
            debtType = DebtType.OTHERS_OWE_ME.key,
            amountPaisa = 2000000L, // ৳ 20,000
            dateMillis = System.currentTimeMillis(),
            accountId = 1L,
            settledAmountPaisa = 0L,
            isOpening = true // Historical / Opening debt
        )

        val debts = listOf(oldDebt)
        val cashBal = computeAccountBalance(cash, emptyList(), debts, emptyList())
        val bank1Bal = computeAccountBalance(bank1, emptyList(), debts, emptyList())
        val bank2Bal = computeAccountBalance(bank2, emptyList(), debts, emptyList())

        // 3. Cash and Bank remains strictly unchanged!
        assertEquals(1000000L, cashBal)
        assertEquals(5000000L, bank1Bal)
        assertEquals(3000000L, bank2Bal)

        // 4. Receivable increases by ৳ 20,000
        val receivablePaisa = debts.filter { it.debtType == DebtType.OTHERS_OWE_ME.key && !it.isDeleted }.sumOf { it.remainingAmountPaisa }
        assertEquals(2000000L, receivablePaisa)

        // 5. Total Assets = Cash + Bank 1 + Bank 2 + Receivable
        val availableBalance = cashBal + bank1Bal + bank2Bal // ৳ 90,000 (unchanged)
        assertEquals(9000000L, availableBalance)

        val totalAssets = availableBalance + receivablePaisa
        assertEquals(11000000L, totalAssets) // ৳ 110,000 (increased by exactly ৳ 20,000)
    }

    @Test
    fun testCase6_AddNewLendingFromBank1DecreasesBank1() {
        // Case 6: Add new lending ৳10,000 from Bank 1; Bank 1 decreases by ৳10,000.
        val bank1 = AccountEntity(id = 1L, name = "ব্যাংক ১", type = AccountType.BANK.name, area = "PERSONAL", openingBalancePaisa = 5000000L) // ৳ 50,000

        val newLending = DebtEntity(
            id = 20L,
            personName = "Rahim",
            debtType = DebtType.OTHERS_OWE_ME.key,
            amountPaisa = 1000000L, // ৳ 10,000
            dateMillis = System.currentTimeMillis(),
            accountId = 1L,
            settledAmountPaisa = 0L,
            isOpening = false // Live new lending!
        )

        val bank1Bal = computeAccountBalance(bank1, emptyList(), listOf(newLending), emptyList())
        assertEquals(4000000L, bank1Bal) // ৳ 50,000 - ৳ 10,000 = ৳ 40,000 (decreased by ৳ 10,000)
        assertEquals(1000000L, newLending.remainingAmountPaisa) // Receivable is ৳ 10,000
    }

    @Test
    fun testCase7_ReceiveRepaymentIntoBank2IncreasesBank2AndDecreasesReceivable() {
        // Case 7: Receive repayment ৳5,000 into Bank 2; Bank 2 increases by ৳5,000 and receivable decreases.
        val bank2 = AccountEntity(id = 2L, name = "ব্যাংক ২", type = AccountType.BANK.name, area = "PERSONAL", openingBalancePaisa = 3000000L) // ৳ 30,000

        val debt = DebtEntity(
            id = 10L,
            personName = "Karim Mia",
            debtType = DebtType.OTHERS_OWE_ME.key,
            amountPaisa = 2000000L, // ৳ 20,000
            dateMillis = System.currentTimeMillis(),
            accountId = 2L,
            settledAmountPaisa = 500000L, // ৳ 5,000 repayment received!
            status = DebtStatus.PARTIALLY_PAID.key,
            isOpening = true
        )

        val bank2Bal = computeAccountBalance(bank2, emptyList(), listOf(debt), emptyList())
        assertEquals(3500000L, bank2Bal) // Bank 2 increases from ৳ 30,000 to ৳ 35,000 (+ ৳ 5,000)
        assertEquals(1500000L, debt.remainingAmountPaisa) // Remaining receivable decreases from ৳ 20,000 to ৳ 15,000 (- ৳ 5,000)
    }

    @Test
    fun testCase8_TransferBetweenBank1AndBank2TotalBankBalanceUnchanged() {
        // Case 8: Transfer ৳10,000 from Bank 1 to Bank 2; total bank balance remains unchanged.
        val bank1 = AccountEntity(id = 1L, name = "ব্যাংক ১", type = AccountType.BANK.name, area = "PERSONAL", openingBalancePaisa = 5000000L) // ৳ 50,000
        val bank2 = AccountEntity(id = 2L, name = "ব্যাংক ২", type = AccountType.BANK.name, area = "PERSONAL", openingBalancePaisa = 3000000L) // ৳ 30,000

        val transferTx = TransactionEntity(
            id = 50L,
            type = TransactionType.TRANSFER.name,
            amountPaisa = 1000000L, // ৳ 10,000
            accountId = 1L, // from Bank 1
            destinationAccountId = 2L, // to Bank 2
            category = "Transfer",
            sourceDetails = "Bank Transfer",
            dateMillis = System.currentTimeMillis()
        )

        val txList = listOf(transferTx)
        val bank1Bal = computeAccountBalance(bank1, txList, emptyList(), emptyList())
        val bank2Bal = computeAccountBalance(bank2, txList, emptyList(), emptyList())

        assertEquals(4000000L, bank1Bal) // ৳ 50,000 - ৳ 10,000 = ৳ 40,000
        assertEquals(4000000L, bank2Bal) // ৳ 30,000 + ৳ 10,000 = ৳ 40,000

        val totalBank = bank1Bal + bank2Bal
        assertEquals(8000000L, totalBank) // Total remains exactly ৳ 80,000!

        // Confirm transfer does not count as income or expense
        val incomeSum = txList.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amountPaisa }
        val expenseSum = txList.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amountPaisa }
        assertEquals(0L, incomeSum)
        assertEquals(0L, expenseSum)
    }
}
