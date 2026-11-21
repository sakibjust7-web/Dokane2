package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.HealthProblem
import com.example.ui.components.ConfigureEdgeToEdgeDialogWindow
import com.example.ui.components.GlassCard
import com.example.ui.components.PlainEmptyState
import com.example.ui.components.edgeToEdgeDialogProperties
import com.example.ui.components.safeDialogWindowPadding
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividGreen
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.StoreViewModel
import com.example.util.AppLanguage
import com.example.util.ShopLocale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Step 6: Data Health Check Screen.
 * Scans for the 7 specific problem types, displays plain-words explanations,
 * gates fixes behind owner verification with explanatory dialogs for staff,
 * and logs all fixes with timestamp and user ID for auditing.
 */
@Composable
fun DataHealthCheckScreen(
    authViewModel: AuthViewModel,
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val problems by storeViewModel.healthProblems.collectAsState()
    val lastScanMillis by storeViewModel.lastHealthScanMillis.collectAsState()
    val isScanning by storeViewModel.isScanningHealth.collectAsState()
    val fixLogs by storeViewModel.healthFixLogs.collectAsState()
    val isOwner by storeViewModel.isOwner.collectAsState()
    val isSimpleMode by storeViewModel.isSimpleMode.collectAsState()
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()

    if (!isOwner) {
        if (isSimpleMode) {
            LaunchedEffect(Unit) { storeViewModel.navigateBackInBusiness() }
            return
        }
        com.example.ui.components.NoPermissionScreen(
            onBack = { storeViewModel.navigateBackInBusiness() },
            modifier = modifier,
            currentLanguage = currentLanguage
        )
        return
    }

    var selectedProblemToFix by remember { mutableStateOf<HealthProblem?>(null) }
    var showStaffBlockedModal by rememberSaveable { mutableStateOf(false) }

    BackHandler {
        storeViewModel.navigateBackInBusiness()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .displayCutoutPadding()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item(key = "health_top_bar") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { storeViewModel.navigateBackInBusiness() },
                    modifier = Modifier.testTag("health_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = ShopLocale.get("data_health_title", currentLanguage),
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Header Card with "Scan now" button
        item(key = "health_scanner_card") {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x335DD62C)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HealthAndSafety,
                                        contentDescription = null,
                                        tint = VividGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = ShopLocale.get("health_scanner_title", currentLanguage),
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = ShopLocale.get("health_scanner_sub", currentLanguage),
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { storeViewModel.scanDataHealth() },
                                enabled = !isScanning,
                                colors = ButtonDefaults.buttonColors(containerColor = VividGreen),
                                modifier = Modifier.testTag("btn_scan_now")
                            ) {
                                if (isScanning) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = DarkBackground,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = ShopLocale.get(if (isScanning) "scanning_btn" else "scan_now_btn", currentLanguage),
                                    color = DarkBackground,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
            if (lastScanMillis <= 0L) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        PlainEmptyState(
                            message = ShopLocale.get("health_tap_scan_empty", currentLanguage)
                        )
                    }
                }
            } else if (problems.isEmpty()) {
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x265DD62C)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = VividGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = ShopLocale.get("health_all_clear_title", currentLanguage),
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ShopLocale.get("health_all_clear_sub", currentLanguage),
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            } else {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ShopLocale.format("detected_problems_header", currentLanguage, problems.size),
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = ShopLocale.get("one_tap_audited_fixes", currentLanguage),
                            color = TextMuted,
                            fontSize = 11.5.sp
                        )
                    }
                }

                items(problems) { prob ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ProblemCard(
                            problem = prob,
                            isOwner = isOwner,
                            currentLanguage = currentLanguage,
                            onFix = {
                                if (isOwner) {
                                    selectedProblemToFix = prob
                                } else {
                                    showStaffBlockedModal = true
                                }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Fix Audit Log Section
            if (fixLogs.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.History, contentDescription = null, tint = VividGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${ShopLocale.get("audited_fixes_history", currentLanguage)} (${ShopLocale.formatNumber(fixLogs.size.toLong(), currentLanguage)})",
                            color = TextPrimary,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(fixLogs) { log ->
                    val dateStr = log.fixedAt?.toDate()?.let {
                        ShopLocale.formatDateShort(it.time, currentLanguage)
                    } ?: ShopLocale.get("recently_label", currentLanguage)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 3.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x0DFFFFFF)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = ShopLocale.displayHealthProblemType(log.problemType, currentLanguage),
                                    color = VividGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = dateStr,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = log.fixDetails,
                                color = TextPrimary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = ShopLocale.format("fixed_by_label", currentLanguage, log.fixedBy),
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
    }

    // Confirmation Dialog before applying fix
    selectedProblemToFix?.let { prob ->
        Dialog(
            onDismissRequest = { selectedProblemToFix = null },
            properties = edgeToEdgeDialogProperties(usePlatformDefaultWidth = false)
        ) {
            ConfigureEdgeToEdgeDialogWindow()
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .safeDialogWindowPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0x335DD62C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = VividGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = ShopLocale.get("confirm_data_fix_title", currentLanguage),
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = ShopLocale.get("what_will_change", currentLanguage),
                        color = TextMuted,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = prob.fixDescription,
                        color = TextPrimary,
                        fontSize = 13.5.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = ShopLocale.get("fix_audit_notice", currentLanguage),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { selectedProblemToFix = null }) {
                            Text(ShopLocale.get("cancel", currentLanguage), color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                val problemToApply = prob
                                selectedProblemToFix = null
                                storeViewModel.applyHealthFix(
                                    problem = problemToApply,
                                    ownerUid = storeViewModel.currentUserId
                                ) {
                                    Toast.makeText(
                                        context,
                                        ShopLocale.get("fix_applied_success", currentLanguage),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VividGreen),
                            modifier = Modifier.testTag("btn_confirm_fix")
                        ) {
                            Text(
                                text = ShopLocale.get("apply_fix_btn", currentLanguage),
                                color = DarkBackground,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Explanatory dialog when Staff taps Fix
    if (showStaffBlockedModal) {
        Dialog(
            onDismissRequest = { showStaffBlockedModal = false },
            properties = edgeToEdgeDialogProperties(usePlatformDefaultWidth = false)
        ) {
            ConfigureEdgeToEdgeDialogWindow()
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .safeDialogWindowPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0x33FF5252)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = ShopLocale.get("owner_permission_required", currentLanguage),
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = ShopLocale.get("owner_fix_only_msg", currentLanguage),
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = ShopLocale.get("owner_fix_policy_note", currentLanguage),
                        color = TextSecondary,
                        fontSize = 11.5.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { showStaffBlockedModal = false },
                            colors = ButtonDefaults.buttonColors(containerColor = VividGreen)
                        ) {
                            Text(
                                text = ShopLocale.get("understood_btn", currentLanguage),
                                color = DarkBackground,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProblemCard(
    problem: HealthProblem,
    isOwner: Boolean,
    currentLanguage: AppLanguage,
    onFix: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFB300)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = problem.title,
                            color = TextPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = problem.description,
                            color = TextSecondary,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onFix,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOwner) VividGreen else Color(0x33FFFFFF)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("fix_btn_${problem.id}")
                ) {
                    Text(
                        text = ShopLocale.get(if (isOwner) "fix_btn" else "owner_only_badge", currentLanguage),
                        color = if (isOwner) DarkBackground else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
