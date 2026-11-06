package ru.urfu.coinpulse.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteCoinDao {

    @Query("SELECT * FROM favorite_coins ORDER BY rank ASC")
    fun observeAll(): Flow<List<FavoriteCoinEntity>>

    @Query("SELECT id FROM favorite_coins")
    suspend fun getFavoriteIds(): List<String>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_coins WHERE id = :id)")
    suspend fun exists(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteCoinEntity)

    @Query("DELETE FROM favorite_coins WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM favorite_coins")
    suspend fun clear()
}
