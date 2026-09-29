package mihon.domain.savedsearch.interactor

import dev.zacsweers.metro.Inject
import mihon.domain.savedsearch.model.SavedSearch
import mihon.domain.savedsearch.repository.SavedSearchRepository

@Inject
class DeleteSavedSearch(
    private val repository: SavedSearchRepository,
) {
    suspend operator fun invoke(savedSearch: SavedSearch) {
        repository.delete(savedSearch.id)
    }
}
