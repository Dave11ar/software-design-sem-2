package account

import io.ktor.server.plugins.*
import stocks.StocksService

class AccountServiceImpl(private val stocksService: StocksService): AccountService {
    private val balances: MutableMap<String, Long> = mutableMapOf()
    private val stocks: MutableMap<String, MutableMap<String, Long>> = mutableMapOf()

    override fun addUser(login: String) {
        if (balances.containsKey(login)) {
            throw BadRequestException("User already exists")
        }

        balances[login] = 0
        stocks[login] = mutableMapOf()
    }

    override fun topUpAccount(login: String, amount: Long) {
        if (!balances.containsKey(login)) {
            throw BadRequestException("User does not exist")
        }

        balances[login] = balances[login]!! + amount
     }

    override suspend fun getStocks(login: String): Map<String, Pair<Long, Long>> {
        if (!stocks.containsKey(login)) {
            throw BadRequestException("User does not exist")
        }

        return stocks[login]!!.mapValues {
            it.value to stocksService.getStockPrice(it.key)
        }
    }

    override suspend fun getTotal(login: String): Long {
        // getStocks will handle user validation
        val stocksSum = getStocks(login).values.sumOf { it.first * it.second }
        return stocksSum + balances[login]!!
    }

    override suspend fun buy(login: String, ticker: String, count: Long) {
        if (!balances.containsKey(login)) {
            throw BadRequestException("User does not exist")
        }

        val stockInfo = stocksService.getStockInfo(ticker)
        if (balances[login]!! < stockInfo.price * count) {
            throw BadRequestException("Not enough money")
        }

        if (stockInfo.count < count) {
            throw BadRequestException("Not enough stocks available")
        }

        if (!stocksService.buyStocks(ticker, count)) {
            throw BadRequestException("Unable to buy stocks. Try again later")
        }

        balances[login] = balances[login]!! - stockInfo.price * count
        stocks[login]?.put(ticker, (stocks[login]?.get(ticker) ?: 0) + count)
    }

    override suspend fun sell(login: String, ticker: String, count: Long) {
        if (!stocks.containsKey(login)) {
            throw BadRequestException("User does not exist")
        }

        if (!stocks[login]!!.containsKey(ticker)) {
            throw BadRequestException("User does not have $ticker stocks")
        }

        if (stocks[login]!![ticker]!! < count) {
            throw BadRequestException("User does not have enough stocks to sell")
        }

        if (!stocksService.sellStocks(ticker, count)) {
            throw BadRequestException("Unable to sell stocks. Try again later")
        }

        val stockInfo = stocksService.getStockInfo(ticker)

        balances[login] = balances[login]!! + stockInfo.price * count
        stocks[login]?.put(ticker, stocks[login]!![ticker]!! - count)
    }
}