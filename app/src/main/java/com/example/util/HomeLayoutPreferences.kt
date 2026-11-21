package com.example.util

import android.content.Context

enum class HomeLayoutMode(val key: String, val maxShortcuts: Int) {
    DETAILED(key = "detailed", maxShortcuts = 8),
    SIMPLE(key = "simple", maxShortcuts = 6)
}

/**
 * Local-only SharedPreferences storage for customizable Home shortcuts.
 * Stored strictly on-device, scoped per signed-in userId and per mode (Detailed vs Simple).
 * Never synced to Firestore or shared across devices/users.
 */
class HomeLayoutPreferences(
    context: Context,
    prefsName: String = PREFS_NAME
) {
    private val prefs = context.applicationContext
        .getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    /**
     * Returns null when the user has not customized this mode's layout yet (so defaults apply),
     * or the ordered list of saved shortcut IDs (which may be empty if user removed all items).
     */
    fun loadShortcuts(userId: String, mode: HomeLayoutMode): List<String>? {
        val key = storageKey(userId, mode)
        if (!prefs.contains(key)) return null
        val raw = prefs.getString(key, "").orEmpty()
        if (raw.isBlank()) return emptyList()
        return raw.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    fun saveShortcuts(userId: String, mode: HomeLayoutMode, ids: List<String>) {
        val key = storageKey(userId, mode)
        val clean = ids.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        prefs.edit().putString(key, clean.joinToString(",")).apply()
    }

    fun clearShortcuts(userId: String, mode: HomeLayoutMode) {
        val key = storageKey(userId, mode)
        prefs.edit().remove(key).apply()
    }

    private fun storageKey(userId: String, mode: HomeLayoutMode): String {
        val safeUid = userId.trim()
            .ifBlank { "anonymous" }
            .replace(Regex("[^A-Za-z0-9_.-]"), "_")
        return "home_shortcuts_${safeUid}_${mode.key}"
    }

    companion object {
        private const val PREFS_NAME = "storeos_home_layout_prefs"
        private const val DEMO_PREFS_NAME = "storeos_demo_home_layout_prefs"

        fun prefsNameForStore(storeId: String): String =
            if (storeId == "demo_store") DEMO_PREFS_NAME else PREFS_NAME

        fun forStore(context: Context, storeId: String): HomeLayoutPreferences =
            HomeLayoutPreferences(
                context = context,
                prefsName = prefsNameForStore(storeId)
            )

        fun clearDemoPrefs(context: Context) {
            context.applicationContext
                .getSharedPreferences(DEMO_PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
        }
    }
}
