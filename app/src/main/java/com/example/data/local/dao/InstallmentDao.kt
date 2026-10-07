package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.InstallmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstallmentDao {
    @Query("SELECT * FROM installments WHERE (:userId = 'local_user' OR userId = :userId OR userId = 'local_user') AND isDeleted = 0 ORDER BY dateMillis DESC")
    fun getInstallments(userId: String = "local_user"): Flow<List<InstallmentEntity>>

    @Query("SELECT * FROM installments WHERE assetId = :assetId AND isDeleted = 0 ORDER BY dateMillis DESC")
    fun getInstallmentsByAsset(assetId: Long): Flow<List<InstallmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstallment(installment: InstallmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstallments(installments: List<InstallmentEntity>): List<Long>

    @Update
    suspend fun updateInstallment(installment: InstallmentEntity)

    @Query("UPDATE installments SET isDeleted = 1, deletedAtMillis = :deletedAt, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteById(id: Long, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE installments SET isDeleted = 0, deletedAtMillis = NULL, updatedAt = :updatedAt WHERE id = :id")
    suspend fun restoreById(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteInstallment(installment: InstallmentEntity)

    @Query("DELETE FROM installments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM installments WHERE userId = :userId")
    suspend fun deleteAll(userId: String = "local_user")
}
