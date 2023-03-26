package currency

import model.Currency
import kotlinx.serialization.Serializable

@Serializable
data class CurrencyRatesResponse(val rates: Map<Currency, Double>, val base: String, val date: String)