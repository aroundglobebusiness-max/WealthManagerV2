package com.soorya.wealthmanager.data.remote

import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.soorya.wealthmanager.domain.model.Transaction
import retrofit2.Response
import retrofit2.http.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

interface NotionApi {
    @GET("databases/{id}")
    suspend fun testDb(@Path("id") id: String): Response<JsonObject>

    @POST("pages")
    suspend fun createPage(@Body body: JsonObject): Response<JsonObject>

    companion object {
        const val BASE = "https://api.notion.com/v1/"
        const val VERSION = "2022-06-28"
    }
}

@Singleton
class NotionService @Inject constructor(private val api: NotionApi) {

    suspend fun testConnection(dbId: String): Boolean = try {
        api.testDb(dbId).isSuccessful
    } catch (e: Exception) { false }

    suspend fun sync(txn: Transaction, dbId: String): String? = try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(txn.date))
        val body = JsonObject().apply {
            add("parent", JsonObject().apply { addProperty("database_id", dbId) })
            add("properties", JsonObject().apply {
                add("Name", JsonObject().apply {
                    add("title", JsonArray().apply {
                        add(JsonObject().apply { add("text", JsonObject().apply { addProperty("content", txn.title) }) })
                    })
                })
                add("Amount", JsonObject().apply { addProperty("number", txn.amount) })
                add("Type", JsonObject().apply { add("select", JsonObject().apply { addProperty("name", txn.type.name) }) })
                add("Category", JsonObject().apply { add("select", JsonObject().apply { addProperty("name", txn.category) }) })
                add("Currency", JsonObject().apply {
                    add("rich_text", JsonArray().apply {
                        add(JsonObject().apply { add("text", JsonObject().apply { addProperty("content", txn.currency) }) })
                    })
                })
                add("Date", JsonObject().apply { add("date", JsonObject().apply { addProperty("start", date) }) })
            })
        }
        val res = api.createPage(body)
        if (res.isSuccessful) res.body()?.get("id")?.asString else null
    } catch (e: Exception) { Log.e("Notion", "Sync error", e); null }
}
