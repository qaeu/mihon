package mihon.domain.savedsearch.repository

import kotlinx.coroutines.flow.Flow
import mihon.domain.savedsearch.model.SavedSearch

interface SavedSearchRepository {
    fun subscribeBySourceId(sourceId: Long): Flow<List<SavedSearch>>

    suspend fun getDefault(sourceId: Long): SavedSearch?

    suspend fun getAll(): List<SavedSearch>

    /**
     * Saves a search, replacing the query and filters of an existing one with the same name for the source.
     */
    suspend fun upsert(sourceId: Long, name: String, query: String?, filters: String)

    suspend fun delete(id: Long)

    /**
     * Makes the search named [name] the default for the source, or clears the default if [name] is null.
     */
    suspend fun setDefault(sourceId: Long, name: String?)
}
