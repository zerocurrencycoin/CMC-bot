package at.chaoticbits.coinmarket

import org.junit.Assert
import org.junit.Test


/**
 * Test CoinMarketCap Schedulers
 */
class CoinMarketSchedulerTest {

    @Test
    fun testRun() {

        val coinMarketScheduler = CoinMarketScheduler()

        coinMarketScheduler.run()

        Assert.assertNotNull(CoinMarketContainer.erc20Tokens)
        Assert.assertNotNull(CoinMarketContainer.coinListings)
    }
}
