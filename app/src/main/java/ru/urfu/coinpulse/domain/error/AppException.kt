package ru.urfu.coinpulse.domain.error

/**
 * Errors the data layer is allowed to expose to the domain/presentation layers.
 */
sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    class NoConnection(cause: Throwable? = null) : AppException(
        "No internet connection. Check your network and try again.",
        cause,
    )

    class RateLimited : AppException(
        "CoinGecko rate limit reached. Please wait about a minute and retry.",
    )

    class Server(val code: Int) : AppException(
        "CoinGecko returned an error ($code). Please try again later.",
    )

    class EmptyResponse : AppException(
        "CoinGecko returned an empty response. Please try again.",
    )

    class InvalidUrl : AppException(
        "Enter a valid http:// or https:// URL.",
    )

    class Unknown(cause: Throwable) : AppException(
        "Something went wrong while loading data. Please try again.",
        cause,
    )
}
