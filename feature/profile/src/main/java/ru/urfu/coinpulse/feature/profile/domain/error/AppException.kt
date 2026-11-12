package ru.urfu.coinpulse.feature.profile.domain.error

sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    class NoConnection(cause: Throwable? = null) : AppException(
        "No internet connection. Check your network and try again.",
        cause,
    )

    class RateLimited : AppException(
        "The server rate limit was reached. Please wait a moment and retry.",
    )

    class Server(val code: Int) : AppException(
        "The server returned an error ($code). Please try again later.",
    )

    class EmptyResponse : AppException(
        "The server returned an empty response. Please try again.",
    )

    class InvalidUrl : AppException(
        "Enter a valid http:// or https:// URL.",
    )

    class InvalidTime : AppException(
        "Enter the class time in 24-hour HH:mm format.",
    )

    class Unknown(cause: Throwable) : AppException(
        "Something went wrong while loading data. Please try again.",
        cause,
    )
}
