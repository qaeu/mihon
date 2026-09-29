package eu.kanade.tachiyomi.data.backup.restore.restorers

import dev.zacsweers.metro.Inject
import eu.kanade.tachiyomi.data.backup.models.BackupSavedSearch
import mihon.domain.savedsearch.repository.SavedSearchRepository

@Inject
class SavedSearchRestorer(
    private val savedSearchRepository: SavedSearchRepository,
) {

    suspend operator fun invoke(
        backupSavedSearch: BackupSavedSearch,
    ) {
        savedSearchRepository.upsert(
            sourceId = backupSavedSearch.sourceId,
            name = backupSavedSearch.name,
            query = backupSavedSearch.query,
            filters = backupSavedSearch.filters,
        )

        // A default chosen on this device is kept over the one in the backup
        if (backupSavedSearch.isDefault && savedSearchRepository.getDefault(backupSavedSearch.sourceId) == null) {
            savedSearchRepository.setDefault(backupSavedSearch.sourceId, backupSavedSearch.name)
        }
    }
}
