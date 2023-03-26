package currency

import model.Currency

class Converter(private val rates: Map<Pair<Currency, Currency>, Double>) {
    fun convert(amount: Double, from: Currency, to: Currency)
        = amount * rates[Pair(from, to)]!!
}