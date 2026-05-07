package com.nuvio.tv.ui.screens.addon

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogBulkToggleSupportTest {

    private fun item(
        key: String,
        addonName: String,
        disableKey: String = key,
        legacyDisableKey: String? = null,
        isDisabled: Boolean = false,
    ): CatalogOrderItem = CatalogOrderItem(
        key = key,
        disableKey = disableKey,
        legacyDisableKey = legacyDisableKey,
        catalogName = key,
        addonName = addonName,
        typeLabel = "movie",
        isDisabled = isDisabled,
        canMoveUp = false,
        canMoveDown = false,
    )

    // ----- computeDisableForAddons -----

    @Test
    fun `disable for empty addon set returns currentDisabled unchanged`() {
        val current = listOf("a", "b")
        val items = listOf(item("k1", "Cinemeta"))
        val result = computeDisableForAddons(current, items, emptySet())
        assertEquals(current, result)
    }

    @Test
    fun `disable for single addon adds only that addon's disable keys`() {
        val items = listOf(
            item("k1", "Cinemeta", disableKey = "d1"),
            item("k2", "TMDB", disableKey = "d2"),
            item("k3", "Cinemeta", disableKey = "d3"),
        )
        val result = computeDisableForAddons(emptyList(), items, setOf("Cinemeta"))
        assertEquals(listOf("d1", "d3"), result)
    }

    @Test
    fun `disable for multiple addons unions all matching disable keys`() {
        val items = listOf(
            item("k1", "Cinemeta", disableKey = "d1"),
            item("k2", "TMDB", disableKey = "d2"),
            item("k3", "Trakt", disableKey = "d3"),
        )
        val result = computeDisableForAddons(emptyList(), items, setOf("Cinemeta", "Trakt"))
        assertEquals(listOf("d1", "d3"), result)
    }

    @Test
    fun `disable preserves existing currentDisabled entries and dedupes`() {
        val current = listOf("d1", "d_legacy")
        val items = listOf(
            item("k1", "Cinemeta", disableKey = "d1"),
            item("k2", "Cinemeta", disableKey = "d2"),
        )
        val result = computeDisableForAddons(current, items, setOf("Cinemeta"))
        assertEquals(listOf("d1", "d_legacy", "d2"), result)
    }

    @Test
    fun `disable never affects collection_ keys even if addon name matches`() {
        val items = listOf(
            item("collection_42", "Cinemeta", disableKey = "collection_42"),
            item("k1", "Cinemeta", disableKey = "d1"),
        )
        val result = computeDisableForAddons(emptyList(), items, setOf("Cinemeta"))
        assertEquals(listOf("d1"), result)
    }

    @Test
    fun `disable preserves a collection_ key already in currentDisabled`() {
        val current = listOf("collection_42")
        val items = listOf(item("k1", "Cinemeta", disableKey = "d1"))
        val result = computeDisableForAddons(current, items, setOf("Cinemeta"))
        assertEquals(listOf("collection_42", "d1"), result)
    }

    @Test
    fun `disable with empty items returns currentDisabled unchanged`() {
        val current = listOf("d1")
        val result = computeDisableForAddons(current, emptyList(), setOf("Cinemeta"))
        assertEquals(current, result)
    }

    // ----- computeEnableForAddons -----

    @Test
    fun `enable for empty addon set returns currentDisabled unchanged`() {
        val current = listOf("d1", "d2")
        val items = listOf(item("k1", "Cinemeta", disableKey = "d1"))
        val result = computeEnableForAddons(current, items, emptySet())
        assertEquals(current, result)
    }

    @Test
    fun `enable removes only matching addon's disable keys from currentDisabled`() {
        val current = listOf("d1", "d2", "d3")
        val items = listOf(
            item("k1", "Cinemeta", disableKey = "d1"),
            item("k2", "TMDB", disableKey = "d2"),
            item("k3", "Cinemeta", disableKey = "d3"),
        )
        val result = computeEnableForAddons(current, items, setOf("Cinemeta"))
        assertEquals(listOf("d2"), result)
    }

    @Test
    fun `enable for multiple addons removes union of their disable keys`() {
        val current = listOf("d1", "d2", "d3", "d4")
        val items = listOf(
            item("k1", "Cinemeta", disableKey = "d1"),
            item("k2", "TMDB", disableKey = "d2"),
            item("k3", "Trakt", disableKey = "d3"),
            item("k4", "Other", disableKey = "d4"),
        )
        val result = computeEnableForAddons(current, items, setOf("Cinemeta", "Trakt"))
        assertEquals(listOf("d2", "d4"), result)
    }

    @Test
    fun `enable never removes collection_ keys even if a matching addon row points to them`() {
        val current = listOf("collection_42", "d1")
        val items = listOf(
            item("collection_42", "Cinemeta", disableKey = "collection_42"),
            item("k1", "Cinemeta", disableKey = "d1"),
        )
        val result = computeEnableForAddons(current, items, setOf("Cinemeta"))
        assertEquals(listOf("collection_42"), result)
    }

    @Test
    fun `enable with addon name not in items is a no-op`() {
        val current = listOf("d1")
        val items = listOf(item("k1", "Cinemeta", disableKey = "d1"))
        val result = computeEnableForAddons(current, items, setOf("Unknown"))
        assertEquals(current, result)
    }

    @Test
    fun `enable preserves entries not corresponding to any item`() {
        val current = listOf("d_legacy", "d1")
        val items = listOf(item("k1", "Cinemeta", disableKey = "d1"))
        val result = computeEnableForAddons(current, items, setOf("Cinemeta"))
        assertEquals(listOf("d_legacy"), result)
    }

    @Test
    fun `computeEnableForAddons removes legacy key for matched items`() {
        val current = listOf("legacy_d1", "d2")
        val items = listOf(
            item("k1", "Cinemeta", disableKey = "d1", legacyDisableKey = "legacy_d1"),
            item("k2", "TMDB", disableKey = "d2"),
        )
        val result = computeEnableForAddons(current, items, setOf("Cinemeta"))
        assertEquals(listOf("d2"), result)
    }

    @Test
    fun `computeEnableForAddons removes both keys when both present`() {
        val current = listOf("d1", "legacy_d1", "d2")
        val items = listOf(
            item("k1", "Cinemeta", disableKey = "d1", legacyDisableKey = "legacy_d1"),
            item("k2", "TMDB", disableKey = "d2"),
        )
        val result = computeEnableForAddons(current, items, setOf("Cinemeta"))
        assertEquals(listOf("d2"), result)
    }

    @Test
    fun `computeEnableForAddons returns same instance when no change`() {
        val current = listOf("d_other")
        val items = listOf(item("k1", "Cinemeta", disableKey = "d1", legacyDisableKey = "legacy_d1"))
        val result = computeEnableForAddons(current, items, setOf("Cinemeta"))
        assertSame(current, result)
    }

    @Test
    fun `computeDisableForAddons returns same instance when all already disabled`() {
        val current = listOf("d1", "d2")
        val items = listOf(
            item("k1", "Cinemeta", disableKey = "d1"),
            item("k2", "Cinemeta", disableKey = "d2"),
        )
        val result = computeDisableForAddons(current, items, setOf("Cinemeta"))
        assertSame(current, result)
    }

    // ----- toAddonSummaries -----

    @Test
    fun `toAddonSummaries groups by addon name with correct enabled count`() {
        val items = listOf(
            item("k1", "Cinemeta", disableKey = "d1", isDisabled = false),
            item("k2", "Cinemeta", disableKey = "d2", isDisabled = true),
            item("k3", "TMDB", disableKey = "d3", isDisabled = false),
        )
        val summaries = items.toAddonSummaries()
        assertEquals(2, summaries.size)
        val cinemeta = summaries.first { it.name == "Cinemeta" }
        assertEquals(2, cinemeta.totalCatalogs)
        assertEquals(1, cinemeta.enabledOnHome)
        val tmdb = summaries.first { it.name == "TMDB" }
        assertEquals(1, tmdb.totalCatalogs)
        assertEquals(1, tmdb.enabledOnHome)
    }

    @Test
    fun `toAddonSummaries excludes collection_ keys`() {
        val items = listOf(
            item("collection_42", "Cinemeta", disableKey = "collection_42"),
            item("k1", "Cinemeta", disableKey = "d1"),
            item("collection_99", "TMDB", disableKey = "collection_99"),
        )
        val summaries = items.toAddonSummaries()
        assertEquals(1, summaries.size)
        assertEquals("Cinemeta", summaries[0].name)
        assertEquals(1, summaries[0].totalCatalogs)
    }

    @Test
    fun `toAddonSummaries sorts alphabetically case insensitive`() {
        val items = listOf(
            item("k1", "zebra", disableKey = "d1"),
            item("k2", "Apple", disableKey = "d2"),
            item("k3", "banana", disableKey = "d3"),
            item("k4", "Cherry", disableKey = "d4"),
        )
        val summaries = items.toAddonSummaries()
        assertEquals(listOf("Apple", "banana", "Cherry", "zebra"), summaries.map { it.name })
    }

    @Test
    fun `toAddonSummaries returns empty list when items is empty`() {
        val summaries = emptyList<CatalogOrderItem>().toAddonSummaries()
        assertTrue(summaries.isEmpty())
    }
}
