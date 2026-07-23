package com.kriptic.app.knowledge

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import org.json.JSONObject

/**
 * Matches the schema already used in content/legal/know_your_rights.json
 * and content/firstaid/first_aid.json — id, title, category, body,
 * source, last_reviewed. Both files are currently placeholder content
 * pending a real medical/legal review (see each file's "_readme" field
 * and CONTRIBUTING.md) — this module reads whatever is actually there,
 * placeholder or reviewed, without special-casing either.
 *
 * Implemented as a single Room FTS4 virtual table (not a separate
 * plain-table + external-content-FTS pair) to keep the Room schema simple
 * and avoid the external-content-table rowid-sync edge cases, since this
 * dataset is small (tens to low hundreds of rows) and read-only at
 * runtime — the simplicity is worth more than the marginal storage cost.
 * Per Room's FTS4 requirement, the primary key must be an Int column
 * named "rowid".
 */
@Fts4
@Entity(tableName = "knowledge_entries")
data class KnowledgeEntry(
    @PrimaryKey @ColumnInfo(name = "rowid") val rowid: Int,
    val entryId: String,
    val title: String,
    val category: String,
    val body: String,
    val source: String,
    val lastReviewed: String?,
    /** Which bundled file this came from — "firstaid" or "legal" — used for the tag chips. */
    val domain: String,
) {
    companion object {
        fun fromJson(rowid: Int, o: JSONObject, domain: String): KnowledgeEntry = KnowledgeEntry(
            rowid = rowid,
            entryId = o.getString("id"),
            title = o.getString("title"),
            category = o.optString("category", "Information"),
            body = o.getString("body"),
            source = o.optString("source", ""),
            lastReviewed = if (o.isNull("last_reviewed")) null else o.optString("last_reviewed", null),
            domain = domain,
        )
    }
}
