package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DebtEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts WHERE (:userId = 'local_user' OR userId = :userId OR userId = 'local_user') AND isDeleted = 0 ORDER BY dateMillis DESC, id DESC")
    fun getDebts(userId: String = "local_user"): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE (:userId = 'local_user' OR userId = :userId OR userId = 'local_user') AND isDeleted = 1 ORDER BY deletedAtMillis DESC")
    fun getDeletedDebts(userId: String = "local_user"): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    suspend fun getDebtById(id: Long): DebtEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebts(debts: List<DebtEntity>): List<Long>

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Query("UPDATE debts SET isDeleted = 1, deletedAtMillis = :deletedAt, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteById(id: Long, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE debts SET isDeleted = 0, deletedAtMillis = NULL, updatedAt = :updatedAt WHERE id = :id")
    suspend fun restoreById(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteDebt(debt: DebtEntity)

    @Query("DELETE FROM debts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM debts WHERE userId = :userId")
    suspend fun deleteAll(userId: String = "local_user")
}
