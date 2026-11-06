package ru.urfu.coinpulse.data.remote

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import ru.urfu.coinpulse.data.remote.dto.CoinDetailsDto
import ru.urfu.coinpulse.data.remote.dto.MarketChartDto
import ru.urfu.coinpulse.data.remote.dto.MarketCoinDto

interface CoinGeckoApi {

    @GET("coins/markets")
    suspend fun getMarkets(
        @Query("vs_currency") vsCurrency: String,
        @Query("order") order: String,
        @Query("per_page") perPage: Int,
        @Query("page") page: Int,
        @Query("sparkline") sparkline: Boolean,
        @Query("price_change_percentage") priceChangePercentage: String,
        @Header("Cache-Control") cacheControl: String?,
    ): List<MarketCoinDto>

    @GET("coins/{id}")
    suspend fun getCoinDetails(
        @Path("id") id: String,
        @Query("localization") localization: Boolean,
        @Query("tickers") tickers: Boolean,
        @Query("market_data") marketData: Boolean,
        @Query("community_data") communityData: Boolean,
        @Query("developer_data") developerData: Boolean,
        @Query("sparkline") sparkline: Boolean,
        @Header("Cache-Control") cacheControl: String?,
    ): CoinDetailsDto

    @GET("coins/{id}/market_chart")
    suspend fun getMarketChart(
        @Path("id") id: String,
        @Query("vs_currency") vsCurrency: String,
        @Query("days") days: Int,
        @Header("Cache-Control") cacheControl: String?,
    ): MarketChartDto
}
