package ru.urfu.coinpulse.data.remote.dto

data class MarketChartDto(
    val prices: List<List<Double>>? = null,
)
