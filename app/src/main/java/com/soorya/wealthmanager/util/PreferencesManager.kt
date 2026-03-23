package com.soorya.wealthmanager.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore("wealth_prefs")

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext private val ctx: Context) {
    private object K {
        val TOKEN = stringPreferencesKey("notion_token")
        val DB_ID = stringPreferencesKey("notion_db")
        val CURRENCY = stringPreferencesKey("currency")
        val SYMBOL = stringPreferencesKey("symbol")
    }

    val token: Flow<String> = ctx.dataStore.data.map { it[K.TOKEN] ?: "" }
    val dbId: Flow<String> = ctx.dataStore.data.map { it[K.DB_ID] ?: "" }
    val currency: Flow<String> = ctx.dataStore.data.map { it[K.CURRENCY] ?: "INR" }
    val symbol: Flow<String> = ctx.dataStore.data.map { it[K.SYMBOL] ?: "₹" }

    suspend fun saveNotion(token: String, dbId: String) {
        ctx.dataStore.edit { it[K.TOKEN] = token; it[K.DB_ID] = dbId }
    }

    suspend fun saveCurrency(code: String, sym: String) {
        ctx.dataStore.edit { it[K.CURRENCY] = code; it[K.SYMBOL] = sym }
    }
}
