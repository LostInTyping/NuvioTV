package com.nuvio.tv.ui.screens.addon

private const val COLLECTION_KEY_PREFIX = "collection_"

internal enum class BulkAction { ENABLE, DISABLE }

internal data class AddonSummary(
    val name: String,
    val totalCatalogs: Int,
    val enabledOnHome: Int,
)

internal fun List<CatalogOrderItem>.toAddonSummaries(): List<AddonSummary> =
    asSequence()
        .filterNot { it.key.startsWith(COLLECTION_KEY_PREFIX) }
        .groupBy { it.addonName }
        .map { (name, group) ->
            AddonSummary(
                name = name,
                totalCatalogs = group.size,
                enabledOnHome = group.count { !it.isDisabled },
            )
        }
        .sortedBy { it.name.lowercase() }

/**
 * Returns the new disabled-keys list after a bulk Disable action scoped to
 * [selectedAddonNames]. Only items whose [CatalogOrderItem.addonName] is in
 * the set AND whose [CatalogOrderItem.disableKey] is not a `collection_*` key
 * contribute. Existing entries in [currentDisabled] are preserved and the
 * result is deduplicated. Returns the same instance when no change is needed.
 */
internal fun computeDisableForAddons(
    currentDisabled: List<String>,
    items: List<CatalogOrderItem>,
    selectedAddonNames: Set<String>,
): List<String> {
    if (selectedAddonNames.isEmpty()) return currentDisabled
    val toDisable = items
        .asSequence()
        .filter { it.addonName in selectedAddonNames }
        .map { it.disableKey }
        .filterNot { it.startsWith(COLLECTION_KEY_PREFIX) }
        .toList()
    if (toDisable.isEmpty()) return currentDisabled
    val currentSet = currentDisabled.toSet()
    if (toDisable.all { it in currentSet }) return currentDisabled
    return (currentDisabled + toDisable).distinct()
}

/**
 * Returns the new disabled-keys list after a bulk Enable action scoped to
 * [selectedAddonNames]. Removes from [currentDisabled] every disable key —
 * including legacy disable keys — for items whose addonName is in the set
 * (excluding `collection_*` keys). Entries with no matching item — and all
 * collection keys — are preserved. Returns the same instance when no change
 * is needed.
 */
internal fun computeEnableForAddons(
    currentDisabled: List<String>,
    items: List<CatalogOrderItem>,
    selectedAddonNames: Set<String>,
): List<String> {
    if (selectedAddonNames.isEmpty()) return currentDisabled
    if (currentDisabled.isEmpty()) return currentDisabled
    val toEnable = items
        .asSequence()
        .filter { it.addonName in selectedAddonNames }
        .flatMap { sequenceOf(it.disableKey, it.legacyDisableKey) }
        .filterNotNull()
        .filterNot { it.startsWith(COLLECTION_KEY_PREFIX) }
        .toSet()
    if (toEnable.isEmpty()) return currentDisabled
    if (currentDisabled.none { it in toEnable }) return currentDisabled
    return currentDisabled.filterNot { it in toEnable }
}
