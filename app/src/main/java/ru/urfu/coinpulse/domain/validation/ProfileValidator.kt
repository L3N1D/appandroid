package ru.urfu.coinpulse.domain.validation

object ProfileValidator {

    private val classTimeRegex = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")

    fun isValidClassTime(value: String): Boolean = classTimeRegex.matches(value)

    fun validate(
        fullName: String,
        resumeUrl: String,
        classTime: String,
    ): String? = when {
        fullName.isBlank() -> "Enter your full name."
        resumeUrl.isNotBlank() &&
            !resumeUrl.startsWith("http://") &&
            !resumeUrl.startsWith("https://") ->
            "Resume URL must start with http:// or https://"
        classTime.isBlank() -> "Set the class time in HH:mm format."
        !isValidClassTime(classTime) ->
            "Use 24-hour HH:mm format (for example 09:30)."
        else -> null
    }
}
