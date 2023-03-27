package stocks

import io.ktor.server.plugins.*

class StocksServiceImpl: StocksService {
    private val stocks: MutableMap<String, StockInfo> = mutableMapOf()

    override fun addStock(ticker: String, price: Long, count: Long) {
        if (stocks.containsKey(ticker)) {
            throw BadRequestException("Stock with ticker $ticker already exists")
        }

        stocks[ticker] = StockInfo(price, count)
    }

    override fun getStock(ticker: String): StockInfo? =
        stocks[ticker]


    override fun buyStock(ticker: String, count: Long) {
        if (!stocks.containsKey(ticker)) {
            throw BadRequestException("Stock with ticker $ticker not found")
        }

        if (stocks[ticker]!!.count < count) {
            throw BadRequestException("Not enough stock for $ticker")
        }

        stocks[ticker] = stocks[ticker]!!.let {
            it.copy(count = it.count - count)
        }
    }

    override fun sellStock(ticker: String, count: Long) {
        if (!stocks.containsKey(ticker)) {
            throw BadRequestException("Stock with ticker $ticker not found")
        }
        stocks[ticker] = stocks[ticker]!!.let {
            it.copy(count = it.count + count)
        }
    }

    override fun changePrice(ticker: String, newPrice: Long) {
        if (!stocks.containsKey(ticker)) {
            throw BadRequestException("Stock $ticker not found")
        }

        stocks[ticker] = stocks[ticker]!!.copy(price = newPrice)
    }
}