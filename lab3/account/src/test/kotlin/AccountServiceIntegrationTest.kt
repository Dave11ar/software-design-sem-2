import account.AccountService
import account.AccountServiceImpl
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.plugins.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.jupiter.api.*
import org.testcontainers.containers.FixedHostPortGenericContainer

import org.testcontainers.containers.GenericContainer
import stocks.HttpStocksServiceImpl
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class AccountServiceIntegrationTest {
    private val port = 8080

    private var stocksMarketContainer: GenericContainer<*> = FixedHostPortGenericContainer("stocks:latest")
        .withExposedPorts(port)
        .withFixedExposedPort(port, port)
        .withCommand("bin/app")

    private val httpClient: HttpClient = HttpClient()
    private val accountService: AccountService = AccountServiceImpl(HttpStocksServiceImpl(httpClient, "http://localhost:$port"))

    @Before
    fun setUp() {
        stocksMarketContainer.start()
    }

    @After
    fun tearDown() {
        stocksMarketContainer.stop()
    }

    @Test
    fun integrationTest() = runBlocking {
        initStocks()

        // Test user creation
        accountService.addUser(user1)
        accountService.addUser(user2)

        // Test balance top up
        accountService.topUpAccount(user1, user1InitialBalance)
        accountService.topUpAccount(user2, user2InitialBalance)
        assertThrows<BadRequestException> { accountService.topUpAccount("nonExistingUser", 100000) }

        // Test total balance with no stocks purchased
        assertEquals(user1InitialBalance, accountService.getTotal(user1))
        assertEquals(user2InitialBalance, accountService.getTotal(user2))
        assertThrows<BadRequestException> { accountService.getTotal("nonExistingUser") }

        // Test purchasing stock
        accountService.buy(user1, stock, 10)
        assertThrows<BadRequestException> { accountService.buy(user2, stock, 1000000000) }
        assertThrows<BadRequestException> { accountService.buy(user1, emptyStock, 1) }

        // Test selling stock
        accountService.sell(user1, stock, 5)
        assertThrows<BadRequestException> { accountService.sell(user2, stock, 1) }

        // Test stocks accounting
        assertTrue {
            val stocks = accountService.getStocks(user1)
            stocks.size == 1 && stocks[stock]!!.first == 5L
        }
        assertTrue {
            val stocks = accountService.getStocks(user2)
            stocks.isEmpty()
        }

        // Test updates on balance after price change
        changePrice(stock, stockPrice * 2)
        assertEquals(user1InitialBalance + 5 * stockPrice, accountService.getTotal(user1))
        assertEquals(user2InitialBalance, accountService.getTotal(user2))
    }

    private suspend inline fun <T>  makePost(body: T, query: String = "") {
        httpClient.post("http://localhost:$port/stocks/$query") {
            contentType(ContentType.Application.Json.withParameter("charset", "utf-8"))
            setBody(jacksonObjectMapper().writeValueAsString(body))
        }
    }

    private suspend fun initStocks() {
        makePost(AddStocksRequest(stock, stockPrice, stockCount))
        makePost(AddStocksRequest(emptyStock, emptyStockPrice, emptyStockCount))
    }

    private suspend fun changePrice(ticker: String, newPrice: Long) {
        makePost(ChangePriceRequest(newPrice), ticker)
    }

    companion object {
        const val user1 = "user1"
        const val user2 = "user2"

        const val user1InitialBalance = 1000L
        const val user2InitialBalance = 10000L

        const val stock = "AAPL"
        const val stockCount = 1000L
        const val stockPrice = 100L

        const val emptyStock = "NS"
        const val emptyStockCount = 0L
        const val emptyStockPrice = 100L

        data class AddStocksRequest(val ticker: String, val price: Long, val count: Long)
        data class ChangePriceRequest(val newPrice: Long)
    }
}
