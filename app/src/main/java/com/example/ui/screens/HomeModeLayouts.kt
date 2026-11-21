package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.data.model.Sale
import com.example.data.model.SavedBusinessStrategy
import com.example.data.model.StrategyActionTarget
import com.example.domain.BakiOverview
import com.example.domain.HomeInsights
import com.example.domain.MoneyNowSummary
import com.example.domain.SevenDayCashflowSummary
import com.example.domain.StoreFinancials
import com.example.ui.components.BusinessStrategyCard
import com.example.ui.theme.AlertRed
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividGreen
import com.example.util.AppLanguage
import com.example.util.ShopLocale
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

internal fun formatHomeMoney(amount: Double, lang: AppLanguage, isHidden: Boolean): String {
    if (isHidden) return "${ShopLocale.CURRENCY}••••"
    if (lang == AppLanguage.BN) {
        return ShopLocale.formatMoney(amount, lang)
    }
    val normalized = if (abs(amount) < 0.005) 0.0 else amount
    val absAmount = abs(normalized)
    val nf = NumberFormat.getNumberInstance(Locale.US).apply {
        val hasCents = abs(absAmount - absAmount.roundToInt()) >= 0.01
        minimumFractionDigits = if (hasCents) 2 else 0
        maximumFractionDigits = if (hasCents) 2 else 0
    }
    val sign = if (normalized < 0.0) "-" else ""
    return "$sign${ShopLocale.CURRENCY}${nf.format(absAmount)}"
}

@Composable
private fun BentoGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    testTag: String? = null,
    padding: PaddingValues = PaddingValues(16.dp),
    showBorder: Boolean = false,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    var mod = modifier
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xE81E231E), Color(0xEC131613))
            )
        )
    if (testTag != null) mod = mod.testTag(testTag)
    if (onClick != null) mod = mod.clickable(onClick = onClick)

    Surface(
        modifier = mod,
        shape = shape,
        color = Color.Transparent,
        border = if (showBorder) {
            BorderStroke(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x28FFFFFF), Color(0x0DFFFFFF))
                )
            )
        } else null
    ) {
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}

private fun formatTodayStrategyKey(ms: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return sdf.format(java.util.Date(ms))
}

internal fun isStrategyFromToday(
    strategy: SavedBusinessStrategy?,
    nowMs: Long = System.currentTimeMillis()
): Boolean {
    if (strategy == null) return false
    val todayKey = formatTodayStrategyKey(nowMs)
    if (strategy.dateKey.isNotBlank()) {
        return strategy.dateKey.trim() == todayKey
    }
    if (strategy.generatedAtMs <= 0L) return false
    return formatTodayStrategyKey(strategy.generatedAtMs) == todayKey
}

private fun isFatherStrategyFromToday(strategy: SavedBusinessStrategy?): Boolean =
    isStrategyFromToday(strategy)

internal fun extractSimpleTipLine(
    strategy: SavedBusinessStrategy?,
    maxWords: Int = 25,
    nowMs: Long = System.currentTimeMillis()
): String? {
    if (!isStrategyFromToday(strategy, nowMs)) return null
    val raw = strategy?.focusLine?.trim()
        ?.ifBlank { strategy.actions.firstOrNull()?.text?.trim().orEmpty() }
        ?.ifBlank { strategy.diagnosis.trim() }
        .orEmpty()
    if (raw.isBlank()) return null
    val words = raw.split(Regex("\\s+")).filter { it.isNotBlank() }
    if (words.isEmpty()) return null
    return words.take(maxWords.coerceAtLeast(1)).joinToString(" ")
}

private fun extractFatherTipLine(strategy: SavedBusinessStrategy?): String? =
    extractSimpleTipLine(strategy, maxWords = 15)

@Composable
fun FatherModeHomeLayout(
    financials: StoreFinancials,
    cashInHand: Double,
    bakiOverview: BakiOverview,
    lowStockProducts: List<Product>,
    isPrivacyHidden: Boolean,
    onNavigateToSell: () -> Unit,
    onOpenBaki: () -> Unit,
    onOpenLowStock: () -> Unit,
    onOpenMonthlyReport: () -> Unit = {},
    businessStrategy: SavedBusinessStrategy? = null,
    onAddProduct: () -> Unit = {},
    fatherAddedProducts: List<Product> = emptyList(),
    onEditFatherProduct: (Product) -> Unit = {}
) {
    val context = LocalContext.current
    val amberAccent = Color(0xFFFFC107)
    val highContrastCardBg = Color(0xFF141814)
    val highContrastBorder = Color(0x52FFFFFF)

    val topDebtors = remember(bakiOverview) {
        bakiOverview.debtors
            .filter { it.currentBalance > 0.001 }
            .sortedByDescending { it.currentBalance }
            .take(3)
    }
    val topLowStock = remember(lowStockProducts) {
        lowStockProducts.take(3)
    }
    val todayTip = remember(businessStrategy) {
        extractFatherTipLine(businessStrategy)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 24.dp)
            .testTag("father_home_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. VERY LARGE GREEN BUTTON: "বিক্রি করুন"
        Surface(
            onClick = onNavigateToSell,
            shape = RoundedCornerShape(22.dp),
            color = VividGreen,
            border = BorderStroke(1.5.dp, Color(0xFFA5F27F)),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 68.dp)
                .testTag("home_new_sale_btn")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = Color(0xFF061204),
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "বিক্রি করুন",
                    color = Color(0xFF061204),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        // 1b. BIG BUTTON BELOW SELL: "পণ্য যোগ করুন"
        Surface(
            onClick = onAddProduct,
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF172715),
            border = BorderStroke(1.5.dp, VividGreen),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 62.dp)
                .testTag("father_add_product_btn")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddBox,
                    contentDescription = null,
                    tint = VividGreen,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "পণ্য যোগ করুন",
                    color = VividGreen,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        // 2. CARD: "আজকের হিসাব" (today's sales, cash in hand, number of customers — nothing else)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = highContrastCardBg,
            border = BorderStroke(1.2.dp, highContrastBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home_todays_summary_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "আজকের হিসাব",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                HorizontalDivider(color = Color(0x28FFFFFF))
                FatherSummaryStatRow(
                    label = "আজকের বিক্রি",
                    value = formatHomeMoney(financials.todaySales, AppLanguage.BN, isPrivacyHidden),
                    valueColor = VividGreen
                )
                FatherSummaryStatRow(
                    label = "হাতে নগদ",
                    value = formatHomeMoney(cashInHand, AppLanguage.BN, isPrivacyHidden),
                    valueColor = Color.White
                )
                FatherSummaryStatRow(
                    label = "কাস্টমার",
                    value = "${ShopLocale.formatNumber(financials.todayOrdersCount, AppLanguage.BN)} জন",
                    valueColor = Color.White
                )
                Surface(
                    onClick = onOpenMonthlyReport,
                    shape = RoundedCornerShape(14.dp),
                    color = VividGreen.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, VividGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("father_monthly_report_btn")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "মাসের হিসাব",
                            tint = VividGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "মাসের হিসাব",
                            color = VividGreen,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        // 3. CARD: "কার কাছে পাব" (top 3 debtors + one-tap call + "সব দেখুন")
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = highContrastCardBg,
            border = BorderStroke(1.2.dp, highContrastBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("father_home_baki_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "কার কাছে পাব",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                    Surface(
                        onClick = onOpenBaki,
                        shape = RoundedCornerShape(14.dp),
                        color = VividGreen.copy(alpha = 0.22f),
                        border = BorderStroke(1.dp, VividGreen),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("father_baki_see_all_btn")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "সব দেখুন",
                                color = VividGreen,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                if (topDebtors.isEmpty()) {
                    Text(
                        text = "আজ এখনো কোনো বাকি নেই",
                        color = Color(0xFFD5DCD5),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    topDebtors.forEachIndexed { idx, summary ->
                        if (idx > 0) {
                            HorizontalDivider(color = Color(0x1EFFFFFF))
                        }
                        val phone = summary.customer.mobile.trim()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 64.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Text(
                                    text = summary.customer.name.ifBlank { "কাস্টমার" },
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = formatHomeMoney(summary.currentBalance, AppLanguage.BN, isPrivacyHidden),
                                color = amberAccent,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false
                            )
                            if (phone.isNotBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    onClick = {
                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                        runCatching { context.startActivity(dialIntent) }
                                    },
                                    shape = CircleShape,
                                    color = VividGreen,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .testTag("father_baki_call_${summary.customer.id}")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = "${summary.customer.name} কল করুন",
                                            tint = Color(0xFF061204),
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. CARD: "কী মাল কমে গেছে" (up to 3 low-stock items + "সব দেখুন")
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = highContrastCardBg,
            border = BorderStroke(1.2.dp, highContrastBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home_low_stock_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "কী মাল কমে গেছে",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                    Surface(
                        onClick = onOpenLowStock,
                        shape = RoundedCornerShape(14.dp),
                        color = amberAccent.copy(alpha = 0.20f),
                        border = BorderStroke(1.dp, amberAccent),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("father_low_stock_see_all_btn")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "সব দেখুন",
                                color = amberAccent,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                if (topLowStock.isEmpty()) {
                    Text(
                        text = "সব মাল পর্যাপ্ত আছে",
                        color = VividGreen,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    topLowStock.forEachIndexed { idx, product ->
                        if (idx > 0) {
                            HorizontalDivider(color = Color(0x1EFFFFFF))
                        }
                        val displayName = product.nameBn.ifBlank { product.name }
                        val unitLabel = ShopLocale.displayUnit(product.unit, AppLanguage.BN)
                        val qtyText = "${ShopLocale.formatNumber(product.stock, AppLanguage.BN)} $unitLabel"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = displayName,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 10.dp)
                            )
                            Text(
                                text = qtyText,
                                color = if (product.stock <= 0.0) Color(0xFFFF5252) else amberAccent,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }

        // 4b. CARD: "আমার যোগ করা পণ্য" (products added by Father — tap to edit)
        if (fatherAddedProducts.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = highContrastCardBg,
                border = BorderStroke(1.2.dp, highContrastBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("father_added_products_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "আমার যোগ করা পণ্য",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                    fatherAddedProducts.forEachIndexed { idx, product ->
                        if (idx > 0) {
                            HorizontalDivider(color = Color(0x1EFFFFFF))
                        }
                        val displayName = product.nameBn.ifBlank { product.name }
                        val unitLabel = ShopLocale.displayUnit(product.unit, AppLanguage.BN)
                        val infoText = "${ShopLocale.formatNumber(product.stock, AppLanguage.BN)} $unitLabel · ${ShopLocale.formatMoney(product.sellPrice, AppLanguage.BN)}"
                        Surface(
                            onClick = { onEditFatherProduct(product) },
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1A201A),
                            border = BorderStroke(1.dp, Color(0x2EFFFFFF)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 56.dp)
                                .testTag("father_added_product_${product.id}")
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
                                        .padding(end = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = displayName,
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = infoText,
                                        color = Color(0xFFD5DCD5),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (product.needsReview) amberAccent.copy(alpha = 0.22f) else VividGreen.copy(alpha = 0.22f),
                                        border = BorderStroke(
                                            1.dp,
                                            if (product.needsReview) amberAccent else VividGreen
                                        )
                                    ) {
                                        Text(
                                            text = if (product.needsReview) "যাচাই বাকি" else "অনুমোদিত",
                                            color = if (product.needsReview) amberAccent else VividGreen,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "ঠিক করুন",
                                        tint = VividGreen,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. SHORT AI TIP LINE (at most 15 words, plain Bengali, hidden if older than today)
        if (!todayTip.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1A2218),
                border = BorderStroke(1.dp, VividGreen.copy(alpha = 0.55f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("father_home_tip_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = amberAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = todayTip,
                        color = Color.White,
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FatherSummaryStatRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color(0xFFDCE2DC),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            softWrap = false
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SimpleHomeLayout(
    financials: StoreFinancials,
    homeInsights: HomeInsights,
    cashInHand: Double,
    @Suppress("UNUSED_PARAMETER") bakiOverview: BakiOverview,
    @Suppress("UNUSED_PARAMETER") lowStockProducts: List<Product>,
    currentLanguage: AppLanguage,
    isPrivacyHidden: Boolean,
    canSell: Boolean,
    @Suppress("UNUSED_PARAMETER") canViewStock: Boolean,
    @Suppress("UNUSED_PARAMETER") canAddEditProducts: Boolean,
    @Suppress("UNUSED_PARAMETER") canAccessBaki: Boolean,
    @Suppress("UNUSED_PARAMETER") canReceivePayment: Boolean,
    canSeeProfit: Boolean,
    canSeeSalesSummary: Boolean,
    onNavigateToSell: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onQuickPaymentIn: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onAddProduct: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onNavigateToBaki: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onOpenLowStock: () -> Unit,
    onNavigateToSalesSummary: () -> Unit = {},
    onOpenMonthlyReport: () -> Unit = {},
    businessStrategy: SavedBusinessStrategy? = null,
    canViewStrategy: Boolean = false,
    showProfitInSimpleMode: Boolean = false,
    shortcuts: List<HomeShortcutDefinition> = HomeShortcutCatalog.defaultSimpleShortcuts(),
    addableShortcuts: List<HomeShortcutDefinition> = emptyList(),
    isEditMode: Boolean = false,
    onEnterEditMode: () -> Unit = {},
    onExitEditMode: () -> Unit = {},
    onShortcutClick: (HomeShortcutDefinition) -> Unit = {},
    onMoveShortcutUp: (String) -> Unit = {},
    onMoveShortcutDown: (String) -> Unit = {},
    onRemoveShortcut: (String) -> Unit = {},
    onAddShortcut: (String) -> ShortcutAddResult = { ShortcutAddResult.NOT_ALLOWED },
    onResetShortcutsToDefault: () -> Unit = {}
) {
    val amberAccent = Color(0xFFFFB300)
    val todayTip = remember(businessStrategy) {
        extractSimpleTipLine(businessStrategy, maxWords = 25)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(isEditMode) {
                if (!isEditMode) {
                    detectTapGestures(onLongPress = { onEnterEditMode() })
                }
            }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_dashboard_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Today's key sales hero ring
            if (canSeeSalesSummary) {
                item(key = "simple_sales_ring") {
                    val ringProgress = remember(financials.todaySales, homeInsights.yesterdaySales) {
                        when {
                            homeInsights.yesterdaySales > 0.0 ->
                                (financials.todaySales / homeInsights.yesterdaySales).toFloat().coerceIn(0.08f, 1f)
                            financials.todaySales > 0.0 -> 0.72f
                            else -> 0f
                        }
                    }
                    SimpleSalesRingHero(
                        todaySales = financials.todaySales,
                        ordersCount = financials.todayOrdersCount,
                        progress = ringProgress,
                        currentLanguage = currentLanguage,
                        isPrivacyHidden = isPrivacyHidden,
                        onClick = onNavigateToSalesSummary
                    )
                }
            }

            // 2. Primary full-width SELL CTA
            if (canSell) {
                item(key = "simple_sell_cta") {
                    Surface(
                        onClick = onNavigateToSell,
                        shape = RoundedCornerShape(20.dp),
                        color = VividGreen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 60.dp)
                            .testTag("home_new_sale_btn")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = Color(0xFF061204),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = ShopLocale.get("simple_btn_sell", currentLanguage),
                                color = Color(0xFF061204),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 3. Today's key numbers ("আজকের হিসাব": cash in hand, optional today's profit & monthly report)
            item(key = "simple_todays_accounts_card") {
                BentoGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "simple_todays_accounts_card",
                    padding = PaddingValues(16.dp),
                    showBorder = false
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ShopLocale.get("simple_card_todays_accounts", currentLanguage),
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )
                            SimpleSeeAllButton(
                                text = ShopLocale.get("simple_monthly_report_btn", currentLanguage),
                                accentColor = VividGreen,
                                onClick = onOpenMonthlyReport,
                                testTag = "simple_monthly_report_btn"
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = ShopLocale.get("simple_cash_in_hand", currentLanguage),
                                    color = TextSecondary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = formatHomeMoney(cashInHand, currentLanguage, isPrivacyHidden),
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.testTag("simple_cash_in_hand_value")
                                )
                            }
                            if (showProfitInSimpleMode && canSeeProfit) {
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .fillMaxHeight()
                                        .background(Color(0x1EFFFFFF))
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = ShopLocale.get("simple_todays_profit", currentLanguage),
                                        color = TextSecondary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = formatHomeMoney(financials.todayProfit, currentLanguage, isPrivacyHidden),
                                        color = if (financials.todayProfit < 0.0) AlertRed else VividGreen,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.testTag("simple_todays_profit_value")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Customizable Shortcut Buttons (max 6 in Simple Mode + Edit Home)
            item(key = "simple_shortcuts_section") {
                HomeShortcutsSection(
                    shortcuts = shortcuts,
                    addableShortcuts = addableShortcuts,
                    isSimpleMode = true,
                    isEditMode = isEditMode,
                    currentLanguage = currentLanguage,
                    onEnterEditMode = onEnterEditMode,
                    onExitEditMode = onExitEditMode,
                    onShortcutClick = onShortcutClick,
                    onMoveUp = onMoveShortcutUp,
                    onMoveDown = onMoveShortcutDown,
                    onRemove = onRemoveShortcut,
                    onAddShortcut = onAddShortcut,
                    onResetToDefault = onResetShortcutsToDefault
                )
            }

            // 5. Strategy Tip card moved to the very bottom of Home
            if (canViewStrategy && !todayTip.isNullOrBlank()) {
                item(key = "simple_business_strategy") {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF172016),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("simple_business_strategy")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = amberAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = todayTip,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 6. Long-press empty Home space target
            item(key = "home_empty_space_target") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = !isEditMode,
                            onClick = {},
                            onLongClick = onEnterEditMode
                        )
                        .testTag("home_empty_space_target")
                )
            }
        }
    }
}

@Composable
private fun SimpleSeeAllButton(
    text: String,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = accentColor.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.65f)),
        modifier = Modifier
            .defaultMinSize(minHeight = 52.dp)
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = text,
                color = accentColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun SimpleSalesRingHero(
    todaySales: Double,
    ordersCount: Long,
    progress: Float,
    currentLanguage: AppLanguage,
    isPrivacyHidden: Boolean,
    onClick: () -> Unit
) {
    val numberPart = remember(todaySales, currentLanguage, isPrivacyHidden) {
        if (isPrivacyHidden) {
            "••••"
        } else if (currentLanguage == AppLanguage.BN) {
            ShopLocale.formatMoney(todaySales, currentLanguage).replace(ShopLocale.CURRENCY, "").trim()
        } else {
            val nf = NumberFormat.getNumberInstance(Locale.US).apply {
                val hasCents = abs(todaySales - todaySales.roundToInt()) >= 0.01
                minimumFractionDigits = if (hasCents) 2 else 0
                maximumFractionDigits = if (hasCents) 2 else 0
            }
            nf.format(todaySales)
        }
    }

    Box(
        modifier = Modifier
            .size(214.dp)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            VividGreen.copy(alpha = 0.36f),
                            VividGreen.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.56f, size.height * 0.68f),
                        radius = size.width * 0.72f
                    )
                )
            }
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
                .testTag("home_todays_summary_card")
        )
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val strokePx = 10.dp.toPx()
            val inset = strokePx / 2f

            // Inner dark glass disk
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF282D28), Color(0xFF131613)),
                    center = Offset(size.width * 0.42f, size.height * 0.34f),
                    radius = size.width * 0.65f
                ),
                radius = (size.minDimension / 2f) - strokePx * 0.45f
            )

            // Outer glass bezel
            drawCircle(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x55FFFFFF), Color(0x14FFFFFF))
                ),
                radius = (size.minDimension / 2f),
                style = Stroke(width = 1.2.dp.toPx())
            )

            // Background circular track
            drawArc(
                color = Color(0x28FFFFFF),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(size.width - strokePx, size.height - strokePx),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Vivid green progress arc
            if (progress > 0f) {
                val sweep = 360f * progress
                drawArc(
                    color = VividGreen.copy(alpha = 0.25f),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = androidx.compose.ui.geometry.Size(size.width - strokePx, size.height - strokePx),
                    style = Stroke(width = strokePx + 6.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFF7BFF48),
                            VividGreen,
                            Color(0xFF3E9C1A),
                            Color(0xFF7BFF48)
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = androidx.compose.ui.geometry.Size(size.width - strokePx, size.height - strokePx),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text(
                text = ShopLocale.get("simple_todays_sales", currentLanguage),
                color = TextSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = VividGreen, fontWeight = FontWeight.ExtraBold, fontSize = 25.sp)) {
                        append(ShopLocale.CURRENCY)
                    }
                    withStyle(SpanStyle(color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)) {
                        append(numberPart)
                    }
                },
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("home_tile_today_sales")
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (currentLanguage == AppLanguage.BN) {
                    "${ShopLocale.formatNumber(ordersCount, currentLanguage)}টি অর্ডার"
                } else {
                    if (ordersCount == 1L) "1 order" else "$ordersCount orders"
                },
                color = TextSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailedHomeLayout(
    financials: StoreFinancials,
    homeInsights: HomeInsights,
    bakiOverview: BakiOverview,
    totalBalance: Double,
    moneyNow: MoneyNowSummary,
    @Suppress("UNUSED_PARAMETER") lowStockProducts: List<Product>,
    @Suppress("UNUSED_PARAMETER") sevenDayCashflow: SevenDayCashflowSummary,
    @Suppress("UNUSED_PARAMETER") recentSales: List<Sale>,
    currentLanguage: AppLanguage,
    isPrivacyHidden: Boolean,
    canSeeSalesSummary: Boolean,
    canSeeProfit: Boolean,
    canAccessBaki: Boolean,
    canReceivePayment: Boolean,
    canAccessCashAndBank: Boolean,
    canAccessSuppliers: Boolean,
    canPaySupplier: Boolean,
    @Suppress("UNUSED_PARAMETER") canViewStock: Boolean,
    @Suppress("UNUSED_PARAMETER") canAddEditProducts: Boolean,
    @Suppress("UNUSED_PARAMETER") canViewReports: Boolean,
    @Suppress("UNUSED_PARAMETER") canAccessExpenses: Boolean,
    @Suppress("UNUSED_PARAMETER") canSeeHistory: Boolean,
    canSell: Boolean = true,
    onNavigateToSell: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onAddProduct: () -> Unit = {},
    onOpenSalesSummary: () -> Unit,
    onOpenBaki: () -> Unit,
    onOpenCashAndBank: () -> Unit,
    onOpenSuppliers: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onOpenLowStock: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onRestockProduct: (Product) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onOpenCashflowDetail: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onOpenHistory: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onOpenSaleDetail: (Sale) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onQuickPaymentIn: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onQuickPaymentOut: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onQuickPurchase: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onQuickExpense: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onQuickAddParty: () -> Unit = {},
    businessStrategy: SavedBusinessStrategy? = null,
    canViewStrategy: Boolean = false,
    canRefreshStrategy: Boolean = false,
    isStrategyRefreshing: Boolean = false,
    strategyRefreshesRemaining: Int = 0,
    onRefreshStrategy: () -> Unit = {},
    onStrategyAction: (StrategyActionTarget) -> Unit = {},
    shortcuts: List<HomeShortcutDefinition> = HomeShortcutCatalog.defaultDetailedShortcuts(),
    addableShortcuts: List<HomeShortcutDefinition> = emptyList(),
    isEditMode: Boolean = false,
    onEnterEditMode: () -> Unit = {},
    onExitEditMode: () -> Unit = {},
    onShortcutClick: (HomeShortcutDefinition) -> Unit = {},
    onMoveShortcutUp: (String) -> Unit = {},
    onMoveShortcutDown: (String) -> Unit = {},
    onRemoveShortcut: (String) -> Unit = {},
    onAddShortcut: (String) -> ShortcutAddResult = { ShortcutAddResult.NOT_ALLOWED },
    onResetShortcutsToDefault: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(isEditMode) {
                if (!isEditMode) {
                    detectTapGestures(onLongPress = { onEnterEditMode() })
                }
            }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_dashboard_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Primary SELL CTA (single clean action, no duplicate Add Product next to it)
            if (canSell) {
                item(key = "detailed_top_sell_add_row") {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 56.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF77EE42), VividGreen, Color(0xFF4BB621))
                                )
                            )
                            .clickable(onClick = onNavigateToSell)
                            .testTag("home_new_sale_btn")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = Color(0xFF0A1406),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = ShopLocale.get("home_sell_cta", currentLanguage),
                                color = Color(0xFF0A1406),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 2. Hero TODAY glowing key numbers card
            if (canSeeSalesSummary) {
                item(key = "detailed_hero_today") {
                    DetailedTodayHeroCard(
                        financials = financials,
                        homeInsights = homeInsights,
                        currentLanguage = currentLanguage,
                        isPrivacyHidden = isPrivacyHidden,
                        canSeeProfit = canSeeProfit,
                        onClick = onOpenSalesSummary
                    )
                }
            }

            // 3. Unified Key Balances Strip (To Receive | Cash & Bank | To Give) — clean dark glass, no borders
            val showLeftBaki = canAccessBaki || canReceivePayment
            val showRightTopCash = canAccessCashAndBank
            val showRightBottomGive = canAccessSuppliers || canPaySupplier

            if (showLeftBaki || showRightTopCash || showRightBottomGive) {
                item(key = "detailed_bento_money") {
                    BentoGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        padding = PaddingValues(horizontal = 14.dp, vertical = 16.dp),
                        showBorder = false
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var renderedFirst = false
                            if (showLeftBaki) {
                                renderedFirst = true
                                DetailedKeyBalanceTile(
                                    label = if (currentLanguage == AppLanguage.BN) "পাব" else "To Receive",
                                    amount = formatHomeMoney(bakiOverview.totalBakiOwed, currentLanguage, isPrivacyHidden),
                                    onClick = onOpenBaki,
                                    testTag = "home_to_receive_card",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (showRightTopCash) {
                                if (renderedFirst) {
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .fillMaxHeight()
                                            .background(Color(0x16FFFFFF))
                                    )
                                }
                                renderedFirst = true
                                DetailedKeyBalanceTile(
                                    label = if (currentLanguage == AppLanguage.BN) "ক্যাশ ও ব্যাংক" else "Cash & Bank",
                                    amount = formatHomeMoney(totalBalance, currentLanguage, isPrivacyHidden),
                                    onClick = onOpenCashAndBank,
                                    testTag = "home_cash_bank_card",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (showRightBottomGive) {
                                if (renderedFirst) {
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .fillMaxHeight()
                                            .background(Color(0x16FFFFFF))
                                    )
                                }
                                DetailedKeyBalanceTile(
                                    label = if (currentLanguage == AppLanguage.BN) "দেব" else "To Give",
                                    amount = formatHomeMoney(moneyNow.debtToSuppliers, currentLanguage, isPrivacyHidden),
                                    onClick = onOpenSuppliers,
                                    testTag = "home_to_give_card",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Customizable Shortcut Buttons (max 8 in Detailed Mode + Edit Home)
            item(key = "detailed_shortcuts_section") {
                HomeShortcutsSection(
                    shortcuts = shortcuts,
                    addableShortcuts = addableShortcuts,
                    isSimpleMode = false,
                    isEditMode = isEditMode,
                    currentLanguage = currentLanguage,
                    onEnterEditMode = onEnterEditMode,
                    onExitEditMode = onExitEditMode,
                    onShortcutClick = onShortcutClick,
                    onMoveUp = onMoveShortcutUp,
                    onMoveDown = onMoveShortcutDown,
                    onRemove = onRemoveShortcut,
                    onAddShortcut = onAddShortcut,
                    onResetToDefault = onResetShortcutsToDefault
                )
            }

            // 5. Business Strategy Card at the VERY BOTTOM below everything else
            if (canViewStrategy && businessStrategy != null) {
                item(key = "detailed_business_strategy") {
                    BusinessStrategyCard(
                        strategy = businessStrategy,
                        isCompactSingleActionMode = false,
                        showRefreshButton = canRefreshStrategy,
                        isRefreshing = isStrategyRefreshing,
                        refreshesRemaining = strategyRefreshesRemaining,
                        onRefresh = onRefreshStrategy,
                        onActionClick = onStrategyAction
                    )
                }
            }

            // 6. Long-press empty Home space target
            item(key = "home_empty_space_target") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = !isEditMode,
                            onClick = {},
                            onLongClick = onEnterEditMode
                        )
                        .testTag("home_empty_space_target")
                )
            }
        }
    }
}

@Composable
private fun DetailedKeyBalanceTile(
    label: String,
    amount: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 56.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = amount,
            color = TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DetailedTodayHeroCard(
    financials: StoreFinancials,
    homeInsights: HomeInsights,
    currentLanguage: AppLanguage,
    isPrivacyHidden: Boolean,
    canSeeProfit: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            VividGreen.copy(alpha = 0.24f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.55f),
                        radius = size.width * 0.65f
                    )
                )
            }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape),
            shape = shape,
            color = Color.Transparent,
            border = BorderStroke(1.3.dp, Color(0xFF53DA24))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xF5152012), Color(0xF70C120A))
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(onClick = onClick)
                        .testTag("home_todays_summary_card")
                )
                // Real sparkline wave rendered behind hero sales figure
                if (homeInsights.todaySparklinePoints.size >= 2) {
                    Canvas(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(top = 34.dp, bottom = 36.dp)
                    ) {
                        val pts = homeInsights.todaySparklinePoints
                        val stepX = size.width / (pts.size - 1).coerceAtLeast(1)
                        val coords = pts.mapIndexed { i, v ->
                            val y = size.height - (v.coerceIn(0f, 1f) * size.height * 0.82f) - size.height * 0.08f
                            Offset(i * stepX, y)
                        }
                        val linePath = Path().apply {
                            moveTo(coords.first().x, coords.first().y)
                            for (i in 0 until coords.lastIndex) {
                                val p0 = coords[i]
                                val p1 = coords[i + 1]
                                val cx = (p0.x + p1.x) / 2f
                                cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                            }
                        }
                        val fillPath = Path().apply {
                            addPath(linePath)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(VividGreen.copy(alpha = 0.22f), Color.Transparent)
                            )
                        )
                        drawPath(
                            path = linePath,
                            color = VividGreen.copy(alpha = 0.28f),
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                        drawPath(
                            path = linePath,
                            color = VividGreen.copy(alpha = 0.85f),
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top chip: TODAY (no "% vs yesterday" fake comparison chip)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x305DD62C),
                            border = BorderStroke(1.dp, Color(0x455DD62C))
                        ) {
                            Text(
                                text = if (currentLanguage == AppLanguage.BN) "আজ" else "TODAY",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Big VividGreen Sales Figure
                    Text(
                        text = formatHomeMoney(financials.todaySales, currentLanguage, isPrivacyHidden),
                        color = VividGreen,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("home_tile_today_sales")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bottom 3-stat row (PROFIT | ORDERS | ITEMS SOLD)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (canSeeProfit) {
                            HeroSubStat(
                                label = if (currentLanguage == AppLanguage.BN) "লাভ" else "PROFIT",
                                value = formatHomeMoney(financials.todayProfit, currentLanguage, isPrivacyHidden),
                                modifier = Modifier.weight(1f),
                                testTag = "home_tile_net_profit"
                            )
                        }
                        HeroSubStat(
                            label = if (currentLanguage == AppLanguage.BN) "অর্ডার" else "ORDERS",
                            value = ShopLocale.formatNumber(financials.todayOrdersCount, currentLanguage),
                            modifier = Modifier.weight(1f),
                            testTag = "home_tile_orders_completed"
                        )
                        HeroSubStat(
                            label = if (currentLanguage == AppLanguage.BN) "বিক্রিত পণ্য" else "ITEMS SOLD",
                            value = ShopLocale.formatNumber(financials.todayItemsSold, currentLanguage),
                            modifier = Modifier.weight(1f),
                            testTag = "home_tile_items_sold"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroSubStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Column(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.3.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            softWrap = true
        )
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 15.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            softWrap = true
        )
    }
}


