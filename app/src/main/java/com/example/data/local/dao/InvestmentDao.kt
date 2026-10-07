package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.InvestmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvestmentDao {
    @Query("SELECT * FROM investments WHERE (:userId = 'local_user' OR userId = :userId OR userId = 'local_user') AND isDeleted = 0 ORDER BY id DESC")
    fun getInvestments(userId: String = "local_user"): Flow<List<InvestmentEntity>>

    @Query("SELECT * FROM investments WHERE (:userId = 'local_user' OR userId = :userId OR userId = 'local_user') AND isDeleted = 1 ORDER BY deletedAtMillis DESC")
    fun getDeletedInvestments(userId: String = "local_user"): Flow<List<InvestmentEntity>>

    @Query("SELECT * FROM investments WHERE id = :id LIMIT 1")
    suspend fun getInvestmentById(id: Long): InvestmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvestment(investment: InvestmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvestments(investments: List<InvestmentEntity>): List<Long>

    @Update
    suspend fun updateInvestment(investment: InvestmentEntity)

    @Query("UPDATE investments SET isDeleted = 1, deletedAtMillis = :deletedAt, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteById(id: Long, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE investments SET isDeleted = 0, deletedAtMillis = NULL, updatedAt = :updatedAt WHERE id = :id")
    suspend fun restoreById(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteInvestment(investment: InvestmentEntity)

    @Query("DELETE FROM investments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM investments WHERE userId = :userId")
    suspend fun deleteAll(userId: String = "local_user")
}
