package mihon.domain.savedsearch.interactor

import dev.zacsweers.metro.Inject
import mihon.domain.savedsearch.repository.SavedSearchRepository

@Inject
class SaveSearch(
    private val repository: SavedSearchRepository,
) {
    suspend operator fun invoke(sourceId: Long, name: String, query: String?, filters: String) {
        repository.upsert(
            sourceId = sourceId,
            name = name.trim(),
            query = query?.takeIf { it.isNotBlank() },
            filters = filters,
        )
    }
}
