package org.sindhipinyin.keyboard.data

import android.content.Context
import org.sindhipinyin.engine.RomanMode

class KeyboardPreferences(context: Context) {
    private val epoch = java.util.concurrent.atomic.AtomicLong()
    val learningRevision: Long get() = epoch.get()
    private val prefs = context.getSharedPreferences("keyboard", Context.MODE_PRIVATE)
    var theme: String
        get() = prefs.getString("theme", "system") ?: "system"
        set(value) { prefs.edit().putString("theme", value).apply() }
    var vibration: Boolean
        get() = prefs.getBoolean("vibration", false)
        set(value) { prefs.edit().putBoolean("vibration", value).apply() }
    var sound: Boolean
        get() = prefs.getBoolean("sound", false)
        set(value) { prefs.edit().putBoolean("sound", value).apply() }
    var learning: Boolean
        get() = prefs.getBoolean("learning", false)
        set(value) { epoch.incrementAndGet(); prefs.edit().putBoolean("learning", value).apply() }
    var romanMode: RomanMode
        get() = runCatching { RomanMode.valueOf(prefs.getString("romanMode", "EVERYDAY")!!) }.getOrDefault(RomanMode.EVERYDAY)
        set(value) { epoch.incrementAndGet(); prefs.edit().putString("romanMode", value.name).apply() }
}
