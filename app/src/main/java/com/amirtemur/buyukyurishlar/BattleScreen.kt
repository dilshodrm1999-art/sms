package com.amirtemur.buyukyurishlar

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.random.Random

class BattleScreen(game: Game) : Screen(game) {

    private val c = game.selectedCampaign!!
    private val army = game.playerArmy
    private val result: BattleResult

    private val approach = 1.4f
    private val clash = 3.0f
    private val total = approach + clash
    private var t = 0f
    private var done = false
    private var clashTick = 0f
    private var arrowTick = 0f

    private val bContinue = Button("NATIJA ▸")

    private val finalPlayerHp: Float
    private val finalEnemyHp: Float

    private data class Slot(val type: TroopType, val depth: Int, val row: Int)
    private val rows = 4
    private val cap = 24
    private lateinit var playerSlots: List<Slot>
    private lateinit var enemySlots: List<Slot>

    private class Proj(var x0: Float, var y0: Float, var x1: Float, var y1: Float, var age: Float, val dur: Float)
    private val arrows = ArrayList<Proj>()

    init {
        val myPower = army.power(game.chosenStrategy, c.terrain)
        val roll = 0.92f + Random.nextFloat() * 0.22f      // o'yinchi foydasiga biroz
        val effective = myPower * roll
        val enemy = c.enemyPower.coerceAtLeast(1f)
        val ratio = effective / enemy
        val win = effective >= enemy
        val lossPct = (enemy / (effective + 1f) * 40f).toInt().coerceIn(5, 90)
        val stars = when {
            !win -> 0
            ratio >= 1.4f -> 3
            ratio >= 1.1f -> 2
            else -> 1
        }
        result = BattleResult(win, effective, enemy, if (win) lossPct.coerceAtMost(55) else lossPct,
            game.chosenStrategy, stars)
        game.lastResult = result
        finalPlayerHp = if (win) (1f - result.playerLossPct / 100f).coerceIn(0.3f, 0.95f) else 0.16f
        finalEnemyHp = if (win) 0.05f else 0.42f

        playerSlots = buildFormation(visibleCounts(playerComposition()))
        enemySlots = buildFormation(visibleCounts(enemyComposition()))

        bContinue.onClick = { game.sound.confirm(); game.setScreen(ResultScreen(game)) }
        buttons.add(bContinue)
        bContinue.visible = false
    }

    override fun onResize(w: Int, h: Int) {
        val u = game.unit
        bContinue.set(w / 2f - u * 22f, h - u * 13f, w / 2f + u * 22f, h - u * 3f)
    }

    // ---- formatsiya ----
    private fun playerComposition(): Map<TroopType, Int> = army.units
    private fun enemyComposition(): Map<TroopType, Int> {
        val p = c.enemyPower
        return mapOf(
            TroopType.OTLIQ to (p * 0.30f).toInt(),
            TroopType.PIYODA to (p * 0.38f).toInt(),
            TroopType.KAMONCHI to (p * 0.20f).toInt(),
            TroopType.ZIRH to (p * 0.12f).toInt()
        )
    }

    private fun visibleCounts(src: Map<TroopType, Int>): Map<TroopType, Int> {
        val total = src.values.sum().coerceAtLeast(1)
        val out = LinkedHashMap<TroopType, Int>()
        var sum = 0
        for (type in listOf(TroopType.OTLIQ, TroopType.PIYODA, TroopType.ZIRH, TroopType.KAMONCHI, TroopType.QAMAL)) {
            val n = src[type] ?: 0
            if (n <= 0) continue
            var v = Math.round(n.toFloat() / total * cap)
            if (v < 1) v = 1
            out[type] = v; sum += v
        }
        // capdan oshib ketsa kamaytirish
        while (sum > cap) {
            val k = out.maxByOrNull { it.value }?.key ?: break
            out[k] = (out[k]!! - 1); if (out[k] == 0) out.remove(k); sum--
        }
        return out
    }

    /** old ustunga otliq/piyoda, orqaga kamonchi/qamal */
    private fun buildFormation(counts: Map<TroopType, Int>): List<Slot> {
        val ordered = ArrayList<TroopType>()
        for (type in listOf(TroopType.OTLIQ, TroopType.PIYODA, TroopType.ZIRH, TroopType.KAMONCHI, TroopType.QAMAL)) {
            repeat(counts[type] ?: 0) { ordered.add(type) }
        }
        return ordered.mapIndexed { i, type -> Slot(type, i / rows, i % rows) }
    }

    private fun aliveCount(slots: List<Slot>, hp: Float): Int {
        if (slots.isEmpty()) return 0
        return (slots.size * hp).toInt().coerceIn(if (hp > 0.02f) 1 else 0, slots.size)
    }

    override fun update(dt: Float) {
        t += dt
        if (t >= total && !done) {
            done = true; bContinue.visible = true
            if (result.win) game.sound.victory() else game.sound.defeat()
        }
        if (t in approach..total) {
            clashTick -= dt
            if (clashTick <= 0f) { game.sound.clash(); clashTick = 0.45f }
        }
        // o'q otish
        if (t in 0.3f..(approach + clash * 0.55f)) {
            arrowTick -= dt
            if (arrowTick <= 0f) { spawnArrows(); arrowTick = 0.28f }
        }
        val it = arrows.iterator()
        while (it.hasNext()) { val a = it.next(); a.age += dt; if (a.age > a.dur) it.remove() }
    }

    private fun spawnArrows() {
        val w = game.width; val h = game.height
        val midY = h * 0.55f
        repeat(2) {
            // o'yinchidan dushmanga
            arrows.add(Proj(currentFrontXp() - game.unit * 12f, midY + (Random.nextFloat() - 0.5f) * h * 0.4f,
                currentFrontXe() + game.unit * 6f, midY + (Random.nextFloat() - 0.5f) * h * 0.35f, 0f, 0.6f))
            // dushmandan o'yinchiga
            arrows.add(Proj(currentFrontXe() + game.unit * 12f, midY + (Random.nextFloat() - 0.5f) * h * 0.4f,
                currentFrontXp() - game.unit * 6f, midY + (Random.nextFloat() - 0.5f) * h * 0.35f, 0f, 0.6f))
        }
    }

    override fun onUp(x: Float, y: Float) { if (done) super.onUp(x, y) }
    override fun onBack(): Boolean { if (done) game.setScreen(ResultScreen(game)); return true }

    private fun ease(x: Float) = 1f - (1f - x) * (1f - x)
    private fun approachK() = ease((t / approach).coerceIn(0f, 1f))
    private fun currentFrontXp(): Float {
        val w = game.width
        val jitter = if (t in approach..total && !done) sin(t * 50f) * game.unit * 0.5f else 0f
        return (w * 0.5f - w * 0.42f) + (w * 0.28f) * approachK() + jitter
    }
    private fun currentFrontXe(): Float {
        val w = game.width
        val jitter = if (t in approach..total && !done) sin(t * 50f + 1f) * game.unit * 0.5f else 0f
        return (w * 0.5f + w * 0.42f) - (w * 0.28f) * approachK() + jitter
    }

    private fun hpNow(finalHp: Float): Float {
        if (t < approach) return 1f
        val k = ((t - approach) / clash).coerceIn(0f, 1f)
        return 1f + (finalHp - 1f) * ease(k)
    }

    override fun draw(canvas: Canvas) {
        val w = game.width; val h = game.height; val u = game.unit
        val midY = h * 0.55f

        // osmon va yer
        Ui.paint.shader = LinearGradient(0f, 0f, 0f, h.toFloat(),
            intArrayOf(0xFF2A2418.toInt(), 0xFF1A140C.toInt()), floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
        Ui.paint.style = Paint.Style.FILL
        canvas.drawRect(0f, h * 0.30f, w.toFloat(), h * 0.62f, Ui.paint)
        Ui.paint.shader = null
        Ui.paint.color = 0xFF221A0E.toInt()
        canvas.drawRect(0f, h * 0.62f, w.toFloat(), h.toFloat(), Ui.paint)
        // yer chizig'i
        Ui.paint.color = 0xFF2E2414.toInt()
        canvas.drawRect(0f, h * 0.62f, w.toFloat(), h * 0.64f, Ui.paint)

        Ui.centerText(canvas, c.title, w / 2f, u * 6f, u * 4f, Palette.GOLD_LIGHT, true)
        Ui.centerText(canvas, "Strategiya: ${game.chosenStrategy.uz}", w / 2f, u * 11f, u * 3f, Palette.GOLD)

        val playerHp = hpNow(finalPlayerHp)
        val enemyHp = hpNow(finalEnemyHp)
        val pAlive = aliveCount(playerSlots, playerHp)
        val eAlive = aliveCount(enemySlots, enemyHp)

        canvas.save()
        if (t in approach..total && !done) canvas.translate(sin(t * 55f) * u * 0.7f, 0f)

        val unitS = u * 4.6f
        val spX = u * 6.5f
        val spY = u * 7.5f
        val frontXp = currentFrontXp()
        val frontXe = currentFrontXe()

        // chizish tartibi: orqa qatorlar avval (depth katta), keyin old
        drawFormation(canvas, playerSlots, pAlive, frontXp, midY, unitS, spX, spY, +1, Palette.ROYAL)
        drawFormation(canvas, enemySlots, eAlive, frontXe, midY, unitS, spX, spY, -1, Palette.RED)

        // o'qlar
        for (a in arrows) {
            val k = (a.age / a.dur).coerceIn(0f, 1f)
            val x = a.x0 + (a.x1 - a.x0) * k
            val arc = -sin(k * Math.PI).toFloat() * u * 10f
            val y = a.y0 + (a.y1 - a.y0) * k + arc
            val nx = a.x0 + (a.x1 - a.x0) * (k + 0.02f)
            val ny = a.y0 + (a.y1 - a.y0) * (k + 0.02f) + (-sin((k + 0.02f) * Math.PI).toFloat() * u * 10f)
            Art.drawArrow(canvas, x, y, atan2(ny - y, nx - x), u * 4f, Palette.PARCHMENT)
        }

        // clash chang/uchqun
        if (t in approach..total) {
            Ui.paint.style = Paint.Style.FILL
            val clx = (frontXp + frontXe) / 2f
            for (i in 0 until 22) {
                val sx = clx + (Random.nextFloat() - 0.5f) * u * 18f
                val sy = midY + (Random.nextFloat() - 0.5f) * h * 0.45f
                Ui.paint.color = if (i % 3 == 0) Palette.GOLD_LIGHT else if (i % 3 == 1) 0xFFFF8A3D.toInt() else 0xAA8A6A3A.toInt()
                canvas.drawCircle(sx, sy, u * (0.4f + Random.nextFloat() * 1.1f), Ui.paint)
            }
        }
        canvas.restore()

        // bayroqlar + sog'liq
        drawBanner(canvas, "AMIR TEMUR QO'SHINI", playerHp, Palette.ROYAL, u * 4f, u * 15f, w * 0.42f, u, true)
        drawBanner(canvas, c.enemy, enemyHp, Palette.RED, w * 0.58f, u * 15f, w - u * 4f, u, false)

        if (done) {
            val msg = if (result.win) "G'ALABA!" else "MAG'LUBIYAT"
            Ui.glassPanel(canvas, RectF(w / 2f - u * 26f, h * 0.36f, w / 2f + u * 26f, h * 0.50f), u * 2f, 0xCC)
            Ui.centerText(canvas, msg, w / 2f, h * 0.43f, u * 8f,
                if (result.win) Palette.GOLD_LIGHT else Palette.RED, true)
            drawButtons(canvas)
        } else {
            Ui.centerText(canvas, "Jang davom etmoqda...", w / 2f, h - u * 5f, u * 3f, 0xFF8C7A4F.toInt())
        }
    }

    private fun drawFormation(canvas: Canvas, slots: List<Slot>, alive: Int, frontX: Float, midY: Float,
                              s: Float, spX: Float, spY: Float, dir: Int, cloth: Int) {
        // orqa qatordan oldinga chizamiz (depth katta -> kichik) to'g'ri qatlamlanish uchun
        val maxDepth = (slots.maxOfOrNull { it.depth } ?: 0)
        for (d in maxDepth downTo 0) {
            for (slot in slots) {
                if (slot.depth != d) continue
                val idx = slots.indexOf(slot)
                val x = frontX - dir * slot.depth * spX
                val y = midY + (slot.row - (rows - 1) / 2f) * spY + slot.depth * s * 0.15f
                Art.drawUnit(canvas, slot.type, x, y, s, cloth, dir, idx < alive)
            }
        }
    }

    private fun drawBanner(canvas: Canvas, name: String, hp: Float, color: Int,
                           left: Float, top: Float, right: Float, u: Float, alignLeft: Boolean) {
        val barRect = RectF(left, top + u * 5f, right, top + u * 8.5f)
        Ui.fill(canvas, barRect, 0xFF2A1E10.toInt(), u * 0.8f)
        val fillW = (barRect.width() - u * 0.8f) * hp.coerceIn(0f, 1f)
        val fill = RectF(barRect.left + u * 0.4f, barRect.top + u * 0.4f, barRect.left + u * 0.4f + fillW, barRect.bottom - u * 0.4f)
        Ui.fill(canvas, fill, color, u * 0.6f)
        Ui.border(canvas, barRect, Palette.GOLD, u * 0.35f, u * 0.8f)
        val nx = if (alignLeft) left else right - Ui.measure(name, u * 3.2f)
        Ui.leftText(canvas, name, nx, top + u * 3.5f, u * 3.2f, Palette.PARCHMENT, true)
        Ui.centerText(canvas, "${(hp * 100).toInt()}%", (barRect.left + barRect.right) / 2f, barRect.centerY(), u * 2.6f, Palette.PARCHMENT)
    }
}
