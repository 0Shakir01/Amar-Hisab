package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.MoneyLentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoneyLentDao {

    @Query("SELECT * FROM money_lent WHERE (:userId = 'local_user' OR userId = :userId OR userId = 'local_user') ORDER BY dateGivenMillis DESC")
    fun getAllMoneyLent(userId: String = "local_user"): Flow<List<MoneyLentEntity>>

    @Query("SELECT * FROM money_lent WHERE id = :id LIMIT 1")
    fun getMoneyLentById(id: Long): Flow<MoneyLentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoneyLent(item: MoneyLentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MoneyLentEntity>)

    @Update
    suspend fun updateMoneyLent(item: MoneyLentEntity)

    @Delete
    suspend fun deleteMoneyLent(item: MoneyLentEntity)

    @Query("DELETE FROM money_lent WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String = "local_user")
}
