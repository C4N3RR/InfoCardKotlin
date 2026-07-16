package com.example.infocard.data

import com.example.infocard.util.AppLanguage
import com.example.infocard.util.Loc

enum class CardProvider(val value: String) {
    VISA("visa"),
    MASTERCARD("mastercard"),
    TROY("troy"),
    UNKNOWN("unknown");

    fun displayName(language: AppLanguage): String {
        return when (this) {
            VISA -> "Visa"
            MASTERCARD -> "Mastercard"
            TROY -> "Troy"
            UNKNOWN -> Loc.UNKNOWN_PROVIDER.get(language)
        }
    }

    companion object {
        fun detect(cardNumber: String): CardProvider {
            val digits = cardNumber.filter { it.isDigit() }
            return when {
                digits.startsWith("4") -> VISA
                digits.startsWith("51") || digits.startsWith("52") || digits.startsWith("53") ||
                        digits.startsWith("54") || digits.startsWith("55") ||
                        digits.startsWith("222") || digits.startsWith("223") ||
                        digits.startsWith("224") || digits.startsWith("225") ||
                        digits.startsWith("226") || digits.startsWith("227") ||
                        digits.startsWith("23") || digits.startsWith("24") ||
                        digits.startsWith("25") || digits.startsWith("26") ||
                        digits.startsWith("27") -> MASTERCARD
                digits.startsWith("9792") -> TROY
                else -> UNKNOWN
            }
        }
    }
}
