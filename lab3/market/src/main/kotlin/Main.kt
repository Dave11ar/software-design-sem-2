import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import stocks.StocksService
import stocks.StocksServiceImpl

object ApiObjects {
    data class AddStocksRequest(val ticker: String, val price: Long, val count: Long)
    data class BuyStocksRequest(val count: Long)
    data class SellStocksRequest(val count: Long)
    data class ChangePriceRequest(val newPrice: Long)
}

fun Application.module(stockService: StocksService) {
    routing {
        install(ContentNegotiation) {
            json()
        }
        post("/stocks") {
            // call.receive() didnt work with ContentNegotiation for whatever reason
            val req: ApiObjects.AddStocksRequest = jacksonObjectMapper().readValue(call.receive<String>(), ApiObjects.AddStocksRequest::class.java)

            stockService.addStock(req.ticker, req.price, req.count)
            call.respond(HttpStatusCode.OK)
        }
        get("/stock/{ticker}") {
            val ticker = call.parameters["ticker"] ?: throw BadRequestException("Ticker not provided")

            val stockInfo = stockService.getStock(ticker)
            if (stockInfo == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(HttpStatusCode.OK, jacksonObjectMapper().writeValueAsString(stockInfo))
            }
        }
        put("/stock/{ticker}") {
            // Buy
            val ticker = call.parameters["ticker"] ?: throw BadRequestException("Ticker not provided")
            val req = jacksonObjectMapper().readValue(call.receive<String>(), ApiObjects.BuyStocksRequest::class.java)

            stockService.buyStock(ticker, req.count)
            call.respond(HttpStatusCode.OK)
        }
        delete("/stock/{ticker}") {
            // Sell
            val ticker = call.parameters["ticker"] ?: throw BadRequestException("Ticker not provided")
            val req = jacksonObjectMapper().readValue(call.receive<String>(), ApiObjects.SellStocksRequest::class.java)

            stockService.sellStock(ticker, req.count)
            call.respond(HttpStatusCode.OK)
        }
        post("/stock/{ticker}") {
            // Change price
            val ticker = call.parameters["ticker"] ?: throw BadRequestException("Ticker not provided")
            val req = jacksonObjectMapper().readValue(call.receive<String>(), ApiObjects.ChangePriceRequest::class.java)

            stockService.changePrice(ticker, req.newPrice)
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun main() {
    val stockService = StocksServiceImpl()

    embeddedServer(
        Netty,
        port = 8080,
        module = {
            module(stockService)
        }).start(wait = true)
}