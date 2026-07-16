package com.example.infocard.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CardStore(context: Context) {
    private val storageKey = "wallet_cards_storage"
    private val gson = Gson()
    private val sharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "wallet_cards_storage_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback to standard SharedPreferences in case of keystore failure (e.g. older devices/testing)
        context.getSharedPreferences("wallet_cards_storage_prefs_backup", Context.MODE_PRIVATE)
    }

    private val _cards = MutableStateFlow<List<Card>>(emptyList())
    val cards: StateFlow<List<Card>> = _cards.asStateFlow()

    init {
        loadCards()
    }

    fun addCard(card: Card) {
        val updated = _cards.value.toMutableList().apply { add(card) }
        updateCardsList(updated)
    }

    fun deleteCard(card: Card) {
        val updated = _cards.value.toMutableList().apply {
            removeAll { it.id == card.id }
        }
        updateCardsList(updated)
    }

    fun updateCard(updatedCard: Card) {
        val updated = _cards.value.toMutableList().apply {
            val idx = indexOfFirst { it.id == updatedCard.id }
            if (idx != -1) {
                set(idx, updatedCard)
            }
        }
        updateCardsList(updated)
    }

    fun moveCard(fromIndex: Int, toIndex: Int) {
        val list = _cards.value.toMutableList()
        if (fromIndex in list.indices && toIndex in 0..list.size) {
            val item = list.removeAt(fromIndex)
            val newTo = if (toIndex > fromIndex) toIndex - 1 else toIndex
            list.add(newTo.coerceIn(0, list.size), item)
            updateCardsList(list)
        }
    }

    private fun updateCardsList(newList: List<Card>) {
        _cards.value = newList
        saveCards(newList)
    }

    private fun saveCards(list: List<Card>) {
        try {
            val json = gson.toJson(list)
            sharedPreferences.edit().putString(storageKey, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadCards() {
        try {
            val json = sharedPreferences.getString(storageKey, null)
            if (json != null) {
                val type = object : TypeToken<List<Card>>() {}.type
                val loadedList: List<Card> = gson.fromJson(json, type)
                _cards.value = loadedList
            } else {
                // Initialize default mock cards if empty, matching iOS implementation
                val initialCards = listOf(
                    Card(
                        name = "Bonus Gold",
                        number = "4355289948216503",
                        expiry = "12/28",
                        cvv = "324",
                        theme = CardTheme.PURPLE_PINK,
                        customColorHex = null,
                        holderName = "JOHN DOE",
                        provider = CardProvider.VISA
                    ),
                    Card(
                        name = "Maximum Platinum",
                        number = "5412750012984531",
                        expiry = "09/29",
                        cvv = "889",
                        theme = CardTheme.BLUE_TEAL,
                        customColorHex = null,
                        holderName = "JANE DOE",
                        provider = CardProvider.MASTERCARD
                    )
                )
                _cards.value = initialCards
                saveCards(initialCards)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
