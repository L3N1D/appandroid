package ru.urfu.coinpulse.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileValidatorTest {

    @Test
    fun `name is required`() {
        assertEquals(
            "Enter your full name.",
            ProfileValidator.validate("", "", "09:30"),
        )
    }

    @Test
    fun `blank resume url is allowed`() {
        assertNull(ProfileValidator.validate("Ivan Ivanov", "", "09:30"))
    }

    @Test
    fun `resume url must use http or https`() {
        assertEquals(
            "Resume URL must start with http:// or https://",
            ProfileValidator.validate("Ivan Ivanov", "ftp://example.com/resume.pdf", "09:30"),
        )
        assertNull(
            ProfileValidator.validate(
                "Ivan Ivanov",
                "https://example.com/resume.pdf",
                "09:30",
            ),
        )
    }

    @Test
    fun `class time is required`() {
        assertEquals(
            "Set the class time in HH:mm format.",
            ProfileValidator.validate("Ivan Ivanov", "", ""),
        )
    }

    @Test
    fun `class time must be valid 24 hour time`() {
        assertTrue(ProfileValidator.isValidClassTime("00:00"))
        assertTrue(ProfileValidator.isValidClassTime("23:59"))
        assertFalse(ProfileValidator.isValidClassTime("24:00"))
        assertFalse(ProfileValidator.isValidClassTime("09:60"))
        assertFalse(ProfileValidator.isValidClassTime("9:30"))
        assertEquals(
            "Use 24-hour HH:mm format (for example 09:30).",
            ProfileValidator.validate("Ivan Ivanov", "", "25:99"),
        )
    }
}
