package com.kriptic.app.reference

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ReferenceArticle(
    val id: String,
    val category: String, // "LEGAL" or "FIRST_AID"
    val title: String,
    val summary: String,
    val content: String
)

class ReferenceRepository(private val context: Context) {

    private val articles = mutableListOf<ReferenceArticle>()

    init {
        loadAssetData()
    }

    private fun loadAssetData() {
        try {
            val legalJson = context.assets.open("legal/know_your_rights.json").bufferedReader().use { it.readText() }
            val firstAidJson = context.assets.open("firstaid/first_aid.json").bufferedReader().use { it.readText() }

            val legalList = Json.decodeFromString<List<ReferenceArticle>>(legalJson)
            val firstAidList = Json.decodeFromString<List<ReferenceArticle>>(firstAidJson)

            articles.clear()
            articles.addAll(legalList)
            articles.addAll(firstAidList)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun search(query: String, categoryFilter: String? = null): List<ReferenceArticle> {
        val q = query.trim().lowercase()
        return articles.filter { article ->
            val matchesCategory = categoryFilter == null || article.category.equals(categoryFilter, ignoreCase = true)
            val matchesQuery = q.isEmpty() ||
                    article.title.lowercase().contains(q) ||
                    article.summary.lowercase().contains(q) ||
                    article.content.lowercase().contains(q)

            matchesCategory && matchesQuery
        }
    }

    fun getArticleById(id: String): ReferenceArticle? {
        return articles.find { it.id == id }
    }
}
