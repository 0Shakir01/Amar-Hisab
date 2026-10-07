package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE (:userId = 'local_user' OR userId = :userId OR userId = 'local_user') AND isDeleted = 0 ORDER BY id ASC")
    fun getAccounts(userId: String = "local_user"): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE (:userId = 'local_user' OR userId = :userId OR userId = 'local_user') AND isDeleted = 1 ORDER BY deletedAtMillis DESC")
    fun getDeletedAccounts(userId: String = "local_user"): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>): List<Long>

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("UPDATE accounts SET isDeleted = 1, deletedAtMillis = :deletedAt, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteById(id: Long, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE accounts SET isDeleted = 0, deletedAtMillis = NULL, updatedAt = :updatedAt WHERE id = :id")
    suspend fun restoreById(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM accounts WHERE userId = :userId")
    suspend fun deleteAll(userId: String = "local_user")
}
