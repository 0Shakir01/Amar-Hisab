package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AccountDao
import com.example.data.local.dao.AssetDao
import com.example.data.local.dao.BudgetDao
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.DebtDao
import com.example.data.local.dao.InstallmentDao
import com.example.data.local.dao.InvestmentDao
import com.example.data.local.dao.MoneyLentDao
import com.example.data.local.dao.StockDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.dao.UserProfileDao
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.AssetEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.InstallmentEntity
import com.example.data.local.entity.InvestmentEntity
import com.example.data.local.entity.MoneyLentEntity
import com.example.data.local.entity.StockItemEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserProfileEntity

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        UserProfileEntity::class,
        MoneyLentEntity::class,
        DebtEntity::class,
        InvestmentEntity::class,
        AssetEntity::class,
        StockItemEntity::class,
        InstallmentEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AmarHisabDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun moneyLentDao(): MoneyLentDao
    abstract fun debtDao(): DebtDao
    abstract fun investmentDao(): InvestmentDao
    abstract fun assetDao(): AssetDao
    abstract fun stockDao(): StockDao
    abstract fun installmentDao(): InstallmentDao

    companion object {
        @Volatile
        private var INSTANCE: AmarHisabDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN section TEXT NOT NULL DEFAULT 'PERSONAL'")
                db.execSQL("ALTER TABLE transactions ADD COLUMN subCategory TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE transactions ADD COLUMN royalTargetAchieved INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE transactions ADD COLUMN royalClientName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE transactions ADD COLUMN royalProjectName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE transactions ADD COLUMN royalPaymentStatus TEXT NOT NULL DEFAULT 'RECEIVED'")
                db.execSQL("ALTER TABLE transactions ADD COLUMN royalExpectedDateMillis INTEGER DEFAULT NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_section ON transactions(section)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS money_lent (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL DEFAULT 'local_user',
                        borrowerName TEXT NOT NULL,
                        amountGivenPaisa INTEGER NOT NULL,
                        dateGivenMillis INTEGER NOT NULL,
                        dueDateMillis INTEGER DEFAULT NULL,
                        amountReturnedPaisa INTEGER NOT NULL DEFAULT 0,
                        status TEXT NOT NULL DEFAULT 'PENDING',
                        accountId INTEGER NOT NULL,
                        note TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_money_lent_userId ON money_lent(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_money_lent_status ON money_lent(status)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_money_lent_dateGivenMillis ON money_lent(dateGivenMillis)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Accounts updates
                db.execSQL("ALTER TABLE accounts ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE accounts ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE accounts ADD COLUMN deletedAtMillis INTEGER DEFAULT NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_accounts_isDeleted ON accounts(isDeleted)")

                // Transactions updates
                db.execSQL("ALTER TABLE transactions ADD COLUMN category TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE transactions ADD COLUMN sourceDetails TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE transactions ADD COLUMN paidTo TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE transactions ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE transactions ADD COLUMN deletedAtMillis INTEGER DEFAULT NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_type ON transactions(type)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_category ON transactions(category)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_isDeleted ON transactions(isDeleted)")

                // Debts table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS debts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL DEFAULT 'local_user',
                        personName TEXT NOT NULL,
                        debtType TEXT NOT NULL,
                        amountPaisa INTEGER NOT NULL,
                        dateMillis INTEGER NOT NULL,
                        dueDateMillis INTEGER DEFAULT NULL,
                        accountId INTEGER NOT NULL,
                        settledAmountPaisa INTEGER NOT NULL DEFAULT 0,
                        status TEXT NOT NULL DEFAULT 'PENDING',
                        note TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0,
                        isDeleted INTEGER NOT NULL DEFAULT 0,
                        deletedAtMillis INTEGER DEFAULT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_debts_userId ON debts(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_debts_debtType ON debts(debtType)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_debts_status ON debts(status)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_debts_dateMillis ON debts(dateMillis)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_debts_isDeleted ON debts(isDeleted)")

                // Investments table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS investments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL DEFAULT 'local_user',
                        projectName TEXT NOT NULL,
                        investmentType TEXT NOT NULL,
                        totalAgreedPaisa INTEGER NOT NULL DEFAULT 0,
                        paidAmountPaisa INTEGER NOT NULL DEFAULT 0,
                        installmentPaisa INTEGER NOT NULL DEFAULT 0,
                        nextPaymentDateMillis INTEGER DEFAULT NULL,
                        accountId INTEGER NOT NULL,
                        note TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0,
                        isDeleted INTEGER NOT NULL DEFAULT 0,
                        deletedAtMillis INTEGER DEFAULT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_investments_userId ON investments(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_investments_investmentType ON investments(investmentType)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_investments_isDeleted ON investments(isDeleted)")

                // Migrate existing money_lent records into debts table safely
                try {
                    db.execSQL("""
                        INSERT INTO debts (
                            id, userId, personName, debtType, amountPaisa, dateMillis, dueDateMillis,
                            accountId, settledAmountPaisa, status, note, createdAt, updatedAt, isDeleted, deletedAtMillis
                        )
                        SELECT 
                            id, userId, borrowerName, 'OTHERS_OWE_ME', amountGivenPaisa, dateGivenMillis, dueDateMillis,
                            accountId, amountReturnedPaisa, status, note, createdAt, updatedAt, 0, NULL
                        FROM money_lent
                    """.trimIndent())
                } catch (_: Exception) {
                }
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Add area to accounts
                db.execSQL("ALTER TABLE accounts ADD COLUMN area TEXT NOT NULL DEFAULT 'PERSONAL'")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_accounts_area ON accounts(area)")

                // 2. Add area to transactions
                db.execSQL("ALTER TABLE transactions ADD COLUMN area TEXT NOT NULL DEFAULT 'PERSONAL'")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_area ON transactions(area)")
                // Migrate section to area
                db.execSQL("UPDATE transactions SET area = CASE WHEN section = 'BUSINESS' THEN 'BUSINESS' ELSE 'PERSONAL' END")
                // Migrate legacy fixed categories into free-text sourceDetails safely
                db.execSQL("UPDATE transactions SET sourceDetails = category WHERE (sourceDetails IS NULL OR sourceDetails = '') AND category IS NOT NULL AND category != ''")

                // 3. Add area & phone to debts
                db.execSQL("ALTER TABLE debts ADD COLUMN area TEXT NOT NULL DEFAULT 'PERSONAL'")
                db.execSQL("ALTER TABLE debts ADD COLUMN phone TEXT NOT NULL DEFAULT ''")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_debts_area ON debts(area)")

                // 4. Create assets table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS assets (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL DEFAULT 'local_user',
                        name TEXT NOT NULL,
                        assetType TEXT NOT NULL,
                        area TEXT NOT NULL DEFAULT 'PERSONAL',
                        totalPurchaseValuePaisa INTEGER NOT NULL DEFAULT 0,
                        paidAmountPaisa INTEGER NOT NULL DEFAULT 0,
                        currentValuePaisa INTEGER NOT NULL DEFAULT 0,
                        purchaseDateMillis INTEGER NOT NULL,
                        accountId INTEGER NOT NULL DEFAULT 1,
                        installmentPaisa INTEGER NOT NULL DEFAULT 0,
                        nextPaymentDateMillis INTEGER DEFAULT NULL,
                        status TEXT NOT NULL DEFAULT 'ACTIVE',
                        note TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0,
                        isDeleted INTEGER NOT NULL DEFAULT 0,
                        deletedAtMillis INTEGER DEFAULT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_assets_userId ON assets(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_assets_area ON assets(area)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_assets_assetType ON assets(assetType)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_assets_status ON assets(status)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_assets_isDeleted ON assets(isDeleted)")

                // 5. Create stock_items table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS stock_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL DEFAULT 'local_user',
                        productName TEXT NOT NULL,
                        area TEXT NOT NULL DEFAULT 'BUSINESS',
                        purchaseCostPerUnitPaisa INTEGER NOT NULL DEFAULT 0,
                        purchasedQuantity REAL NOT NULL DEFAULT 0,
                        soldQuantity REAL NOT NULL DEFAULT 0,
                        accountId INTEGER NOT NULL DEFAULT 1,
                        dateMillis INTEGER NOT NULL,
                        note TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0,
                        isDeleted INTEGER NOT NULL DEFAULT 0,
                        deletedAtMillis INTEGER DEFAULT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_stock_items_userId ON stock_items(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_stock_items_area ON stock_items(area)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_stock_items_isDeleted ON stock_items(isDeleted)")

                // 6. Create installments table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS installments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL DEFAULT 'local_user',
                        assetId INTEGER NOT NULL,
                        amountPaisa INTEGER NOT NULL,
                        accountId INTEGER NOT NULL,
                        dateMillis INTEGER NOT NULL,
                        note TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0,
                        isDeleted INTEGER NOT NULL DEFAULT 0,
                        deletedAtMillis INTEGER DEFAULT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_installments_userId ON installments(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_installments_assetId ON installments(assetId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_installments_isDeleted ON installments(isDeleted)")

                // 7. Migrate existing investments into assets table safely
                try {
                    db.execSQL("""
                        INSERT OR IGNORE INTO assets (
                            id, userId, name, assetType, area, totalPurchaseValuePaisa, paidAmountPaisa,
                            currentValuePaisa, purchaseDateMillis, accountId, installmentPaisa, nextPaymentDateMillis,
                            status, note, createdAt, updatedAt, isDeleted, deletedAtMillis
                        )
                        SELECT 
                            id, userId, projectName, investmentType, 'PERSONAL', totalAgreedPaisa, paidAmountPaisa,
                            paidAmountPaisa, createdAt, accountId, installmentPaisa, nextPaymentDateMillis,
                            CASE WHEN paidAmountPaisa >= totalAgreedPaisa AND totalAgreedPaisa > 0 THEN 'FULLY_PAID' ELSE 'ACTIVE' END,
                            note, createdAt, updatedAt, isDeleted, deletedAtMillis
                        FROM investments
                    """.trimIndent())
                } catch (_: Exception) {
                }
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Add isOpening and originalDateMillis to debts
                db.execSQL("ALTER TABLE debts ADD COLUMN isOpening INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE debts ADD COLUMN originalDateMillis INTEGER DEFAULT NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_debts_isOpening ON debts(isOpening)")

                // 2. Add isOpening to assets
                db.execSQL("ALTER TABLE assets ADD COLUMN isOpening INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_assets_isOpening ON assets(isOpening)")

                // 3. Add isOpening to stock_items
                db.execSQL("ALTER TABLE stock_items ADD COLUMN isOpening INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_stock_items_isOpening ON stock_items(isOpening)")

                // 4. Add accountsStartDateMillis to user_profiles
                db.execSQL("ALTER TABLE user_profiles ADD COLUMN accountsStartDateMillis INTEGER DEFAULT NULL")
            }
        }

        fun getInstance(context: Context): AmarHisabDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AmarHisabDatabase::class.java,
                    "amar_hisab_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
