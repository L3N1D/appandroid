package ru.urfu.coinpulse.data.remote.mapper

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import ru.urfu.coinpulse.data.remote.dto.CoinDetailsDto
import ru.urfu.coinpulse.data.remote.dto.MarketChartDto
import ru.urfu.coinpulse.data.remote.dto.MarketCoinDto
import ru.urfu.coinpulse.domain.model.Coin
import ru.urfu.coinpulse.domain.model.CoinDetails

fun MarketCoinDto.toDomain(fallbackRank: Int): Coin? {
    val coinId = id ?: return null
    val coinName = name ?: coinId
    val currentPrice = currentPrice ?: 0.0

    return Coin(
        id = coinId,
        rank = marketCapRank ?: fallbackRank,
        symbol = (symbol ?: "").uppercase(Locale.US),
        name = coinName,
        imageUrl = image.orEmpty(),
        price = currentPrice,
        change24h = priceChange24hInCurrency ?: priceChange24h ?: 0.0,
        change7d = priceChange7dInCurrency ?: 0.0,
        marketCap = marketCap?.toLong() ?: 0L,
        volume24h = totalVolume?.toLong() ?: 0L,
        high24h = high24h ?: currentPrice,
        low24h = low24h ?: currentPrice,
        circulatingSupply = circulatingSupply ?: 0.0,
        maxSupply = maxSupply,
        ath = ath ?: 0.0,
        atl = atl ?: 0.0,
        sparkline = sparklineIn7d?.price.orEmpty(),
    )
}

fun CoinDetailsDto.toDomain(fallback: Coin? = null): CoinDetails? {
    val coinId = id ?: fallback?.id ?: return null
    val market = marketData
    val price = market?.currentPrice.usd() ?: fallback?.price ?: 0.0
    val rank = marketCapRank ?: fallback?.rank ?: 0

    val coin = Coin(
        id = coinId,
        rank = rank,
        symbol = (symbol ?: fallback?.symbol ?: "").uppercase(Locale.US),
        name = name ?: fallback?.name ?: coinId,
        imageUrl = image?.large ?: image?.small ?: fallback?.imageUrl.orEmpty(),
        price = price,
        change24h = market?.priceChange24h ?: fallback?.change24h ?: 0.0,
        change7d = market?.priceChange7d ?: fallback?.change7d ?: 0.0,
        marketCap = market?.marketCap.usd()?.toLong() ?: fallback?.marketCap ?: 0L,
        volume24h = market?.totalVolume.usd()?.toLong() ?: fallback?.volume24h ?: 0L,
        high24h = market?.high24h.usd() ?: fallback?.high24h ?: price,
        low24h = market?.low24h.usd() ?: fallback?.low24h ?: price,
        circulatingSupply = market?.circulatingSupply ?: fallback?.circulatingSupply ?: 0.0,
        maxSupply = market?.maxSupply ?: fallback?.maxSupply,
        ath = market?.ath.usd() ?: fallback?.ath ?: 0.0,
        atl = market?.atl.usd() ?: fallback?.atl ?: 0.0,
        sparkline = market?.sparkline7d?.price?.takeIf { it.isNotEmpty() } ?: fallback?.sparkline.orEmpty(),
    )

    val descriptionText = stripHtml(description?.en.orEmpty()).trim()
    val homepage = links?.homepage?.firstOrNull { it.isNotBlank() }
        ?: "https://www.coingecko.com/en/coins/$coinId"
    val categories = categories.orEmpty().filter { it.isNotBlank() }

    return CoinDetails(
        coin = coin,
        description = descriptionText.ifBlank { "No description is available for this coin yet." },
        homepage = homepage,
        githubUrl = links?.reposUrl?.github?.firstOrNull { it.isNotBlank() },
        subredditUrl = links?.subredditUrl?.takeIf { it.isNotBlank() },
        categories = categories,
        genesisDate = genesisDate?.takeIf { it.isNotBlank() } ?: "—",
        hashingAlgorithm = hashingAlgorithm?.takeIf { it.isNotBlank() },
        change30d = market?.priceChange30d ?: 0.0,
        change60d = market?.priceChange60d ?: 0.0,
        change1y = market?.priceChange1y ?: 0.0,
        fullyDilutedValuation = market?.fullyDilutedValuation.usd()?.toLong()
            ?: (coin.maxSupply?.times(price)?.toLong()),
        totalSupply = market?.totalSupply,
        lastUpdated = formatLastUpdated(lastUpdated),
    )
}

fun MarketChartDto.toPricePoints(): List<Double> = prices
    .orEmpty()
    .mapNotNull { point -> point.getOrNull(1) }
    .filter { it > 0.0 }

private fun Map<String, Double>?.usd(): Double? = this?.get("usd")

private fun stripHtml(value: String): String = value
    .replace(Regex("<[^>]*>"), " ")
    .replace("&amp;", "&")
    .replace("&quot;", "\"")
    .replace("&#39;", "'")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&nbsp;", " ")
    .replace(Regex("\\s+"), " ")
    .trim()

private fun formatLastUpdated(raw: String?): String {
    if (raw.isNullOrBlank()) return "just now"
    return runCatching {
        val withoutMillis = raw.take(19)
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(withoutMillis) ?: return@runCatching "just now"
        val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm 'UTC'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        formatter.format(date)
    }.getOrDefault("just now")
}
