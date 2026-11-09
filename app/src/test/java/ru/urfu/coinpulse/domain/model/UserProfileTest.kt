package ru.urfu.coinpulse.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserProfileTest {

    @Test
    fun `default profile is empty`() {
        assertTrue(UserProfile().isEmpty)
    }

    @Test
    fun `any filled field makes profile non empty`() {
        assertFalse(UserProfile(fullName = "Ivan").isEmpty)
        assertFalse(UserProfile(avatarUri = "content://avatar").isEmpty)
        assertFalse(UserProfile(resumeUrl = "https://example.com/cv.pdf").isEmpty)
        assertFalse(UserProfile(classTime = "09:30").isEmpty)
    }
}
