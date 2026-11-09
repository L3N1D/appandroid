package ru.urfu.coinpulse.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.urfu.coinpulse.domain.model.Coin

@Entity(tableName = "favorite_coins")
data class FavoriteCoinEntity(
    @PrimaryKey val id: String,
    val rank: Int,
    val symbol: String,
    val name: String,
    val imageUrl: String,
    val price: Double,
    val change24h: Double,
    val change7d: Double,
    val marketCap: Long,
    val volume24h: Long,
    val high24h: Double,
    val low24h: Double,
    val circulatingSupply: Double,
    val maxSupply: Double?,
    val ath: Double,
    val atl: Double,
    val sparkline: List<Double>,
)

fun Coin.toFavoriteEntity(): FavoriteCoinEntity = FavoriteCoinEntity(
    id = id,
    rank = rank,
    symbol = symbol,
    name = name,
    imageUrl = imageUrl,
    price = price,
    change24h = change24h,
    change7d = change7d,
    marketCap = marketCap,
    volume24h = volume24h,
    high24h = high24h,
    low24h = low24h,
    circulatingSupply = circulatingSupply,
    maxSupply = maxSupply,
    ath = ath,
    atl = atl,
    sparkline = sparkline,
)

fun FavoriteCoinEntity.toDomain(): Coin = Coin(
    id = id,
    rank = rank,
    symbol = symbol,
    name = name,
    imageUrl = imageUrl,
    price = price,
    change24h = change24h,
    change7d = change7d,
    marketCap = marketCap,
    volume24h = volume24h,
    high24h = high24h,
    low24h = low24h,
    circulatingSupply = circulatingSupply,
    maxSupply = maxSupply,
    ath = ath,
    atl = atl,
    sparkline = sparkline,
)
