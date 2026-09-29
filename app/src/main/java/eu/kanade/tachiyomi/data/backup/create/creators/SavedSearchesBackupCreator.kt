package eu.kanade.tachiyomi.data.backup.create.creators

import dev.zacsweers.metro.Inject
import eu.kanade.tachiyomi.data.backup.models.BackupSavedSearch
import eu.kanade.tachiyomi.data.backup.models.backupSavedSearchMapper
import mihon.domain.savedsearch.interactor.GetSavedSearches

@Inject
class SavedSearchesBackupCreator(
    private val getSavedSearches: GetSavedSearches,
) {

    suspend operator fun invoke(): List<BackupSavedSearch> {
        return getSavedSearches.awaitAll()
            .map(backupSavedSearchMapper)
    }
}
