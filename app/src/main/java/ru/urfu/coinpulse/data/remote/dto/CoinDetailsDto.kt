package ru.urfu.coinpulse.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ImageDto(
    val large: String? = null,
    val small: String? = null,
    val thumb: String? = null,
)

data class DescriptionDto(
    val en: String? = null,
)

data class ReposUrlDto(
    val github: List<String>? = null,
)

data class LinksDto(
    val homepage: List<String>? = null,
    @SerializedName("repos_url") val reposUrl: ReposUrlDto? = null,
    @SerializedName("subreddit_url") val subredditUrl: String? = null,
)

data class MarketDataDto(
    @SerializedName("current_price") val currentPrice: Map<String, Double>? = null,
    @SerializedName("market_cap") val marketCap: Map<String, Double>? = null,
    @SerializedName("total_volume") val totalVolume: Map<String, Double>? = null,
    @SerializedName("high_24h") val high24h: Map<String, Double>? = null,
    @SerializedName("low_24h") val low24h: Map<String, Double>? = null,
    @SerializedName("price_change_percentage_24h") val priceChange24h: Double? = null,
    @SerializedName("price_change_percentage_7d") val priceChange7d: Double? = null,
    @SerializedName("price_change_percentage_30d") val priceChange30d: Double? = null,
    @SerializedName("price_change_percentage_60d") val priceChange60d: Double? = null,
    @SerializedName("price_change_percentage_1y") val priceChange1y: Double? = null,
    @SerializedName("circulating_supply") val circulatingSupply: Double? = null,
    @SerializedName("total_supply") val totalSupply: Double? = null,
    @SerializedName("max_supply") val maxSupply: Double? = null,
    val ath: Map<String, Double>? = null,
    val atl: Map<String, Double>? = null,
    @SerializedName("fully_diluted_valuation") val fullyDilutedValuation: Map<String, Double>? = null,
    @SerializedName("sparkline_7d") val sparkline7d: SparklineDto? = null,
)

data class CoinDetailsDto(
    val id: String? = null,
    val symbol: String? = null,
    val name: String? = null,
    val image: ImageDto? = null,
    val description: DescriptionDto? = null,
    val links: LinksDto? = null,
    val categories: List<String>? = null,
    @SerializedName("genesis_date") val genesisDate: String? = null,
    @SerializedName("hashing_algorithm") val hashingAlgorithm: String? = null,
    @SerializedName("market_cap_rank") val marketCapRank: Int? = null,
    @SerializedName("market_data") val marketData: MarketDataDto? = null,
    @SerializedName("last_updated") val lastUpdated: String? = null,
)
