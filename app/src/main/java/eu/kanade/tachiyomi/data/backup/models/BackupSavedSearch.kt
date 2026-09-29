package eu.kanade.tachiyomi.data.backup.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import mihon.domain.savedsearch.model.SavedSearch

@Serializable
class BackupSavedSearch(
    @ProtoNumber(1) var sourceId: Long,
    @ProtoNumber(2) var name: String,
    @ProtoNumber(3) var query: String? = null,
    @ProtoNumber(4) var filters: String,
    @ProtoNumber(5) var isDefault: Boolean = false,
)

val backupSavedSearchMapper = { savedSearch: SavedSearch ->
    BackupSavedSearch(
        sourceId = savedSearch.sourceId,
        name = savedSearch.name,
        query = savedSearch.query,
        filters = savedSearch.filters,
        isDefault = savedSearch.isDefault,
    )
}
