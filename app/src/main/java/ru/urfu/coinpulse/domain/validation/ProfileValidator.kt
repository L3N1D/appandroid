package ru.urfu.coinpulse.domain.validation

object ProfileValidator {

    fun validate(fullName: String, resumeUrl: String): String? = when {
        fullName.isBlank() -> "Enter your full name."
        resumeUrl.isNotBlank() &&
            !resumeUrl.startsWith("http://") &&
            !resumeUrl.startsWith("https://") ->
            "Resume URL must start with http:// or https://"
        else -> null
    }
}
