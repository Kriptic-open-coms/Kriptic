package com.kriptic.app.knowledge

import android.content.Context
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

/**
 * Loads content/legal/know_your_rights.json and content/firstaid/first_aid.json
 * (bundled at app/src/main/assets/knowledge/ at build time — see
 * docs/02_PROJECT_STRUCTURE.md for why the source lives in content/ at the
 * repo root rather than directly under app/) into the local FTS4 table on
 * first run, then serves everything from Room from then on. Zero network
 * calls, per docs/00_BRIEF.md success criterion #6.
 */
class KnowledgeRepository(private val context: Context) {

    private val dao = KnowledgeDatabase.getInstance(context).knowledgeDao()

    suspend fun ensureLoaded() {
        if (dao.count() > 0) return
        val entries = mutableListOf<KnowledgeEntry>()
        var rowid = 1
        for ((assetFile, domain) in listOf(
            "knowledge/know_your_rights.json" to "legal",
            "knowledge/first_aid.json" to "firstaid",
        )) {
            val json = context.assets.open(assetFile).bufferedReader().use { it.readText() }
            val root = JSONObject(json)
            val arr = root.getJSONArray("entries")
            for (i in 0 until arr.length()) {
                entries.add(KnowledgeEntry.fromJson(rowid++, arr.getJSONObject(i), domain))
            }
        }
        dao.insertAll(entries)
    }

    /**
     * Empty query returns everything (optionally filtered by [domainFilter]).
     * LIMITATION: domain filtering is only applied when [query] is blank —
     * combining a text search with a domain/category filter simultaneously
     * isn't wired yet (would need a second FTS query variant with an extra
     * WHERE domain = ? clause). Fine for v1's two-domain dataset; flag if
     * more domains get added later.
     */
    fun search(query: String, domainFilter: String? = null): Flow<List<KnowledgeEntry>> {
        return if (query.isBlank()) {
            dao.observeAll(domainFilter ?: "")
        } else {
            // FTS4 MATCH syntax: wrap terms with trailing '*' for prefix matching
            // so partial words (typed mid-query) still return results.
            val ftsQuery = query.trim().split(Regex("\\s+")).joinToString(" ") { "$it*" }
            dao.search(ftsQuery)
        }
    }

    suspend fun getByEntryId(entryId: String): KnowledgeEntry? = dao.getByEntryId(entryId)
}
