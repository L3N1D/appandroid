package ru.urfu.coinpulse.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MockCoinRepositoryTest {

    private val repository = MockCoinRepository(simulateNetworkDelay = false)

    @Test
    fun `first page returns ten coins and reports more pages`() = runBlocking {
        val page = repository.getCoins(page = 0, pageSize = 10)

        assertEquals(10, page.items.size)
        assertEquals(30, page.total)
        assertTrue(page.hasMore)
    }

    @Test
    fun `last page contains the remaining coins`() = runBlocking {
        val page = repository.getCoins(page = 2, pageSize = 10)

        assertEquals(10, page.items.size)
        assertFalse(page.hasMore)
    }

    @Test
    fun `page beyond the end is empty`() = runBlocking {
        val page = repository.getCoins(page = 4, pageSize = 10)

        assertTrue(page.items.isEmpty())
        assertFalse(page.hasMore)
    }

    @Test
    fun `search matches names and tickers`() = runBlocking {
        val byName = repository.getCoins(query = "bitcoin", pageSize = 10)
        val byTicker = repository.getCoins(query = "btc", pageSize = 10)

        assertEquals(2, byName.total) // Bitcoin and Bitcoin Cash
        assertEquals(1, byTicker.total)
        assertEquals("bitcoin", byTicker.items.first().id)
    }

    @Test
    fun `sorting by price and change works`() = runBlocking {
        val byPrice = repository.getCoins(sort = CoinSort.PRICE, pageSize = 8)
        val byChange = repository.getCoins(sort = CoinSort.CHANGE_24H, pageSize = 8)

        assertEquals("bitcoin", byPrice.items.first().id)
        assertEquals("pepe", byChange.items.first().id)
    }

    @Test
    fun `favorites are shared and toggled`() = runBlocking {
        assertTrue("bitcoin" in repository.favoriteIds.value)

        repository.toggleFavorite("ethereum")
        assertTrue("ethereum" in repository.favoriteIds.value)
        assertTrue(repository.getFavoriteCoins().any { it.id == "ethereum" })

        repository.toggleFavorite("ethereum")
        assertFalse("ethereum" in repository.favoriteIds.value)
    }

    @Test
    fun `details expose coin gecko shaped fields`() = runBlocking {
        val details = repository.getCoinDetails("bitcoin")

        assertNotNull(details)
        requireNotNull(details)
        assertEquals("Bitcoin", details.coin.name)
        assertTrue(details.categories.contains("Layer 1"))
        assertTrue(details.description.contains("Bitcoin"))
        assertEquals("https://bitcoin.org", details.homepage)
        assertEquals(21_000_000.0, details.coin.maxSupply!!, 0.01)
    }

    @Test
    fun `chart series keeps the requested range and current price`() = runBlocking {
        val coin = repository.getCoins(pageSize = 1).items.first()

        val week = repository.getChartPoints(coin, ChartRange.WEEK)
        val month = repository.getChartPoints(coin, ChartRange.MONTH)

        assertEquals(56, week.size)
        assertEquals(72, month.size)
        assertEquals(coin.price, week.last(), 0.000001)
        assertEquals(coin.price, month.last(), 0.000001)
    }
}
