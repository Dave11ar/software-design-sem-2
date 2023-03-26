package currency

import config.ConverterConfig
import http.HttpClient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.parse
import model.Currency
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class ConverterProvider private constructor(initialConverter: Converter) {
    private val converter = AtomicReference(initialConverter)
    private val scheduler = Executors.newScheduledThreadPool(1)

    constructor(config: ConverterConfig, client: HttpClient) : this(getConverter(config, client)) {
        val updateTask = Runnable {
            try {
                val newConverter = getConverter(config, client)
                val oldConverter = converter.get()
                converter.compareAndSet(oldConverter, newConverter)
            } catch (e: Exception) {
                System.err.println("Failed to update rates")
            }
        }
        scheduler.scheduleAtFixedRate(updateTask, 2, 2, TimeUnit.HOURS)
    }

    fun get(): Converter = converter.get()

    companion object {
        private val parser = Json { allowStructuredMapKeys = true }

        private fun getConverter(config: ConverterConfig, client: HttpClient): Converter {
            val symbols = Currency.values().joinToString(separator = ",") { it.toString() }
            val url = "${config.schema}://${config.host}/${config.path}?base=USD&symbols=${symbols}"
            val strResponse = client.use { it.getResponse(url) }
            val typedResponse = parser.decodeFromString(CurrencyRatesResponse.serializer(), strResponse);
            val rates = HashMap<Pair<Currency, Currency>, Double>()
            for (from in Currency.values()) {
                for (to in Currency.values()) {
                    if (from == to) {
                        rates[Pair(from, to)] = 1.0
                    } else {
                        rates[Pair(from, to)] =
                            typedResponse.rates[to]!! / typedResponse.rates[from]!!
                    }
                }
            }
            return Converter(rates)
        }
    }
}
