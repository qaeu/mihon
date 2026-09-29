package mihon.domain.savedsearch.model

/**
 * A named search query and filter state for one source.
 *
 * [filters] holds the filter state serialized as JSON; only the source it was saved from can make sense of it.
 */
data class SavedSearch(
    val id: Long,
    val sourceId: Long,
    val name: String,
    val query: String?,
    val filters: String,
    val isDefault: Boolean,
)
