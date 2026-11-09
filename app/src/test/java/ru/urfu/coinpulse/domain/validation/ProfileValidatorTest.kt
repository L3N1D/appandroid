package ru.urfu.coinpulse.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileValidatorTest {

    @Test
    fun `name is required`() {
        assertEquals("Enter your full name.", ProfileValidator.validate("", ""))
    }

    @Test
    fun `blank resume url is allowed`() {
        assertNull(ProfileValidator.validate("Ivan Ivanov", ""))
    }

    @Test
    fun `resume url must use http or https`() {
        assertEquals(
            "Resume URL must start with http:// or https://",
            ProfileValidator.validate("Ivan Ivanov", "ftp://example.com/resume.pdf"),
        )
        assertNull(
            ProfileValidator.validate(
                "Ivan Ivanov",
                "https://example.com/resume.pdf",
            ),
        )
    }
}
