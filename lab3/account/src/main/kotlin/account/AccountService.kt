package account

interface AccountService {
    fun addUser(login: String)
    fun topUpAccount(login: String, amount: Long)
    suspend fun getStocks(login: String): Map<String, Pair<Long, Long>>
    suspend fun getTotal(login: String): Long
    suspend fun buy(login: String, ticker: String, count: Long)
    suspend fun sell(login: String, ticker: String, count: Long)
}