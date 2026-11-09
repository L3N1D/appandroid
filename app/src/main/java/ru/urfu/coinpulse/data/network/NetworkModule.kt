package ru.urfu.coinpulse.data.network

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import java.io.File
import java.util.concurrent.TimeUnit
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.urfu.coinpulse.data.remote.CoinGeckoApi

/**
 * Creates the network stack once per process.
 *
 * The keyless CoinGecko API has a strict rate limit, so the client also uses an
 * HTTP cache. Chucker is only attached to debuggable builds.
 */
class NetworkModule(context: Context) {

    private val appContext = context.applicationContext
    private val isDebuggable: Boolean =
        (appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    val client: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .cache(Cache(File(appContext.cacheDir, "coinpulse_http_cache"), 20L * 1024 * 1024))
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(RequestHeadersInterceptor())
            .addInterceptor(RateLimitRetryInterceptor())
            .addNetworkInterceptor(CacheControlInterceptor())

        if (isDebuggable) {
            val collector = ChuckerCollector(
                context = appContext,
                showNotification = true,
                retentionPeriod = RetentionManager.Period.ONE_HOUR,
            )
            builder.addInterceptor(
                ChuckerInterceptor.Builder(appContext)
                    .collector(collector)
                    .maxContentLength(250_000L)
                    .redactHeaders("Authorization", "Cookie")
                    .alwaysReadResponseBody(true)
                    .build(),
            )
        }

        builder.build()
    }

    val api: CoinGeckoApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CoinGeckoApi::class.java)
    }

    companion object {
        const val BASE_URL = "https://api.coingecko.com/api/v3/"
    }
}

private class RequestHeadersInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Accept", "application/json")
            .header("User-Agent", "CoinPulse/1.0 (Android practice; ${Build.MODEL})")
            .build()
        return chain.proceed(request)
    }
}

/**
 * CoinGecko's keyless API does not always send cache headers, so give successful
 * GET responses a small TTL. This is what keeps the demo below the rate limit.
 */
private class CacheControlInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (!response.isSuccessful) return response
        return response.newBuilder()
            .header("Cache-Control", "public, max-age=60")
            .removeHeader("Pragma")
            .build()
    }
}

/**
 * A single polite retry for HTTP 429. If the limit is still active the repository
 * surfaces [ru.urfu.coinpulse.domain.error.AppException.RateLimited].
 */
private class RateLimitRetryInterceptor(
    private val maxRetries: Int = 1,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var response = chain.proceed(chain.request())
        var attempt = 0
        while (response.code == 429 && attempt < maxRetries) {
            val retryAfterSeconds = response.header("Retry-After")
                ?.toLongOrNull()
                ?.coerceIn(1, 10)
                ?: 2L
            response.close()
            Thread.sleep(retryAfterSeconds * 1_000)
            response = chain.proceed(chain.request())
            attempt++
        }
        return response
    }
}
