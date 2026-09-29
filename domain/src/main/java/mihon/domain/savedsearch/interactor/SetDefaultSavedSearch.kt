package mihon.domain.savedsearch.interactor

import dev.zacsweers.metro.Inject
import mihon.domain.savedsearch.repository.SavedSearchRepository

@Inject
class SetDefaultSavedSearch(
    private val repository: SavedSearchRepository,
) {
    /**
     * Makes the search named [name] the default for [sourceId], or clears the default if [name] is null.
     */
    suspend operator fun invoke(sourceId: Long, name: String?) {
        repository.setDefault(sourceId, name)
    }
}
