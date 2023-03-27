package stocks

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ApiObjects {
    data class StockInfoResponse(
        val price: Long,
        val count: Long
    )
    data class BuyStocksRequest(
        val count: Long
    )
    data class SellStocksRequest(
        val count: Long
    )
}

class HttpStocksServiceImpl(private val httpClient: HttpClient, private val host: String): StocksService {
    override suspend fun getStockPrice(ticker: String): Long {
        return getStockInfo(ticker).price
    }

    override suspend fun getStockInfo(ticker: String): ApiObjects.StockInfoResponse {
        return jacksonObjectMapper().readValue(
            httpClient.get("$host/stock/$ticker").body<String>(),
            ApiObjects.StockInfoResponse::class.java
        )
    }

    override suspend fun buyStocks(ticker: String, count: Long): Boolean {
        val response = httpClient.put("$host/stock/$ticker") {
            contentType(ContentType.Application.Json.withParameter("charset", "utf-8"))
            setBody(jacksonObjectMapper().writeValueAsString(ApiObjects.BuyStocksRequest(count)))
        }

        return response.status == HttpStatusCode.OK
    }

    override suspend fun sellStocks(ticker: String, count: Long): Boolean {
        val response = httpClient.delete("$host/stock/$ticker") {
            contentType(ContentType.Application.Json.withParameter("charset", "utf-8"))
            setBody(jacksonObjectMapper().writeValueAsString(ApiObjects.SellStocksRequest(count)))
        }

        return response.status == HttpStatusCode.OK
    }
}