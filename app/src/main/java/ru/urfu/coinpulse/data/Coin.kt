package ru.urfu.coinpulse.data

/**
 * Domain model for a single row of `/coins/markets`.
 */
data class Coin(
    val id: String,
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
) {
    val isPositive24h: Boolean get() = change24h >= 0.0
}

/**
 * Domain model for `/coins/{id}` — the market fields plus the extra detail fields.
 */
data class CoinDetails(
    val coin: Coin,
    val description: String,
    val homepage: String,
    val githubUrl: String?,
    val subredditUrl: String?,
    val categories: List<String>,
    val genesisDate: String,
    val hashingAlgorithm: String?,
    val change30d: Double,
    val change60d: Double,
    val change1y: Double,
    val fullyDilutedValuation: Long?,
    val totalSupply: Double?,
    val lastUpdated: String,
)

data class CoinPage(
    val items: List<Coin>,
    val hasMore: Boolean,
    val total: Int,
)

enum class CoinSort(val label: String) {
    MARKET_CAP("Market cap"),
    PRICE("Price"),
    CHANGE_24H("24h change"),
}

enum class ChartRange(val label: String) {
    DAY("1D"),
    WEEK("7D"),
    MONTH("30D"),
    YEAR("1Y"),
}
