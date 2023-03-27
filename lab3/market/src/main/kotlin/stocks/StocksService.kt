package stocks

interface StocksService {
    fun addStock(ticker: String, price: Long, count: Long)
    fun getStock(ticker: String): StockInfo?
    fun buyStock(ticker: String, count: Long)
    fun sellStock(ticker: String, count: Long)
    fun changePrice(ticker: String, newPrice: Long)
}