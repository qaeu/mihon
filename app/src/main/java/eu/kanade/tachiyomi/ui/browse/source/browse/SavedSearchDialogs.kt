package eu.kanade.tachiyomi.ui.browse.source.browse

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import mihon.domain.savedsearch.model.SavedSearch
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun SaveSearchDialog(
    onDismissRequest: () -> Unit,
    onSave: (String) -> Unit,
    savedSearchNames: List<String>,
) {
    var name by remember { mutableStateOf("") }
    val trimmedName = name.trim()

    val focusRequester = remember { FocusRequester() }
    val nameAlreadyExists = remember(trimmedName) { trimmedName in savedSearchNames }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                enabled = trimmedName.isNotEmpty(),
                onClick = {
                    onSave(trimmedName)
                    onDismissRequest()
                },
            ) {
                Text(text = stringResource(MR.strings.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(MR.strings.action_cancel))
            }
        },
        title = {
            Text(text = stringResource(MR.strings.action_save_search))
        },
        text = {
            OutlinedTextField(
                modifier = Modifier.focusRequester(focusRequester),
                value = name,
                onValueChange = { name = it },
                label = { Text(text = stringResource(MR.strings.name)) },
                supportingText = if (nameAlreadyExists) {
                    { Text(text = stringResource(MR.strings.saved_search_overwrite, trimmedName)) }
                } else {
                    null
                },
                singleLine = true,
            )
        },
    )

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }
}

@Composable
fun ManageSavedSearchDialog(
    onDismissRequest: () -> Unit,
    savedSearch: SavedSearch,
    onSetDefault: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(MR.strings.action_cancel))
            }
        },
        title = {
            Text(text = savedSearch.name)
        },
        text = {
            Column {
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onSetDefault(!savedSearch.isDefault)
                        onDismissRequest()
                    },
                ) {
                    val label = if (savedSearch.isDefault) {
                        MR.strings.action_remove_default
                    } else {
                        MR.strings.action_set_as_default
                    }
                    Text(text = stringResource(label))
                }
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onDelete()
                        onDismissRequest()
                    },
                ) {
                    Text(text = stringResource(MR.strings.action_delete))
                }
            }
        },
    )
}
