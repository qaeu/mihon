package mihon.domain.savedsearch.interactor

import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import mihon.domain.savedsearch.model.SavedSearch
import mihon.domain.savedsearch.repository.SavedSearchRepository

@Inject
class GetSavedSearches(
    private val repository: SavedSearchRepository,
) {
    fun subscribe(sourceId: Long): Flow<List<SavedSearch>> = repository.subscribeBySourceId(sourceId)

    suspend fun awaitDefault(sourceId: Long): SavedSearch? = repository.getDefault(sourceId)

    suspend fun awaitAll(): List<SavedSearch> = repository.getAll()
}
