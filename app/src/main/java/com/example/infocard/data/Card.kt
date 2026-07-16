package com.example.infocard.data

import java.util.UUID

data class Card(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var number: String, // raw 13-16 digit number
    var expiry: String, // MM/YY
    var cvv: String, // 3 or 4 digits
    var theme: CardTheme,
    var customColorHex: String? = null, // Custom user color selection
    var holderName: String? = null, // Optional cardholder name
    var provider: CardProvider = CardProvider.UNKNOWN
)
