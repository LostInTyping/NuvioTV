@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.nuvio.tv.ui.screens.addon

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card as TvCard
import androidx.tv.material3.CardDefaults as TvCardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.nuvio.tv.R
import com.nuvio.tv.ui.components.LoadingIndicator
import com.nuvio.tv.ui.components.NuvioDialog
import com.nuvio.tv.ui.theme.NuvioColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CatalogOrderScreen(
    viewModel: CatalogOrderViewModel = hiltViewModel(),
    onBackPress: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var showBulkDialog by remember { mutableStateOf(false) }
    val bulkActionsEnabled = remember(uiState.items) {
        uiState.items.any { !it.key.startsWith("collection_") }
    }

    BackHandler { onBackPress() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 24.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.catalog_order_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = NuvioColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.catalog_order_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = NuvioColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.catalog_order_follow_addons),
                            style = MaterialTheme.typography.titleMedium,
                            color = NuvioColors.TextPrimary
                        )
                        Text(
                            text = stringResource(R.string.catalog_order_follow_addons_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = NuvioColors.TextSecondary
                        )
                    }
                    Switch(
                        checked = uiState.followAddonsOrder,
                        onCheckedChange = { viewModel.toggleFollowAddonsOrder(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NuvioColors.Primary,
                            checkedTrackColor = NuvioColors.Primary.copy(alpha = 0.5f)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.catalog_order_bulk_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = NuvioColors.TextPrimary
                        )
                        Text(
                            text = stringResource(R.string.catalog_order_bulk_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = NuvioColors.TextSecondary
                        )
                    }
                    NuvioCardButton(
                        label = stringResource(R.string.catalog_order_bulk_actions),
                        contentColor = NuvioColors.TextPrimary,
                        enabled = bulkActionsEnabled,
                        onClick = { showBulkDialog = true },
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingIndicator()
                        }
                    }
                }

                uiState.items.isEmpty() -> {
                    item {
                        Text(
                            text = stringResource(R.string.catalog_order_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = NuvioColors.TextSecondary
                        )
                    }
                }

                else -> {
                    itemsIndexed(
                        items = uiState.items,
                        key = { _, item -> item.key }
                    ) { index, item ->
                        CatalogOrderCard(
                            item = item,
                            onMoveUp = {
                                viewModel.moveUp(item.key)
                                scope.launch {
                                    listState.animateScrollToItem((index - 1).coerceAtLeast(0))
                                }
                            },
                            onMoveDown = {
                                viewModel.moveDown(item.key)
                                scope.launch {
                                    listState.animateScrollToItem(
                                        (index + 1).coerceAtMost(uiState.items.lastIndex)
                                    )
                                }
                            },
                            onToggleEnabled = { viewModel.toggleCatalogEnabled(item) }
                        )
                    }
                }
            }
        }
    }

    if (showBulkDialog) {
        val summaries = remember(uiState.items) { uiState.items.toAddonSummaries() }
        BulkActionsDialog(
            addons = summaries,
            onConfirm = { action, selectedAddons ->
                viewModel.applyBulkAction(action, selectedAddons)
                showBulkDialog = false
            },
            onDismiss = { showBulkDialog = false },
        )
    }
}

@Composable
private fun CatalogOrderCard(
    item: CatalogOrderItem,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleEnabled: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NuvioColors.BackgroundCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.catalogName} - ${item.typeLabel.toDisplayTypeLabel()}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (item.isDisabled) NuvioColors.TextSecondary else NuvioColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.addonName,
                    style = MaterialTheme.typography.bodySmall,
                    color = NuvioColors.TextSecondary
                )
                if (item.isDisabled) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.catalog_order_disabled_on_home),
                        style = MaterialTheme.typography.bodySmall,
                        color = NuvioColors.Error
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onMoveUp,
                    enabled = item.canMoveUp,
                    colors = ButtonDefaults.colors(
                        containerColor = NuvioColors.BackgroundCard,
                        contentColor = NuvioColors.TextSecondary,
                        focusedContainerColor = NuvioColors.FocusBackground,
                        focusedContentColor = NuvioColors.Primary
                    ),
                    border = ButtonDefaults.border(
                        focusedBorder = Border(
                            border = BorderStroke(2.dp, NuvioColors.FocusRing),
                            shape = RoundedCornerShape(12.dp)
                        )
                    ),
                    shape = ButtonDefaults.shape(RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = stringResource(R.string.cd_move_up)
                    )
                }

                Button(
                    onClick = onMoveDown,
                    enabled = item.canMoveDown,
                    colors = ButtonDefaults.colors(
                        containerColor = NuvioColors.BackgroundCard,
                        contentColor = NuvioColors.TextSecondary,
                        focusedContainerColor = NuvioColors.FocusBackground,
                        focusedContentColor = NuvioColors.Primary
                    ),
                    border = ButtonDefaults.border(
                        focusedBorder = Border(
                            border = BorderStroke(2.dp, NuvioColors.FocusRing),
                            shape = RoundedCornerShape(12.dp)
                        )
                    ),
                    shape = ButtonDefaults.shape(RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = stringResource(R.string.cd_move_down)
                    )
                }

                Button(
                    onClick = onToggleEnabled,
                    colors = ButtonDefaults.colors(
                        containerColor = NuvioColors.BackgroundCard,
                        contentColor = if (item.isDisabled) NuvioColors.Success else NuvioColors.TextSecondary,
                        focusedContainerColor = NuvioColors.FocusBackground,
                        focusedContentColor = if (item.isDisabled) NuvioColors.Success else NuvioColors.Error
                    ),
                    border = ButtonDefaults.border(
                        focusedBorder = Border(
                            border = BorderStroke(2.dp, NuvioColors.FocusRing),
                            shape = RoundedCornerShape(12.dp)
                        )
                    ),
                    shape = ButtonDefaults.shape(RoundedCornerShape(12.dp))
                ) {
                    Text(text = if (item.isDisabled) stringResource(R.string.catalog_order_enable) else stringResource(R.string.catalog_order_disable))
                }
            }
        }
    }
}

private fun String.toDisplayTypeLabel(): String {
    return replaceFirstChar { ch ->
        if (ch.isLowerCase()) ch.titlecase() else ch.toString()
    }
}

@Composable
private fun BulkActionsDialog(
    addons: List<AddonSummary>,
    onConfirm: (BulkAction, Set<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by remember { mutableStateOf(setOf<String>()) }
    var pendingAction by remember { mutableStateOf<BulkAction?>(null) }

    val totalAddons = addons.size
    val totalSelected = selected.size
    val allSelected = totalSelected == totalAddons && totalAddons > 0

    val toggleAddon: (String) -> Unit = { name ->
        selected = if (name in selected) selected - name else selected + name
    }
    val toggleAll: () -> Unit = {
        selected = if (allSelected) emptySet() else addons.map { it.name }.toSet()
    }

    val action = pendingAction
    val selectedAddons = remember(addons, selected) { addons.filter { it.name in selected } }
    val showConfirm = action != null && selectedAddons.isNotEmpty()
    val totalCatalogs = remember(selectedAddons) { selectedAddons.sumOf { it.totalCatalogs } }

    val confirmColor = when (action) {
        BulkAction.ENABLE -> NuvioColors.Success
        BulkAction.DISABLE -> NuvioColors.Error
        null -> NuvioColors.TextPrimary
    }

    val confirmFocusRequester = remember { FocusRequester() }
    LaunchedEffect(showConfirm) {
        if (showConfirm) {
            confirmFocusRequester.requestFocus()
        }
    }

    NuvioDialog(
        onDismiss = if (showConfirm) ({ pendingAction = null }) else onDismiss,
        title = if (showConfirm && action != null) {
            val titleRes = when (action) {
                BulkAction.ENABLE -> R.plurals.catalog_order_bulk_confirm_enable_title
                BulkAction.DISABLE -> R.plurals.catalog_order_bulk_confirm_disable_title
            }
            pluralStringResource(titleRes, selectedAddons.size, selectedAddons.size)
        } else {
            stringResource(R.string.catalog_order_bulk_title)
        },
        subtitle = if (showConfirm && action != null) {
            val bodyRes = when (action) {
                BulkAction.ENABLE -> R.plurals.catalog_order_bulk_confirm_enable_body
                BulkAction.DISABLE -> R.plurals.catalog_order_bulk_confirm_disable_body
            }
            pluralStringResource(bodyRes, totalCatalogs, totalCatalogs)
        } else null,
        width = 640.dp,
        contentSpacing = if (showConfirm) 16.dp else 0.dp,
        suppressFirstKeyUp = false,
    ) {
        if (showConfirm && action != null) {
            val confirmScrollState = rememberScrollState()
            LaunchedEffect(confirmScrollState.maxValue) {
                if (confirmScrollState.maxValue == 0) return@LaunchedEffect
                delay(1200L)
                while (true) {
                    confirmScrollState.animateScrollTo(
                        value = confirmScrollState.maxValue,
                        animationSpec = tween(
                            durationMillis = (confirmScrollState.maxValue * 25).coerceAtLeast(2000),
                            easing = LinearEasing,
                        ),
                    )
                    delay(1500L)
                    confirmScrollState.animateScrollTo(
                        value = 0,
                        animationSpec = tween(durationMillis = 600),
                    )
                    delay(1500L)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NuvioColors.SurfaceVariant)
                    .verticalScroll(confirmScrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                selectedAddons.forEach { addon ->
                    Text(
                        text = pluralStringResource(
                            R.plurals.catalog_order_bulk_confirm_addon_row,
                            addon.totalCatalogs,
                            addon.name,
                            addon.totalCatalogs,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = NuvioColors.TextSecondary,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                NuvioCardButton(
                    label = stringResource(R.string.action_cancel),
                    contentColor = NuvioColors.TextPrimary,
                    cornerRadius = 10.dp,
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.titleMedium,
                    fillWidth = true,
                    onClick = { pendingAction = null },
                )
                NuvioCardButton(
                    label = if (action == BulkAction.ENABLE) {
                        stringResource(R.string.catalog_order_enable)
                    } else {
                        stringResource(R.string.catalog_order_disable)
                    },
                    contentColor = confirmColor,
                    cornerRadius = 10.dp,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(confirmFocusRequester),
                    textStyle = MaterialTheme.typography.titleMedium,
                    fillWidth = true,
                    onClick = { onConfirm(action, selected) },
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BulkActionsPickerHeader(
                    totalSelected = totalSelected,
                    totalAddons = totalAddons,
                    allSelected = allSelected,
                    onToggleAll = toggleAll,
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 312.dp),
                ) {
                    items(addons, key = { it.name }) { addon ->
                        AddonPickerRow(
                            addon = addon,
                            selected = addon.name in selected,
                            onToggle = { toggleAddon(addon.name) },
                        )
                    }
                }

                BulkActionsPickerFooter(
                    onEnableAll = { if (selected.isNotEmpty()) pendingAction = BulkAction.ENABLE },
                    onDisableAll = { if (selected.isNotEmpty()) pendingAction = BulkAction.DISABLE },
                )
            }
        }
    }
}

@Composable
private fun BulkActionsPickerHeader(
    totalSelected: Int,
    totalAddons: Int,
    allSelected: Boolean,
    onToggleAll: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = pluralStringResource(
                R.plurals.catalog_order_bulk_addons_counter,
                totalAddons,
                totalSelected,
                totalAddons,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = NuvioColors.TextSecondary,
        )
        NuvioCardButton(
            label = if (allSelected)
                stringResource(R.string.catalog_order_bulk_deselect_all)
            else
                stringResource(R.string.catalog_order_bulk_select_all),
            contentColor = NuvioColors.TextPrimary,
            cornerRadius = 10.dp,
            onClick = onToggleAll,
        )
    }
}

@Composable
private fun AddonPickerRow(
    addon: AddonSummary,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    TvCard(
        onClick = onToggle,
        colors = TvCardDefaults.colors(
            containerColor = if (selected) NuvioColors.SurfaceVariant else NuvioColors.BackgroundCard,
            focusedContainerColor = NuvioColors.FocusBackground,
        ),
        border = TvCardDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, NuvioColors.FocusRing),
                shape = RoundedCornerShape(12.dp),
            ),
        ),
        scale = TvCardDefaults.scale(focusedScale = 1.04f),
        shape = TvCardDefaults.shape(shape = RoundedCornerShape(12.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BulkCheckbox(checked = selected)
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = addon.name,
                style = MaterialTheme.typography.titleMedium,
                color = NuvioColors.TextPrimary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = if (selected)
                    stringResource(R.string.catalog_order_bulk_selected_count, addon.totalCatalogs)
                else
                    stringResource(
                        R.string.catalog_order_bulk_enabled_count,
                        addon.enabledOnHome,
                        addon.totalCatalogs,
                    ),
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) NuvioColors.TextSecondary else NuvioColors.TextTertiary,
                modifier = Modifier.width(150.dp),
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun BulkCheckbox(checked: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (checked) NuvioColors.TextPrimary else Color.Transparent)
            .border(
                width = 2.dp,
                color = if (checked) NuvioColors.TextPrimary else NuvioColors.Border,
                shape = RoundedCornerShape(6.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = NuvioColors.BackgroundElevated,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun BulkActionsPickerFooter(
    onEnableAll: () -> Unit,
    onDisableAll: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        NuvioCardButton(
            label = stringResource(R.string.catalog_order_disable),
            contentColor = NuvioColors.Error,
            cornerRadius = 14.dp,
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.titleMedium,
            fillWidth = true,
            onClick = onDisableAll,
        )
        NuvioCardButton(
            label = stringResource(R.string.catalog_order_enable),
            contentColor = NuvioColors.Success,
            cornerRadius = 14.dp,
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.titleMedium,
            fillWidth = true,
            onClick = onEnableAll,
        )
    }
}

@Composable
private fun NuvioCardButton(
    label: String,
    contentColor: Color,
    cornerRadius: Dp = 12.dp,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    textStyle: TextStyle? = null,
    fillWidth: Boolean = false,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.colors(
            containerColor = NuvioColors.BackgroundCard,
            contentColor = contentColor,
            focusedContainerColor = NuvioColors.FocusBackground,
            focusedContentColor = contentColor,
            disabledContainerColor = NuvioColors.BackgroundCard,
            disabledContentColor = NuvioColors.TextDisabled,
        ),
        border = ButtonDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, NuvioColors.FocusRing),
                shape = RoundedCornerShape(cornerRadius),
            ),
        ),
        shape = ButtonDefaults.shape(RoundedCornerShape(cornerRadius)),
    ) {
        Text(
            text = label,
            modifier = if (fillWidth) Modifier.fillMaxWidth() else Modifier,
            textAlign = if (fillWidth) TextAlign.Center else null,
            style = textStyle ?: MaterialTheme.typography.labelLarge,
        )
    }
}
