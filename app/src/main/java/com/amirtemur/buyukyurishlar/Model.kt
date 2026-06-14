package com.amirtemur.buyukyurishlar

import android.content.Context

/** Qo'shin turlari */
enum class TroopType(
    val uz: String,
    val short: String,
    val cost: Int,
    val attack: Float,
    val defense: Float
) {
    OTLIQ("Otliq qo'shin", "Otliq", 5, 9f, 5f),
    PIYODA("Piyoda askar", "Piyoda", 2, 5f, 6f),
    KAMONCHI("Kamonchi", "Kamonchi", 3, 7f, 3f),
    ZIRH("Og'ir zirhli", "Zirhli", 6, 6f, 10f),
    QAMAL("Qamal quroli", "Qamal", 10, 12f, 2f)
}

/** Strategiyalar */
enum class Strategy(val uz: String, val tavsif: String) {
    FRONTAL("Frontal hujum", "To'g'ridan-to'g'ri zarba. Otliq va zirhlilar uchun kuchli."),
    YASHIRIN("Yashirin hujum", "Tungi/kutilmagan zarba. Kichik tezkor qo'shin uchun."),
    QANOT("Qanotdan aylanish", "Dushmanni qanotdan o'rab olish. Otliqlar bilan halokatli."),
    MUDOFAA("Mudofaa", "Himoyaga o'tib, dushmanni charchatish. Zirhli va kamonchilar uchun."),
    QAMAL("Qamal", "Qal'ani qamal qilish. Qamal qurollari shart.")
}

/** Jang ichidagi har bosqich uchun taktika tanlovi */
enum class Tactic(val uz: String, val tavsif: String, val atk: Float, val taken: Float, val heal: Float) {
    HUJUM("Hujum", "Kuchli zarba — lekin ko'proq zarar olasiz.", 1.30f, 1.15f, 0f),
    MUDOFAA("Mudofaa", "Himoya: kam zarar, biroz qo'shin tiklanadi.", 0.65f, 0.60f, 8f),
    QANOT("Qanotdan", "Aylanib zarba — dushman hujum qilsa halokatli.", 1.20f, 1.00f, 0f),
    OQ("O'q yog'dirish", "Masofadan o'q — dushman hujumida juda samarali.", 1.05f, 0.85f, 0f),
    ZARBA("Hal qiluvchi zarba", "Bor kuch bilan — katta zarar, katta xavf.", 1.65f, 1.35f, 0f)
}

/** O'yinchi qo'shini */
class Army {
    val units = HashMap<TroopType, Int>().apply { TroopType.values().forEach { put(it, 0) } }

    fun count(t: TroopType) = units[t] ?: 0
    fun set(t: TroopType, n: Int) { units[t] = n.coerceAtLeast(0) }
    fun add(t: TroopType, d: Int) = set(t, count(t) + d)
    fun totalSoldiers(): Int = units.values.sum()
    fun spentGold(): Int = units.entries.sumOf { it.key.cost * it.value }

    /** Strategiyaga qarab umumiy jang quvvati */
    fun power(strategy: Strategy, terrain: Terrain): Float {
        var p = 0f
        for ((t, n) in units) {
            var unitP = t.attack * 0.6f + t.defense * 0.4f
            unitP *= strategyTroopBonus(strategy, t)
            p += unitP * n
        }
        p *= terrainBonus(strategy, terrain)
        return p
    }

    private fun strategyTroopBonus(s: Strategy, t: TroopType): Float = when (s) {
        Strategy.FRONTAL -> when (t) { TroopType.OTLIQ -> 1.35f; TroopType.ZIRH -> 1.25f; else -> 1f }
        Strategy.YASHIRIN -> when (t) { TroopType.OTLIQ -> 1.3f; TroopType.KAMONCHI -> 1.2f; TroopType.QAMAL -> 0.4f; else -> 1f }
        Strategy.QANOT -> when (t) { TroopType.OTLIQ -> 1.5f; TroopType.KAMONCHI -> 1.1f; TroopType.QAMAL -> 0.5f; else -> 1f }
        Strategy.MUDOFAA -> when (t) { TroopType.ZIRH -> 1.4f; TroopType.KAMONCHI -> 1.35f; TroopType.OTLIQ -> 0.85f; else -> 1f }
        Strategy.QAMAL -> when (t) { TroopType.QAMAL -> 1.8f; TroopType.PIYODA -> 1.2f; TroopType.OTLIQ -> 0.7f; else -> 1f }
    }

    private fun terrainBonus(s: Strategy, terrain: Terrain): Float = when (terrain) {
        Terrain.SHAHAR -> if (s == Strategy.QAMAL) 1.3f else 0.95f
        Terrain.DASHT -> if (s == Strategy.QANOT || s == Strategy.FRONTAL) 1.2f else 0.95f
        Terrain.ISSIQ -> if (s == Strategy.MUDOFAA) 1.25f else 0.9f
        Terrain.TOG -> if (s == Strategy.YASHIRIN) 1.25f else 1f
        Terrain.OCHIQ -> if (s == Strategy.FRONTAL) 1.2f else 1f
    }
}

enum class Terrain(val uz: String) {
    SHAHAR("Shahar / qal'a"),
    DASHT("Dasht"),
    ISSIQ("Issiq iqlim"),
    TOG("Tog'li"),
    OCHIQ("Ochiq maydon")
}

data class Campaign(
    val id: Int,
    val title: String,
    val years: String,
    val region: String,
    val enemy: String,
    val reason: String,
    val terrain: Terrain,
    val enemyPower: Float,
    val budget: Int,
    val bestStrategy: Strategy,
    val headline: String,
    val cutscene: List<String>,
    val info: List<String>,
    val mapX: Float,            // 0..1 xarita koordinatasi
    val mapY: Float,
    val isFinal: Boolean = false
)

data class BattleResult(
    val win: Boolean,
    val playerPower: Float,
    val enemyPower: Float,
    val playerLossPct: Int,
    val strategy: Strategy,
    val rating: Int            // 1..3 yulduz
)

/** O'yin progressini SharedPreferences orqali saqlash */
class Progress(context: Context) {
    private val prefs = context.getSharedPreferences("temur_progress", Context.MODE_PRIVATE)

    var unlocked: Int
        get() = prefs.getInt("unlocked", 0)
        set(v) { prefs.edit().putInt("unlocked", maxOf(unlocked, v)).apply() }

    var soundOn: Boolean
        get() = prefs.getBoolean("sound", true)
        set(v) { prefs.edit().putBoolean("sound", v).apply() }

    fun stars(id: Int): Int = prefs.getInt("stars_$id", 0)
    fun setStars(id: Int, s: Int) {
        if (s > stars(id)) prefs.edit().putInt("stars_$id", s).apply()
    }

    fun reset() = prefs.edit().clear().apply()
}
