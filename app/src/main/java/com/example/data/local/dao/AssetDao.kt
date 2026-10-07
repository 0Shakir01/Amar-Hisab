package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    @Query("SELECT * FROM assets WHERE (:userId = 'local_user' OR userId = :userId OR userId = 'local_user') AND isDeleted = 0 ORDER BY purchaseDateMillis DESC")
    fun getAssets(userId: String = "local_user"): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE (:userId = 'local_user' OR userId = :userId OR userId = 'local_user') AND area = :area AND isDeleted = 0 ORDER BY purchaseDateMillis DESC")
    fun getAssetsByArea(area: String, userId: String = "local_user"): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE (:userId = 'local_user' OR userId = :userId OR userId = 'local_user') AND isDeleted = 1 ORDER BY deletedAtMillis DESC")
    fun getDeletedAssets(userId: String = "local_user"): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE id = :id LIMIT 1")
    suspend fun getAssetById(id: Long): AssetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: AssetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<AssetEntity>): List<Long>

    @Update
    suspend fun updateAsset(asset: AssetEntity)

    @Query("UPDATE assets SET isDeleted = 1, deletedAtMillis = :deletedAt, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteById(id: Long, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE assets SET isDeleted = 0, deletedAtMillis = NULL, updatedAt = :updatedAt WHERE id = :id")
    suspend fun restoreById(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteAsset(asset: AssetEntity)

    @Query("DELETE FROM assets WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM assets WHERE userId = :userId")
    suspend fun deleteAll(userId: String = "local_user")
}
