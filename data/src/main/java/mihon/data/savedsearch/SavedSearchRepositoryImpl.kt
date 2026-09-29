package mihon.data.savedsearch

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import mihon.domain.savedsearch.model.SavedSearch
import mihon.domain.savedsearch.repository.SavedSearchRepository
import tachiyomi.data.Database
import tachiyomi.data.subscribeToList

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SavedSearchRepositoryImpl(
    private val database: Database,
) : SavedSearchRepository {

    override fun subscribeBySourceId(sourceId: Long): Flow<List<SavedSearch>> {
        return database.saved_searchQueries.getBySourceId(sourceId, ::savedSearchMapper).subscribeToList()
    }

    override suspend fun getDefault(sourceId: Long): SavedSearch? {
        return database.saved_searchQueries.getDefault(sourceId, ::savedSearchMapper).awaitAsOneOrNull()
    }

    override suspend fun getAll(): List<SavedSearch> {
        return database.saved_searchQueries.getAll(::savedSearchMapper).awaitAsList()
    }

    override suspend fun upsert(sourceId: Long, name: String, query: String?, filters: String) {
        database.saved_searchQueries.upsert(
            sourceId = sourceId,
            name = name,
            query = query,
            filters = filters,
        )
    }

    override suspend fun delete(id: Long) {
        database.saved_searchQueries.delete(id)
    }

    override suspend fun setDefault(sourceId: Long, name: String?) {
        database.transaction {
            database.saved_searchQueries.clearDefault(sourceId)
            if (name != null) {
                database.saved_searchQueries.setDefault(sourceId = sourceId, name = name)
            }
        }
    }

    private fun savedSearchMapper(
        id: Long,
        sourceId: Long,
        name: String,
        query: String?,
        filters: String,
        isDefault: Boolean,
    ): SavedSearch = SavedSearch(
        id = id,
        sourceId = sourceId,
        name = name,
        query = query,
        filters = filters,
        isDefault = isDefault,
    )
}
