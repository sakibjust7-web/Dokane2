package com.example.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.components.ShopNavTab
import com.example.ui.viewmodel.BusinessNavDestination
import com.example.util.AppLanguage
import com.example.util.HomeLayoutMode

data class HomePermissionSnapshot(
    val isOwner: Boolean = false,
    val isSimpleMode: Boolean = false,
    val isFatherMode: Boolean = false,
    val isDemoMode: Boolean = false,
    val canSell: Boolean = false,
    val canViewStock: Boolean = false,
    val canAddEditProducts: Boolean = false,
    val canSeeProfit: Boolean = false,
    val canSeeHistory: Boolean = false,
    val canViewReports: Boolean = false,
    val canAccessBaki: Boolean = false,
    val canReceivePayment: Boolean = false,
    val canAccessSuppliers: Boolean = false,
    val canPaySupplier: Boolean = false,
    val canAccessCashAndBank: Boolean = false,
    val canAccessExpenses: Boolean = false,
    val canAccessNotes: Boolean = false
)

sealed interface HomeShortcutTarget {
    data class BusinessDest(val dest: BusinessNavDestination) : HomeShortcutTarget
    data class Tab(val tab: ShopNavTab, val filterLowStock: Boolean = false) : HomeShortcutTarget
    data object OpenPaymentIn : HomeShortcutTarget
    data object OpenPaymentOut : HomeShortcutTarget
    data object OpenRecordPurchase : HomeShortcutTarget
    data object OpenAddParty : HomeShortcutTarget
    data object OpenProductBin : HomeShortcutTarget
    data object OpenAddProduct : HomeShortcutTarget
    data object OpenNotifications : HomeShortcutTarget
    data object OpenSettings : HomeShortcutTarget
}

data class HomeShortcutDefinition(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val icon: ImageVector,
    val target: HomeShortcutTarget,
    val allowedInSimpleMode: Boolean,
    val isPermitted: (HomePermissionSnapshot) -> Boolean
) {
    fun localizedTitle(lang: AppLanguage): String =
        if (lang == AppLanguage.BN) titleBn else titleEn
}

enum class ShortcutAddResult {
    ADDED,
    ALREADY_ADDED,
    LIMIT_REACHED,
    NOT_ALLOWED
}

object HomeShortcutCatalog {

    val DEFAULT_DETAILED_IDS: List<String> = listOf(
        "payment_in",
        "payment_out",
        "record_purchase",
        "expenses",
        "customers_baki",
        "add_product"
    )

    val DEFAULT_SIMPLE_IDS: List<String> = listOf(
        "payment_in",
        "add_product",
        "customers_baki",
        "low_stock"
    )

    val ALL_SHORTCUTS: List<HomeShortcutDefinition> = listOf(
        // Core & More actions allowed in Simple Mode as well (strictly non-owner/route-guarded)
        HomeShortcutDefinition(
            id = "payment_in",
            titleEn = "Payment In",
            titleBn = "টাকা আদায়",
            icon = Icons.AutoMirrored.Filled.CallReceived,
            target = HomeShortcutTarget.OpenPaymentIn,
            allowedInSimpleMode = true,
            isPermitted = { p -> !p.isFatherMode && p.canReceivePayment }
        ),
        HomeShortcutDefinition(
            id = "customers_baki",
            titleEn = "Customer Baki",
            titleBn = "ক্রেতা ও বাকি",
            icon = Icons.Outlined.People,
            target = HomeShortcutTarget.BusinessDest(BusinessNavDestination.BAKI_SCREEN),
            allowedInSimpleMode = true,
            isPermitted = { p -> !p.isFatherMode && p.canAccessBaki }
        ),
        HomeShortcutDefinition(
            id = "add_product",
            titleEn = "Add Stock",
            titleBn = "মাল যোগ",
            icon = Icons.Outlined.AddBox,
            target = HomeShortcutTarget.OpenAddProduct,
            allowedInSimpleMode = true,
            isPermitted = { p -> !p.isFatherMode && p.canAddEditProducts }
        ),
        HomeShortcutDefinition(
            id = "low_stock",
            titleEn = "Low Stock",
            titleBn = "স্বল্প মজুদ",
            icon = Icons.Outlined.WarningAmber,
            target = HomeShortcutTarget.Tab(ShopNavTab.STOCK, filterLowStock = true),
            allowedInSimpleMode = true,
            isPermitted = { p -> !p.isFatherMode && p.canViewStock }
        ),
        HomeShortcutDefinition(
            id = "stock",
            titleEn = "Stock",
            titleBn = "স্টক",
            icon = Icons.Outlined.Inventory2,
            target = HomeShortcutTarget.Tab(ShopNavTab.STOCK),
            allowedInSimpleMode = true,
            isPermitted = { p -> !p.isFatherMode && p.canViewStock }
        ),
        HomeShortcutDefinition(
            id = "alerts",
            titleEn = "Alerts",
            titleBn = "অ্যালার্ট",
            icon = Icons.Outlined.NotificationsNone,
            target = HomeShortcutTarget.OpenNotifications,
            allowedInSimpleMode = true,
            isPermitted = { p -> !p.isFatherMode && (p.canViewStock || p.canAccessBaki) }
        ),
        HomeShortcutDefinition(
            id = "sell",
            titleEn = "New Sale",
            titleBn = "নতুন বিক্রি",
            icon = Icons.Outlined.ShoppingBag,
            target = HomeShortcutTarget.Tab(ShopNavTab.SELL),
            allowedInSimpleMode = true,
            isPermitted = { p -> !p.isFatherMode && p.canSell }
        ),

        // Detailed-only More menu screens (never exposed or opened in Simple Mode)
        HomeShortcutDefinition(
            id = "cash_bank",
            titleEn = "Cash & Bank",
            titleBn = "ক্যাশ ও ব্যাংক",
            icon = Icons.Outlined.AccountBalanceWallet,
            target = HomeShortcutTarget.BusinessDest(BusinessNavDestination.CASH_BANK_SCREEN),
            allowedInSimpleMode = false,
            isPermitted = { p -> !p.isFatherMode && !p.isSimpleMode && p.canAccessCashAndBank }
        ),
        HomeShortcutDefinition(
            id = "payment_out",
            titleEn = "Payment Out",
            titleBn = "টাকা পরিশোধ",
            icon = Icons.AutoMirrored.Filled.CallMade,
            target = HomeShortcutTarget.OpenPaymentOut,
            allowedInSimpleMode = false,
            isPermitted = { p -> !p.isFatherMode && !p.isSimpleMode && p.canPaySupplier }
        ),
        HomeShortcutDefinition(
            id = "expenses",
            titleEn = "Expenses",
            titleBn = "দৈনিক খরচ",
            icon = Icons.Outlined.Percent,
            target = HomeShortcutTarget.BusinessDest(BusinessNavDestination.EXPENSES_SCREEN),
            allowedInSimpleMode = false,
            isPermitted = { p -> !p.isFatherMode && !p.isSimpleMode && p.canAccessExpenses }
        ),
        HomeShortcutDefinition(
            id = "other_income",
            titleEn = "Other Income",
            titleBn = "অন্যান্য আয়",
            icon = Icons.Outlined.AttachMoney,
            target = HomeShortcutTarget.BusinessDest(BusinessNavDestination.OTHER_INCOMES_SCREEN),
            allowedInSimpleMode = false,
            isPermitted = { p -> !p.isFatherMode && !p.isSimpleMode && p.canAccessExpenses }
        ),
        HomeShortcutDefinition(
            id = "add_party",
            titleEn = "Add Party",
            titleBn = "পার্টি যোগ",
            icon = Icons.Outlined.PersonAdd,
            target = HomeShortcutTarget.OpenAddParty,
            allowedInSimpleMode = false,
            isPermitted = { p ->
                !p.isFatherMode && !p.isSimpleMode && (p.canAccessBaki || p.canAccessSuppliers)
            }
        ),
        HomeShortcutDefinition(
            id = "suppliers",
            titleEn = "Suppliers",
            titleBn = "সরবরাহকারী",
            icon = Icons.Outlined.Storefront,
            target = HomeShortcutTarget.BusinessDest(BusinessNavDestination.SUPPLIERS_SCREEN),
            allowedInSimpleMode = false,
            isPermitted = { p -> !p.isFatherMode && !p.isSimpleMode && p.canAccessSuppliers }
        ),
        HomeShortcutDefinition(
            id = "staff_permissions",
            titleEn = "Staff",
            titleBn = "কর্মচারী",
            icon = Icons.Outlined.Badge,
            target = HomeShortcutTarget.BusinessDest(BusinessNavDestination.STAFF_MANAGEMENT_SCREEN),
            allowedInSimpleMode = false,
            isPermitted = { p -> !p.isFatherMode && !p.isSimpleMode && p.isOwner && !p.isDemoMode }
        ),
        HomeShortcutDefinition(
            id = "purchases",
            titleEn = "Purchases",
            titleBn = "ক্রয় তালিকা",
            icon = Icons.Outlined.ShoppingCart,
            target = HomeShortcutTarget.BusinessDest(BusinessNavDestination.REPORT_PURCHASES),
            allowedInSimpleMode = false,
            isPermitted = { p ->
                !p.isFatherMode && !p.isSimpleMode && p.canViewReports && p.canAccessSuppliers
            }
        ),
        HomeShortcutDefinition(
            id = "record_purchase",
            titleEn = "Buy Stock",
            titleBn = "স্টক ক্রয়",
            icon = Icons.Outlined.ShoppingBag,
            target = HomeShortcutTarget.OpenRecordPurchase,
            allowedInSimpleMode = false,
            isPermitted = { p -> !p.isFatherMode && !p.isSimpleMode && p.canAccessSuppliers }
        ),
        HomeShortcutDefinition(
            id = "categories",
            titleEn = "Categories",
            titleBn = "ক্যাটাগরি",
            icon = Icons.Outlined.Category,
            target = HomeShortcutTarget.BusinessDest(BusinessNavDestination.MANAGE_CATEGORIES_SCREEN),
            allowedInSimpleMode = false,
            isPermitted = { p -> !p.isFatherMode && !p.isSimpleMode && p.canAccessExpenses }
        ),
        HomeShortcutDefinition(
            id = "product_bin",
            titleEn = "Bin",
            titleBn = "বিন",
            icon = Icons.Outlined.DeleteOutline,
            target = HomeShortcutTarget.OpenProductBin,
            allowedInSimpleMode = false,
            isPermitted = { p -> !p.isFatherMode && !p.isSimpleMode && p.canViewStock }
        ),
        HomeShortcutDefinition(
            id = "notes",
            titleEn = "Notebook",
            titleBn = "নোটবুক",
            icon = Icons.Outlined.Description,
            target = HomeShortcutTarget.BusinessDest(BusinessNavDestination.NOTES_SCREEN),
            allowedInSimpleMode = false,
            isPermitted = { p -> !p.isFatherMode && !p.isSimpleMode && p.canAccessNotes }
        ),
        HomeShortcutDefinition(
            id = "greeting_cards",
            titleEn = "Greeting Cards",
            titleBn = "শুভেচ্ছা কার্ড",
            icon = Icons.Outlined.Style,
            target = HomeShortcutTarget.BusinessDest(BusinessNavDestination.GREETING_CARDS_SCREEN),
            allowedInSimpleMode = false,
            isPermitted = { p -> !p.isFatherMode && !p.isSimpleMode }
        ),
        HomeShortcutDefinition(
            id = "financial_reports",
            titleEn = "Reports",
            titleBn = "রিপোর্ট",
            icon = Icons.Outlined.Assessment,
            target = HomeShortcutTarget.BusinessDest(BusinessNavDestination.FINANCIAL_REPORTS_HUB),
            allowedInSimpleMode = false,
            isPermitted = { p -> !p.isFatherMode && !p.isSimpleMode && p.canViewReports }
        ),
        HomeShortcutDefinition(
            id = "monthly_report",
            titleEn = "Monthly Report",
            titleBn = "মাসিক রিপোর্ট",
            icon = Icons.Outlined.CalendarMonth,
            target = HomeShortcutTarget.BusinessDest(BusinessNavDestination.MONTHLY_REPORT),
            allowedInSimpleMode = false,
            isPermitted = { p ->
                !p.isFatherMode && !p.isSimpleMode && p.canViewReports && p.canSeeProfit
            }
        ),
        HomeShortcutDefinition(
            id = "data_health",
            titleEn = "Data Health",
            titleBn = "তথ্য যাচাই",
            icon = Icons.Outlined.HealthAndSafety,
            target = HomeShortcutTarget.BusinessDest(BusinessNavDestination.DATA_HEALTH_CHECK),
            allowedInSimpleMode = false,
            isPermitted = { p -> !p.isFatherMode && !p.isSimpleMode && p.isOwner }
        ),
        HomeShortcutDefinition(
            id = "feature_checklist",
            titleEn = "Checklist",
            titleBn = "চেকলিস্ট",
            icon = Icons.Outlined.CheckCircle,
            target = HomeShortcutTarget.BusinessDest(BusinessNavDestination.TEST_CHECKLIST),
            allowedInSimpleMode = false,
            isPermitted = { p -> !p.isFatherMode && !p.isSimpleMode && p.isOwner }
        ),
        HomeShortcutDefinition(
            id = "sub_sales_report",
            titleEn = "Sales Report",
            titleBn = "বিক্রয় রিপোর্ট",
            icon = Icons.AutoMirrored.Outlined.TrendingUp,
            target = HomeShortcutTarget.BusinessDest(BusinessNavDestination.REPORT_SALES),
            allowedInSimpleMode = false,
            isPermitted = { p ->
                !p.isFatherMode && !p.isSimpleMode && p.canViewReports && p.canSeeHistory
            }
        ),
        HomeShortcutDefinition(
            id = "sub_day_book",
            titleEn = "Day Book",
            titleBn = "ডে বুক",
            icon = Icons.Outlined.Book,
            target = HomeShortcutTarget.BusinessDest(BusinessNavDestination.REPORT_DAY_BOOK),
            allowedInSimpleMode = false,
            isPermitted = { p -> !p.isFatherMode && !p.isSimpleMode && p.canViewReports }
        ),
        HomeShortcutDefinition(
            id = "sub_pnl",
            titleEn = "Profit & Loss",
            titleBn = "লাভ ও ক্ষতি",
            icon = Icons.Outlined.AccountBalance,
            target = HomeShortcutTarget.BusinessDest(BusinessNavDestination.REPORT_PROFIT_LOSS),
            allowedInSimpleMode = false,
            isPermitted = { p ->
                !p.isFatherMode && !p.isSimpleMode && p.canViewReports && p.canSeeProfit
            }
        ),
        HomeShortcutDefinition(
            id = "settings",
            titleEn = "Settings",
            titleBn = "সেটিংস",
            icon = Icons.Outlined.Settings,
            target = HomeShortcutTarget.OpenSettings,
            allowedInSimpleMode = false,
            isPermitted = { p -> !p.isFatherMode && !p.isSimpleMode }
        )
    )

    private val byId: Map<String, HomeShortcutDefinition> = ALL_SHORTCUTS.associateBy { it.id }

    fun findById(id: String): HomeShortcutDefinition? = byId[id.trim()]

    fun defaultIdsFor(mode: HomeLayoutMode): List<String> = when (mode) {
        HomeLayoutMode.DETAILED -> DEFAULT_DETAILED_IDS
        HomeLayoutMode.SIMPLE -> DEFAULT_SIMPLE_IDS
    }

    fun defaultSimpleShortcuts(permissions: HomePermissionSnapshot = HomePermissionSnapshot(isOwner = true, isSimpleMode = true, canSell = true, canViewStock = true, canAddEditProducts = true, canAccessBaki = true, canReceivePayment = true)): List<HomeShortcutDefinition> =
        resolveVisible(DEFAULT_SIMPLE_IDS, permissions.copy(isSimpleMode = true, isFatherMode = false))

    fun defaultDetailedShortcuts(permissions: HomePermissionSnapshot = HomePermissionSnapshot(isOwner = true, canSell = true, canViewStock = true, canAddEditProducts = true, canSeeProfit = true, canSeeHistory = true, canViewReports = true, canAccessBaki = true, canReceivePayment = true, canAccessSuppliers = true, canPaySupplier = true, canAccessCashAndBank = true, canAccessExpenses = true, canAccessNotes = true)): List<HomeShortcutDefinition> =
        resolveVisible(DEFAULT_DETAILED_IDS, permissions.copy(isSimpleMode = false, isFatherMode = false))

    fun resolveVisible(
        savedIds: List<String>,
        permissions: HomePermissionSnapshot
    ): List<HomeShortcutDefinition> {
        if (permissions.isFatherMode) return emptyList()
        val mode = if (permissions.isSimpleMode) HomeLayoutMode.SIMPLE else HomeLayoutMode.DETAILED
        return savedIds.asSequence()
            .distinct()
            .mapNotNull { findById(it) }
            .filter { def ->
                (!permissions.isSimpleMode || def.allowedInSimpleMode) && def.isPermitted(permissions)
            }
            .take(mode.maxShortcuts)
            .toList()
    }

    fun availableForPicker(permissions: HomePermissionSnapshot): List<HomeShortcutDefinition> {
        if (permissions.isFatherMode) return emptyList()
        return ALL_SHORTCUTS.filter { def ->
            (!permissions.isSimpleMode || def.allowedInSimpleMode) && def.isPermitted(permissions)
        }
    }
}
