package ru.urfu.coinpulse.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SparklineDto(
    val price: List<Double>? = null,
)

data class MarketCoinDto(
    val id: String? = null,
    val symbol: String? = null,
    val name: String? = null,
    val image: String? = null,
    @SerializedName("current_price") val currentPrice: Double? = null,
    @SerializedName("market_cap") val marketCap: Double? = null,
    @SerializedName("market_cap_rank") val marketCapRank: Int? = null,
    @SerializedName("total_volume") val totalVolume: Double? = null,
    @SerializedName("high_24h") val high24h: Double? = null,
    @SerializedName("low_24h") val low24h: Double? = null,
    @SerializedName("price_change_percentage_24h") val priceChange24h: Double? = null,
    @SerializedName("price_change_percentage_24h_in_currency") val priceChange24hInCurrency: Double? = null,
    @SerializedName("price_change_percentage_7d_in_currency") val priceChange7dInCurrency: Double? = null,
    @SerializedName("circulating_supply") val circulatingSupply: Double? = null,
    @SerializedName("max_supply") val maxSupply: Double? = null,
    val ath: Double? = null,
    val atl: Double? = null,
    @SerializedName("sparkline_in_7d") val sparklineIn7d: SparklineDto? = null,
)
