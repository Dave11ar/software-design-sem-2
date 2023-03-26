package model

import java.util.Locale
import java.lang.IllegalArgumentException

enum class Currency {
    RUB, USB, EUR;

    companion object {
        fun fromString(s: String): Currency {
            val normalizerName = s.uppercase(Locale.getDefault())
            val availableValues = values().map { it.name }
            if (availableValues.contains(normalizerName)) {
                return valueOf(normalizerName)
            } else {
                throw IllegalArgumentException("Unknown currency: $s")
            }
        }
    }
}
