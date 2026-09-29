package eu.kanade.tachiyomi.ui.browse.source.browse

import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class FilterSerializerTest {

    private class TestCheckBox(name: String) : Filter.CheckBox(name)
    private class TestTriState(name: String) : Filter.TriState(name)
    private class TestText(name: String) : Filter.Text(name)
    private class TestSelect(name: String, values: Array<String>) : Filter.Select<String>(name, values)
    private class TestSort(name: String, values: Array<String>) : Filter.Sort(name, values)
    private class TestGroup(name: String, state: List<Filter<*>>) : Filter.Group<Filter<*>>(name, state)

    private fun sourceFilters() = FilterList(
        Filter.Header("Note"),
        TestText("Author"),
        Filter.Separator(),
        TestSelect("Status", arrayOf("Any", "Ongoing", "Completed")),
        TestSort("Sort", arrayOf("Popular", "Latest", "Title")),
        TestCheckBox("Adult"),
        TestGroup(
            "Genres",
            listOf(TestTriState("Action"), TestTriState("Comedy"), TestTriState("Drama")),
        ),
    )

    private fun FilterList.byName(name: String): Filter<*> = flatMap {
        if (it is Filter.Group<*>) it.state.filterIsInstance<Filter<*>>() + it else listOf(it)
    }.first { it.name == name }

    private fun customised() = sourceFilters().apply {
        (byName("Author") as Filter.Text).state = "Someone"
        (byName("Status") as Filter.Select<*>).state = 2
        (byName("Sort") as Filter.Sort).state = Filter.Sort.Selection(index = 1, ascending = false)
        (byName("Adult") as Filter.CheckBox).state = true
        (byName("Comedy") as Filter.TriState).state = Filter.TriState.STATE_INCLUDE
        (byName("Drama") as Filter.TriState).state = Filter.TriState.STATE_EXCLUDE
    }

    @Test
    fun `round trip restores every filter type`() {
        val saved = FilterSerializer.serialize(customised(), sourceFilters())
        val restored = sourceFilters()

        val missed = FilterSerializer.deserializeInto(saved, restored)

        assertEquals(0, missed)
        // FilterList never equals anything, so the underlying lists are compared
        assertEquals(customised().list, restored.list)
    }

    @Test
    fun `only filters changed from the defaults are stored`() {
        assertEquals("[]", FilterSerializer.serialize(sourceFilters(), sourceFilters()))
    }

    @Test
    fun `filters are found by name after the source reorders them`() {
        val saved = FilterSerializer.serialize(customised(), sourceFilters())
        val reordered = FilterList(
            TestGroup(
                "Genres",
                listOf(TestTriState("Drama"), TestTriState("Action"), TestTriState("Comedy")),
            ),
            TestCheckBox("Adult"),
            TestSort("Sort", arrayOf("Popular", "Latest", "Title")),
            TestSelect("Status", arrayOf("Any", "Ongoing", "Completed")),
            TestText("Author"),
        )

        val missed = FilterSerializer.deserializeInto(saved, reordered)

        assertEquals(0, missed)
        assertEquals("Someone", reordered.byName("Author").state)
        assertEquals(2, reordered.byName("Status").state)
        assertEquals(true, reordered.byName("Adult").state)
        assertEquals(Filter.TriState.STATE_INCLUDE, reordered.byName("Comedy").state)
        assertEquals(Filter.TriState.STATE_EXCLUDE, reordered.byName("Drama").state)
    }

    @Test
    fun `state that no longer fits is skipped and counted`() {
        val saved = FilterSerializer.serialize(customised(), sourceFilters())
        val changed = FilterList(
            TestText("Writer"),
            TestSelect("Status", arrayOf("Any", "Ongoing")),
            TestSort("Sort", arrayOf("Popular")),
            TestCheckBox("Adult"),
            TestGroup("Genres", listOf(TestTriState("Action"), TestTriState("Comedy"))),
        )

        val missed = FilterSerializer.deserializeInto(saved, changed)

        // Author was renamed, Status and Sort lost the selected option, and Drama is gone
        assertEquals(4, missed)
        assertEquals("", changed.byName("Writer").state)
        assertEquals(0, changed.byName("Status").state)
        assertNull(changed.byName("Sort").state)
        assertEquals(true, changed.byName("Adult").state)
        assertEquals(Filter.TriState.STATE_INCLUDE, changed.byName("Comedy").state)
    }

    @Test
    fun `malformed data leaves the filters untouched`() {
        val filters = sourceFilters()

        val missed = FilterSerializer.deserializeInto("not json", filters)

        assertEquals(1, missed)
        assertEquals(sourceFilters().list, filters.list)
    }
}
