package ru.urfu.coinpulse.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [FavoriteCoinEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(FavoriteConverters::class)
abstract class CoinPulseDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteCoinDao
}
