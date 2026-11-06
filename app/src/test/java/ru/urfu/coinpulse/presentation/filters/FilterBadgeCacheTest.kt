package ru.urfu.coinpulse.presentation.filters

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FilterBadgeCacheTest {

    @Test
    fun `shared cache tracks badge visibility`() {
        val cache = FilterBadgeCache()
        assertFalse(cache.visible.value)

        cache.update(true)
        assertTrue(cache.visible.value)

        cache.update(false)
        assertFalse(cache.visible.value)
    }
}
