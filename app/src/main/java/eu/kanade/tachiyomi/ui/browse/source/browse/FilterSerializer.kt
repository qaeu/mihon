package eu.kanade.tachiyomi.ui.browse.source.browse

import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Saves and restores the state of a source's filters, for saved searches.
 *
 * Filters are defined by the source, so only their state is stored: each filter that differs from the source's
 * defaults is recorded with its position, type and name. Restoring applies that state onto a fresh
 * [FilterList] from the same source. A filter is found by its position when its type and name still match
 * there, and otherwise by type and name anywhere at the same level, so a saved search keeps working when a
 * source update reorders its filters. State that no longer fits any filter is skipped and counted.
 */
object FilterSerializer {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Serializes the state of [filters] that differs from [defaults], which should be a fresh
     * `source.getFilterList()` from the same source.
     */
    fun serialize(filters: FilterList, defaults: FilterList): String {
        return json.encodeToString(changedStates(filters, defaults))
    }

    /**
     * Applies [serialized] state onto [filters] in place.
     *
     * @return the number of saved filter states that could not be applied.
     */
    fun deserializeInto(serialized: String, filters: FilterList): Int {
        val states = try {
            json.decodeFromString<List<SavedFilter>>(serialized)
        } catch (_: Exception) {
            return 1
        }
        return applyStates(states, filters)
    }

    private fun changedStates(filters: List<Any?>, defaults: List<Any?>): List<SavedFilter> {
        return filters.mapIndexedNotNull { index, filter ->
            if (filter !is Filter<*>) return@mapIndexedNotNull null
            val default = (defaults.getOrNull(index) as? Filter<*>)
                ?.takeIf { it::class == filter::class && it.name == filter.name }
            when (filter) {
                is Filter.Group<*> -> {
                    val children = changedStates(filter.state, (default as? Filter.Group<*>)?.state.orEmpty())
                    if (children.isEmpty()) null else SavedFilter.Group(index, filter.name, children)
                }
                else -> if (default != null && default.state == filter.state) null else toSaved(index, filter)
            }
        }
    }

    private fun toSaved(index: Int, filter: Filter<*>): SavedFilter? {
        return when (filter) {
            is Filter.CheckBox -> SavedFilter.CheckBox(index, filter.name, filter.state)
            is Filter.TriState -> SavedFilter.TriState(index, filter.name, filter.state)
            is Filter.Text -> SavedFilter.Text(index, filter.name, filter.state)
            is Filter.Select<*> -> SavedFilter.Select(index, filter.name, filter.state)
            is Filter.Sort -> SavedFilter.Sort(
                index = index,
                name = filter.name,
                selection = filter.state?.let { SavedFilter.Sort.Selection(it.index, it.ascending) },
            )
            is Filter.Header, is Filter.Separator, is Filter.Group<*> -> null
        }
    }

    private fun applyStates(states: List<SavedFilter>, filters: List<Any?>): Int {
        var missed = 0
        for (saved in states) {
            val target = (filters.getOrNull(saved.index) as? Filter<*>)?.takeIf { saved.matches(it) }
                ?: filters.firstOrNull { it is Filter<*> && saved.matches(it) } as? Filter<*>
            if (target == null) {
                missed += saved.count()
                continue
            }
            missed += apply(saved, target)
        }
        return missed
    }

    private fun apply(saved: SavedFilter, target: Filter<*>): Int {
        when (saved) {
            is SavedFilter.CheckBox -> (target as Filter.CheckBox).state = saved.state
            is SavedFilter.TriState -> {
                if (saved.state !in Filter.TriState.STATE_IGNORE..Filter.TriState.STATE_EXCLUDE) return 1
                (target as Filter.TriState).state = saved.state
            }
            is SavedFilter.Text -> (target as Filter.Text).state = saved.state
            is SavedFilter.Select -> {
                val select = target as Filter.Select<*>
                if (saved.state !in select.values.indices) return 1
                select.state = saved.state
            }
            is SavedFilter.Sort -> {
                val sort = target as Filter.Sort
                val selection = saved.selection
                if (selection != null && selection.index !in sort.values.indices) return 1
                sort.state = selection?.let { Filter.Sort.Selection(it.index, it.ascending) }
            }
            is SavedFilter.Group -> return applyStates(saved.children, (target as Filter.Group<*>).state)
        }
        return 0
    }

    private fun SavedFilter.matches(filter: Filter<*>): Boolean {
        if (filter.name != name) return false
        return when (this) {
            is SavedFilter.CheckBox -> filter is Filter.CheckBox
            is SavedFilter.TriState -> filter is Filter.TriState
            is SavedFilter.Text -> filter is Filter.Text
            is SavedFilter.Select -> filter is Filter.Select<*>
            is SavedFilter.Sort -> filter is Filter.Sort
            is SavedFilter.Group -> filter is Filter.Group<*>
        }
    }

    private fun SavedFilter.count(): Int = when (this) {
        is SavedFilter.Group -> children.sumOf { it.count() }
        else -> 1
    }

    @Serializable
    private sealed interface SavedFilter {
        val index: Int
        val name: String

        @Serializable
        @SerialName("checkbox")
        data class CheckBox(override val index: Int, override val name: String, val state: Boolean) : SavedFilter

        @Serializable
        @SerialName("tristate")
        data class TriState(override val index: Int, override val name: String, val state: Int) : SavedFilter

        @Serializable
        @SerialName("text")
        data class Text(override val index: Int, override val name: String, val state: String) : SavedFilter

        @Serializable
        @SerialName("select")
        data class Select(override val index: Int, override val name: String, val state: Int) : SavedFilter

        @Serializable
        @SerialName("sort")
        data class Sort(
            override val index: Int,
            override val name: String,
            val selection: Selection?,
        ) : SavedFilter {
            @Serializable
            data class Selection(val index: Int, val ascending: Boolean)
        }

        @Serializable
        @SerialName("group")
        data class Group(
            override val index: Int,
            override val name: String,
            val children: List<SavedFilter>,
        ) : SavedFilter
    }
}
