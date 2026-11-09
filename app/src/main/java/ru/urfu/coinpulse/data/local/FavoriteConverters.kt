package ru.urfu.coinpulse.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FavoriteConverters {

    private val gson = Gson()
    private val listType = object : TypeToken<List<Double>>() {}.type

    @TypeConverter
    fun fromSparkline(value: List<Double>): String = gson.toJson(value)

    @TypeConverter
    fun toSparkline(value: String): List<Double> =
        runCatching { gson.fromJson<List<Double>>(value, listType) ?: emptyList() }
            .getOrDefault(emptyList())
}
