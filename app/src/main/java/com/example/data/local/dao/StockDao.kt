package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.StockItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    @Query("SELECT * FROM stock_items WHERE (:userId = 'local_user' OR userId = :userId OR userId = 'local_user') AND isDeleted = 0 ORDER BY dateMillis DESC")
    fun getStockItems(userId: String = "local_user"): Flow<List<StockItemEntity>>

    @Query("SELECT * FROM stock_items WHERE (:userId = 'local_user' OR userId = :userId OR userId = 'local_user') AND area = :area AND isDeleted = 0 ORDER BY dateMillis DESC")
    fun getStockItemsByArea(area: String, userId: String = "local_user"): Flow<List<StockItemEntity>>

    @Query("SELECT * FROM stock_items WHERE (:userId = 'local_user' OR userId = :userId OR userId = 'local_user') AND isDeleted = 1 ORDER BY deletedAtMillis DESC")
    fun getDeletedStockItems(userId: String = "local_user"): Flow<List<StockItemEntity>>

    @Query("SELECT * FROM stock_items WHERE id = :id LIMIT 1")
    suspend fun getStockItemById(id: Long): StockItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockItem(item: StockItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockItems(items: List<StockItemEntity>): List<Long>

    @Update
    suspend fun updateStockItem(item: StockItemEntity)

    @Query("UPDATE stock_items SET isDeleted = 1, deletedAtMillis = :deletedAt, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteById(id: Long, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE stock_items SET isDeleted = 0, deletedAtMillis = NULL, updatedAt = :updatedAt WHERE id = :id")
    suspend fun restoreById(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteStockItem(item: StockItemEntity)

    @Query("DELETE FROM stock_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM stock_items WHERE userId = :userId")
    suspend fun deleteAll(userId: String = "local_user")
}
