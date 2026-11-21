package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Product
import com.example.ui.components.ConfigureEdgeToEdgeDialogWindow
import com.example.ui.components.PlainEmptyState
import com.example.ui.components.edgeToEdgeDialogProperties
import com.example.ui.components.safeDialogWindowPadding
import com.example.ui.theme.AlertRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividGreen
import com.example.util.AppLanguage
import com.example.util.HomeLayoutMode
import com.example.util.ShopLocale

@Composable
fun HomeShortcutsSection(
    shortcuts: List<HomeShortcutDefinition>,
    addableShortcuts: List<HomeShortcutDefinition>,
    isSimpleMode: Boolean,
    isEditMode: Boolean,
    currentLanguage: AppLanguage,
    onEnterEditMode: () -> Unit,
    onExitEditMode: () -> Unit,
    onShortcutClick: (HomeShortcutDefinition) -> Unit,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit,
    onRemove: (String) -> Unit,
    onAddShortcut: (String) -> ShortcutAddResult,
    onResetToDefault: () -> Unit,
    modifier: Modifier = Modifier
) {
    val maxLimit = if (isSimpleMode) HomeLayoutMode.SIMPLE.maxShortcuts else HomeLayoutMode.DETAILED.maxShortcuts
    var showAddPicker by rememberSaveable(isEditMode, isSimpleMode) { mutableStateOf(false) }
    var showResetConfirm by rememberSaveable(isEditMode, isSimpleMode) { mutableStateOf(false) }
    var showLimitErrorInPicker by rememberSaveable(showAddPicker) { mutableStateOf(false) }

    val countLabel = "${ShopLocale.formatNumber(shortcuts.size.toLong(), currentLanguage)} / ${ShopLocale.formatNumber(maxLimit.toLong(), currentLanguage)}"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_shortcuts_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header: Shortcuts + "Edit Home / হোম সাজান" (or count when editing)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isEditMode) {
                    ShopLocale.get("edit_home_btn", currentLanguage)
                } else {
                    ShopLocale.get("home_shortcuts_title", currentLanguage)
                },
                color = if (isSimpleMode || isEditMode) TextPrimary else TextSecondary,
                fontSize = if (isSimpleMode) 18.sp else if (isEditMode) 17.sp else 12.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = if (isSimpleMode || isEditMode) 0.sp else 0.8.sp,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )

            if (isEditMode) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x1EFFFFFF)
                ) {
                    Text(
                        text = countLabel,
                        color = if (shortcuts.size >= maxLimit) Color(0xFFFFB300) else VividGreen,
                        fontSize = if (isSimpleMode) 15.sp else 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("home_shortcuts_count_badge")
                    )
                }
            } else {
                Surface(
                    onClick = onEnterEditMode,
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0x18FFFFFF),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("home_edit_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = VividGreen,
                            modifier = Modifier.size(if (isSimpleMode) 18.dp else 16.dp)
                        )
                        Text(
                            text = ShopLocale.get("edit_home_btn", currentLanguage),
                            color = TextPrimary,
                            fontSize = if (isSimpleMode) 15.sp else 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        if (!isEditMode) {
            if (shortcuts.isEmpty()) {
                Surface(
                    onClick = onEnterEditMode,
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0x16FFFFFF),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 64.dp)
                        .testTag("home_shortcuts_empty_card")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ShopLocale.get("no_shortcuts_hint", currentLanguage),
                            color = TextSecondary,
                            fontSize = if (isSimpleMode) 16.sp else 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                val rows = shortcuts.chunked(2)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    rows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { shortcut ->
                                HomeShortcutTile(
                                    shortcut = shortcut,
                                    isSimpleMode = isSimpleMode,
                                    currentLanguage = currentLanguage,
                                    onClick = { onShortcutClick(shortcut) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        } else {
            // EDIT MODE: Clean vertical rows with large Up / Down / Remove(X) controls (no dragging)
            if (shortcuts.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0x16FFFFFF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = ShopLocale.get("home_edit_empty_hint", currentLanguage),
                        color = TextSecondary,
                        fontSize = if (isSimpleMode) 16.sp else 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp, horizontal = 16.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    shortcuts.forEachIndexed { idx, shortcut ->
                        HomeEditShortcutRow(
                            shortcut = shortcut,
                            canMoveUp = idx > 0,
                            canMoveDown = idx < shortcuts.lastIndex,
                            isSimpleMode = isSimpleMode,
                            currentLanguage = currentLanguage,
                            onMoveUp = { onMoveUp(shortcut.id) },
                            onMoveDown = { onMoveDown(shortcut.id) },
                            onRemove = { onRemove(shortcut.id) }
                        )
                    }
                }
            }

            if (shortcuts.size >= maxLimit) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0x28FFB300),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_shortcut_limit_msg")
                ) {
                    Text(
                        text = ShopLocale.get("shortcut_limit_reached_msg", currentLanguage),
                        color = Color(0xFFFFCA45),
                        fontSize = if (isSimpleMode) 16.sp else 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    )
                }
            }

            // Add shortcut button
            Surface(
                onClick = {
                    showLimitErrorInPicker = shortcuts.size >= maxLimit
                    showAddPicker = true
                },
                shape = RoundedCornerShape(18.dp),
                color = VividGreen.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, VividGreen.copy(alpha = 0.50f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = if (isSimpleMode) 60.dp else 54.dp)
                    .testTag("home_add_shortcut_btn")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = VividGreen,
                        modifier = Modifier.size(if (isSimpleMode) 24.dp else 20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = ShopLocale.get("add_shortcut_btn", currentLanguage),
                        color = VividGreen,
                        fontSize = if (isSimpleMode) 17.sp else 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Reset & Done buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    onClick = { showResetConfirm = true },
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0x1EFFFFFF),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = if (isSimpleMode) 58.dp else 52.dp)
                        .testTag("home_edit_reset_btn")
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isSimpleMode) {
                                ShopLocale.get("reset_short_btn", currentLanguage)
                            } else {
                                ShopLocale.get("reset_to_default_btn", currentLanguage)
                            },
                            color = TextPrimary,
                            fontSize = if (isSimpleMode) 16.sp else 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    onClick = onExitEditMode,
                    shape = RoundedCornerShape(18.dp),
                    color = VividGreen,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = if (isSimpleMode) 58.dp else 52.dp)
                        .testTag("home_edit_done_btn")
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ShopLocale.get("edit_home_done", currentLanguage),
                            color = Color(0xFF061204),
                            fontSize = if (isSimpleMode) 17.sp else 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    if (showAddPicker && isEditMode) {
        HomeAddShortcutDialog(
            addableShortcuts = addableShortcuts,
            currentIds = shortcuts.map { it.id }.toSet(),
            isAtLimit = shortcuts.size >= maxLimit,
            showLimitMessage = showLimitErrorInPicker || shortcuts.size >= maxLimit,
            isSimpleMode = isSimpleMode,
            currentLanguage = currentLanguage,
            onSelect = { entry ->
                val result = onAddShortcut(entry.id)
                when (result) {
                    ShortcutAddResult.ADDED -> showAddPicker = false
                    ShortcutAddResult.LIMIT_REACHED -> showLimitErrorInPicker = true
                    ShortcutAddResult.ALREADY_ADDED -> showAddPicker = false
                    ShortcutAddResult.NOT_ALLOWED -> {}
                }
            },
            onDismiss = { showAddPicker = false }
        )
    }

    if (showResetConfirm && isEditMode) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            properties = edgeToEdgeDialogProperties(),
            modifier = Modifier
                .safeDialogWindowPadding()
                .testTag("home_reset_confirm_dialog"),
            containerColor = Color(0xFF1A1E1A),
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = {
                ConfigureEdgeToEdgeDialogWindow()
                Text(
                    text = ShopLocale.get("reset_home_confirm_title", currentLanguage),
                    fontSize = if (isSimpleMode) 20.sp else 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = ShopLocale.get("reset_home_confirm_desc", currentLanguage),
                    fontSize = if (isSimpleMode) 16.sp else 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetToDefault()
                        showResetConfirm = false
                    },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("home_edit_reset_confirm_btn")
                ) {
                    Text(
                        text = ShopLocale.get("reset_home_confirm_yes", currentLanguage),
                        color = VividGreen,
                        fontSize = if (isSimpleMode) 16.sp else 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetConfirm = false },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("home_edit_reset_cancel_btn")
                ) {
                    Text(
                        text = ShopLocale.get("cancel", currentLanguage),
                        color = TextSecondary,
                        fontSize = if (isSimpleMode) 16.sp else 14.sp
                    )
                }
            }
        )
    }
}

@Composable
private fun HomeShortcutTile(
    shortcut: HomeShortcutDefinition,
    isSimpleMode: Boolean,
    currentLanguage: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    Surface(
        onClick = onClick,
        shape = shape,
        color = Color.Transparent,
        modifier = modifier
            .defaultMinSize(minHeight = if (isSimpleMode) 74.dp else 66.dp)
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xE8212621), Color(0xEA141714))
                )
            )
            .testTag("home_shortcut_${shortcut.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (isSimpleMode) 42.dp else 38.dp)
                    .clip(CircleShape)
                    .background(VividGreen.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = shortcut.icon,
                    contentDescription = null,
                    tint = VividGreen,
                    modifier = Modifier.size(if (isSimpleMode) 22.dp else 20.dp)
                )
            }
            Text(
                text = shortcut.localizedTitle(currentLanguage),
                color = TextPrimary,
                fontSize = if (isSimpleMode) 16.sp else 14.5.sp,
                lineHeight = if (isSimpleMode) 20.sp else 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HomeEditShortcutRow(
    shortcut: HomeShortcutDefinition,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    isSimpleMode: Boolean,
    currentLanguage: AppLanguage,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Surface(
        shape = shape,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = if (isSimpleMode) 68.dp else 60.dp)
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xEB222722), Color(0xEC141714))
                )
            )
            .testTag("home_edit_item_${shortcut.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = shortcut.icon,
                contentDescription = null,
                tint = VividGreen,
                modifier = Modifier.size(if (isSimpleMode) 24.dp else 21.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = shortcut.localizedTitle(currentLanguage),
                color = TextPrimary,
                fontSize = if (isSimpleMode) 17.sp else 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onMoveUp,
                enabled = canMoveUp,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("home_edit_up_${shortcut.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = ShopLocale.get("move_up_desc", currentLanguage),
                    tint = if (canMoveUp) TextPrimary else TextMuted.copy(alpha = 0.35f),
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(
                onClick = onMoveDown,
                enabled = canMoveDown,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("home_edit_down_${shortcut.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = ShopLocale.get("move_down_desc", currentLanguage),
                    tint = if (canMoveDown) TextPrimary else TextMuted.copy(alpha = 0.35f),
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("home_edit_remove_${shortcut.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = ShopLocale.get("remove_shortcut_desc", currentLanguage),
                    tint = AlertRed,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun HomeAddShortcutDialog(
    addableShortcuts: List<HomeShortcutDefinition>,
    currentIds: Set<String>,
    isAtLimit: Boolean,
    showLimitMessage: Boolean,
    isSimpleMode: Boolean,
    currentLanguage: AppLanguage,
    onSelect: (HomeShortcutDefinition) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = edgeToEdgeDialogProperties(usePlatformDefaultWidth = false)
    ) {
        ConfigureEdgeToEdgeDialogWindow()
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF141814),
            border = BorderStroke(1.dp, Color(0x30FFFFFF)),
            modifier = Modifier
                .fillMaxWidth()
                .safeDialogWindowPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .testTag("home_add_shortcut_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ShopLocale.get("add_shortcut_sheet_title", currentLanguage),
                        color = TextPrimary,
                        fontSize = if (isSimpleMode) 20.sp else 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("home_add_shortcut_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = ShopLocale.get("close", currentLanguage),
                            tint = TextSecondary
                        )
                    }
                }

                if (showLimitMessage || isAtLimit) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0x28FFB300),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_add_limit_msg")
                    ) {
                        Text(
                            text = ShopLocale.get("shortcut_limit_reached_msg", currentLanguage),
                            color = Color(0xFFFFCA45),
                            fontSize = if (isSimpleMode) 16.sp else 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        )
                    }
                }

                if (addableShortcuts.isEmpty()) {
                    PlainEmptyState(
                        message = ShopLocale.get("no_shortcuts_hint", currentLanguage)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 380.dp)
                            .testTag("home_add_shortcut_list"),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            count = addableShortcuts.size,
                            key = { addableShortcuts[it].id }
                        ) { idx ->
                            val entry = addableShortcuts[idx]
                            val alreadyAdded = entry.id in currentIds
                            Surface(
                                onClick = { onSelect(entry) },
                                shape = RoundedCornerShape(16.dp),
                                color = if (alreadyAdded) Color(0x185DD62C) else Color(0xFF1D221D),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = if (isSimpleMode) 60.dp else 54.dp)
                                    .testTag("home_picker_item_${entry.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = entry.icon,
                                        contentDescription = null,
                                        tint = VividGreen,
                                        modifier = Modifier.size(if (isSimpleMode) 24.dp else 21.dp)
                                    )
                                    Text(
                                        text = entry.localizedTitle(currentLanguage),
                                        color = TextPrimary,
                                        fontSize = if (isSimpleMode) 17.sp else 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (alreadyAdded) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = VividGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = ShopLocale.get("shortcut_added_badge", currentLanguage),
                                                color = VividGreen,
                                                fontSize = if (isSimpleMode) 13.sp else 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeProductBinDialog(
    products: List<Product>,
    isOwner: Boolean,
    currentLanguage: AppLanguage,
    onRestore: (Product) -> Unit,
    onEmptyBin: () -> Unit,
    onDismiss: () -> Unit
) {
    val isBn = currentLanguage == AppLanguage.BN
    Dialog(
        onDismissRequest = onDismiss,
        properties = edgeToEdgeDialogProperties(usePlatformDefaultWidth = false)
    ) {
        ConfigureEdgeToEdgeDialogWindow()
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF141814),
            border = BorderStroke(1.dp, Color(0x30FFFFFF)),
            modifier = Modifier
                .fillMaxWidth()
                .safeDialogWindowPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("home_product_bin_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBn) "পণ্য বিন (${ShopLocale.formatNumber(products.size.toLong(), currentLanguage)})" else "Product Bin (${products.size})",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (isOwner && products.isNotEmpty()) {
                        Surface(
                            onClick = onEmptyBin,
                            shape = RoundedCornerShape(14.dp),
                            color = AlertRed.copy(alpha = 0.16f),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("home_empty_bin_btn")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = if (isBn) "সব মুছুন" else "Empty Bin",
                                    color = AlertRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                if (products.isEmpty()) {
                    PlainEmptyState(
                        message = if (isBn) "রিসাইকেল বিন খালি" else "Recycle Bin is empty"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(min = 100.dp, max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(products.size, key = { products[it].id }) { idx ->
                            val item = products[idx]
                            val unitLabel = ShopLocale.displayUnit(item.unit, currentLanguage)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF1D221D)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp)
                                    ) {
                                        Text(
                                            text = if (isBn && item.nameBn.isNotBlank()) item.nameBn else item.name,
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${ShopLocale.formatNumber(item.stock, currentLanguage)} $unitLabel · ${ShopLocale.formatMoney(item.sellPrice, currentLanguage)}",
                                            color = TextSecondary,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Surface(
                                        onClick = { onRestore(item) },
                                        shape = RoundedCornerShape(12.dp),
                                        color = VividGreen.copy(alpha = 0.16f),
                                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.RestoreFromTrash,
                                                contentDescription = null,
                                                tint = VividGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = if (isBn) "ফেরত নিন" else "Restore",
                                                color = VividGreen,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Surface(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF242924),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBn) "বন্ধ করুন" else "Close",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}
