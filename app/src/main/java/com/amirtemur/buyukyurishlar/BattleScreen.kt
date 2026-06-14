package com.amirtemur.buyukyurishlar

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.sin
import kotlin.random.Random

/**
 * Real vaqtli (10–15 soniya) jang. 2.5D (pseudo-3D) maydon.
 * Ekranni bosib kamondan o'q uzasiz; har otishdan keyin 2 soniya qayta o'qlash.
 * Qo'shin va strategiya asosiy kuchni, kamon mahorati esa qo'shimcha ustunlikni beradi.
 */
class BattleScreen(game: Game) : Screen(game) {

    private val c = game.selectedCampaign!!
    private val army = game.playerArmy
    private val strategy = game.chosenStrategy

    private val duration = 13f
    private var t = 0f
    private var pMorale = 100f
    private var eMorale = 100f
    private val ratio: Float
    private val eDrain: Float
    private val pDrain: Float

    private val reloadTime = 2f
    private var reload = 0f
    private var aimX = 0f
    private var shots = 0
    private var hits = 0

    private var finished = false
    private var resultStored = false
    private var clashTick = 0f
    private var ambientTick = 1.2f

    private val bContinue = Button("NATIJA ▸")

    private class Proj(val x0: Float, val y0: Float, val x1: Float, val y1: Float,
                       var age: Float, val dur: Float, val fromEnemy: Boolean, val dmg: Float, var applied: Boolean = false)
    private val arrows = ArrayList<Proj>()
    private class Spark(val x: Float, val y: Float, var age: Float)
    private val sparks = ArrayList<Spark>()

    private class Tok(val type: TroopType, val col: Int, val row: Int, val cols: Int)
    private val enemyToks: List<Tok>
    private val playerToks: List<Tok>

    init {
        ratio = (army.power(strategy, c.terrain) / c.enemyPower.coerceAtLeast(1f)).coerceIn(0.5f, 2.3f)
        val edgeP = 2f * ratio / (ratio + 1f)
        val edgeE = 2f / (ratio + 1f)
        eDrain = 6.4f * edgeP
        pDrain = 5.8f * edgeE
        enemyToks = build(enemyComposition(), 6, 3)
        playerToks = build(army.units, 7, 2)
        bContinue.onClick = { game.sound.confirm(); game.setScreen(ResultScreen(game)) }
        buttons.add(bContinue); bContinue.visible = false
        game.sound.horn()
    }

    override fun onShow() { aimX = game.width / 2f }
    override fun onResize(w: Int, h: Int) {
        aimX = w / 2f
        val u = game.unit
        bContinue.set(w / 2f - u * 22f, h - u * 13f, w / 2f + u * 22f, h - u * 3f)
    }

    private fun enemyComposition(): Map<TroopType, Int> {
        val p = c.enemyPower
        return mapOf(
            TroopType.OTLIQ to (p * 0.30f).toInt(), TroopType.PIYODA to (p * 0.40f).toInt(),
            TroopType.KAMONCHI to (p * 0.18f).toInt(), TroopType.ZIRH to (p * 0.12f).toInt()
        )
    }
    private fun build(src: Map<TroopType, Int>, cols: Int, rows: Int): List<Tok> {
        val cap = cols * rows
        val total = src.values.sum().coerceAtLeast(1)
        val ordered = ArrayList<TroopType>()
        for (type in listOf(TroopType.OTLIQ, TroopType.PIYODA, TroopType.ZIRH, TroopType.KAMONCHI, TroopType.QAMAL)) {
            var v = Math.round((src[type] ?: 0).toFloat() / total * cap)
            if ((src[type] ?: 0) > 0 && v < 1) v = 1
            repeat(v) { if (ordered.size < cap) ordered.add(type) }
        }
        return ordered.mapIndexed { i, type -> Tok(type, i % cols, i / cols, cols) }
    }
    private fun aliveOf(list: List<Tok>, frac: Float) =
        if (list.isEmpty()) 0 else (list.size * frac).toInt().coerceIn(if (frac > 0.02f) 1 else 0, list.size)

    override fun onDown(x: Float, y: Float) {
        if (!finished) {
            aimX = x
            if (reload <= 0f) fire(x, y)
        } else super.onDown(x, y)
    }
    override fun onUp(x: Float, y: Float) { if (finished) super.onUp(x, y) }
    override fun onBack(): Boolean { if (finished) game.setScreen(ResultScreen(game)); return true }

    private fun fire(tx: Float, ty: Float) {
        val w = game.width; val h = game.height
        val targetY = ty.coerceIn(h * 0.34f, h * 0.6f)
        arrows.add(Proj(w / 2f, h * 0.9f, tx, targetY, 0f, 0.5f, false, 8f))
        reload = reloadTime; shots++
        game.sound.volley()
    }

    override fun update(dt: Float) {
        if (!finished) {
            t += dt
            reload = (reload - dt).coerceAtLeast(0f)
            pMorale = (pMorale - pDrain * dt).coerceAtLeast(0f)
            eMorale = (eMorale - eDrain * dt).coerceAtLeast(0f)
            clashTick -= dt
            if (clashTick <= 0f) { game.sound.clash(); clashTick = 0.7f }
            ambientTick -= dt
            if (ambientTick <= 0f) { spawnEnemyArrow(); ambientTick = 1.1f + Random.nextFloat() }
            if (t >= duration || eMorale <= 0f || pMorale <= 0f) finish()
        }
        // arrows
        val it = arrows.iterator()
        while (it.hasNext()) {
            val a = it.next(); a.age += dt
            if (!a.applied && a.age >= a.dur * 0.95f) {
                a.applied = true
                if (a.fromEnemy) pMorale = (pMorale - a.dmg).coerceAtLeast(0f)
                else { eMorale = (eMorale - a.dmg).coerceAtLeast(0f); hits++ }
                sparks.add(Spark(a.x1, a.y1, 0f))
            }
            if (a.age > a.dur + 0.2f) it.remove()
        }
        val si = sparks.iterator()
        while (si.hasNext()) { val s = si.next(); s.age += dt; if (s.age > 0.4f) si.remove() }
        bContinue.visible = finished
    }

    private fun spawnEnemyArrow() {
        val w = game.width; val h = game.height
        arrows.add(Proj(w * (0.3f + Random.nextFloat() * 0.4f), h * 0.45f,
            w * (0.35f + Random.nextFloat() * 0.3f), h * 0.82f, 0f, 0.7f, true, 1.5f))
    }

    private fun finish() {
        if (finished) return
        finished = true
        if (!resultStored) {
            resultStored = true
            val win = pMorale > 0f && (eMorale <= 0f || pMorale >= eMorale)
            val lossPct = (100f - pMorale).toInt().coerceIn(0, 100)
            val rating = when { !win -> 0; pMorale >= 60 -> 3; pMorale >= 30 -> 2; else -> 1 }
            game.lastResult = BattleResult(win, army.power(strategy, c.terrain), c.enemyPower, lossPct, strategy, rating)
            if (win) game.sound.fanfare() else game.sound.defeat()
        }
        bContinue.visible = true
    }

    override fun draw(canvas: Canvas) {
        val w = game.width; val h = game.height; val u = game.unit
        val horizon = h * 0.30f

        // === 2.5D maydon ===
        // osmon
        Ui.paint.style = Paint.Style.FILL
        Ui.paint.shader = LinearGradient(0f, 0f, 0f, horizon,
            intArrayOf(0xFF3A4A66.toInt(), 0xFF8A7A55.toInt()), null, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w.toFloat(), horizon, Ui.paint)
        Ui.paint.shader = null
        // quyosh
        Art.glow(canvas, w * 0.75f, horizon * 0.55f, u * 22f, 0xFFFFE08A.toInt(), 130)
        Ui.paint.color = 0xFFFFE7A6.toInt(); canvas.drawCircle(w * 0.75f, horizon * 0.55f, u * 6f, Ui.paint)
        // tog'lar
        Ui.paint.color = 0xFF2A2236.toInt()
        val mt = Path().apply {
            moveTo(0f, horizon)
            lineTo(w * 0.2f, horizon - u * 10f); lineTo(w * 0.4f, horizon)
            lineTo(w * 0.6f, horizon - u * 14f); lineTo(w * 0.8f, horizon); lineTo(w.toFloat(), horizon - u * 8f)
            lineTo(w.toFloat(), horizon); close()
        }
        canvas.drawPath(mt, Ui.paint)
        // yer (perspektiv gradient)
        Ui.paint.shader = LinearGradient(0f, horizon, 0f, h.toFloat(),
            intArrayOf(0xFF5A6B3A.toInt(), 0xFF2A2412.toInt()), null, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, horizon, w.toFloat(), h.toFloat(), Ui.paint)
        Ui.paint.shader = null
        // perspektiv chiziqlar
        Ui.stroke.color = 0x22000000; Ui.stroke.strokeWidth = u * 0.3f
        for (i in 0..6) {
            val fx = w * i / 6f
            canvas.drawLine(w / 2f, horizon, fx, h.toFloat(), Ui.stroke)
        }

        val eAlive = aliveOf(enemyToks, eMorale / 100f)
        val pAlive = aliveOf(playerToks, pMorale / 100f)
        val advance = (t / duration) * h * 0.05f

        // dushman (uzoqda, kichik) — orqa qatordan oldinga
        for (row in 2 downTo 0) {
            for (i in enemyToks.indices) {
                val tk = enemyToks[i]; if (tk.row != row) continue
                val s = u * (3.0f + row * 0.9f)
                val y = horizon + u * 7f + row * u * 7f + advance
                val x = w / 2f + (tk.col - (tk.cols - 1) / 2f) * u * (9f + row * 1.2f)
                if (i < eAlive) Art.trooper(canvas, x, y, s, Palette.RED, true, tk.type)
                else Art.shadow(canvas, x, y, s * 0.4f, s * 0.12f)
            }
        }

        // clash uchqun chizig'i
        if (t > 0.5f) {
            Ui.paint.style = Paint.Style.FILL
            for (i in 0 until 12) {
                val sx = w / 2f + (Random.nextFloat() - 0.5f) * w * 0.5f
                val sy = h * 0.55f + (Random.nextFloat() - 0.5f) * h * 0.08f
                Ui.paint.color = if (i % 2 == 0) Palette.GOLD_LIGHT else 0xFFFF8A3D.toInt()
                canvas.drawCircle(sx, sy, u * (0.4f + Random.nextFloat() * 0.9f), Ui.paint)
            }
        }

        // o'yinchi qo'shini (oldinda, katta, orqadan)
        for (row in 0..1) {
            for (i in playerToks.indices) {
                val tk = playerToks[i]; if (tk.row != row) continue
                val s = u * (6.2f - row * 1.0f)
                val y = h * 0.70f + row * u * 8f
                val x = w / 2f + (tk.col - (tk.cols - 1) / 2f) * u * 12f
                if (i < pAlive) Art.trooper(canvas, x, y, s, Palette.ROYAL, false, tk.type)
            }
        }

        // o'qlar
        for (a in arrows) {
            val k = (a.age / a.dur).coerceIn(0f, 1f)
            val x = a.x0 + (a.x1 - a.x0) * k
            val arc = -sin(k * Math.PI).toFloat() * (game.height * 0.12f)
            val y = a.y0 + (a.y1 - a.y0) * k + arc
            val k2 = (k + 0.03f)
            val nx = a.x0 + (a.x1 - a.x0) * k2
            val ny = a.y0 + (a.y1 - a.y0) * k2 + (-sin(k2 * Math.PI).toFloat() * game.height * 0.12f)
            Art.drawArrow(canvas, x, y, kotlin.math.atan2(ny - y, nx - x), u * 4.5f, Palette.PARCHMENT)
        }
        // impact sparks
        Ui.paint.style = Paint.Style.FILL
        for (s in sparks) {
            Ui.paint.color = Palette.GOLD_LIGHT; Ui.paint.alpha = ((1f - s.age / 0.4f) * 255).toInt().coerceIn(0, 255)
            canvas.drawCircle(s.x, s.y, u * (1f + s.age * 8f), Ui.paint)
        }
        Ui.paint.alpha = 255

        // qahramon kamonchi (past markaz)
        val pull = (1f - reload / reloadTime).coerceIn(0f, 1f)
        Art.heroBow(canvas, w / 2f, h * 0.92f, u * 9f, pull, aimX - w / 2f)

        // === HUD ===
        Ui.centerText(canvas, c.title, w / 2f, u * 5f, u * 3.6f, Palette.GOLD_LIGHT, true)
        drawBar(canvas, "SIZ", pMorale / 100f, Palette.ROYAL, u * 3f, u * 9f, w * 0.38f, u, true)
        drawBar(canvas, c.enemy, eMorale / 100f, Palette.RED, w * 0.62f, u * 9f, w - u * 3f, u, false)

        // taymer
        val left = (duration - t).coerceAtLeast(0f)
        Ui.centerText(canvas, "${left.toInt() + if (left > 0f) 1 else 0}", w / 2f, u * 9f, u * 6f, Palette.GOLD_LIGHT, true)

        // qayta o'qlash ko'rsatkichi
        val rb = RectF(w / 2f - u * 16f, h - u * 4.5f, w / 2f + u * 16f, h - u * 2f)
        Ui.fill(canvas, rb, 0xAA000000.toInt(), u)
        if (reload > 0f) {
            Ui.fill(canvas, RectF(rb.left, rb.top, rb.left + rb.width() * (1f - reload / reloadTime), rb.bottom), Palette.GOLD_DEEP, u)
            Ui.centerText(canvas, "Qayta o'qlanmoqda...", rb.centerX(), rb.centerY(), u * 2.6f, Palette.PARCHMENT)
        } else {
            Ui.fill(canvas, rb, Palette.GREEN, u)
            Ui.centerText(canvas, "BOSIB O'Q UZING!", rb.centerX(), rb.centerY(), u * 2.8f, Palette.PARCHMENT)
        }
        Ui.centerText(canvas, "O'q: $shots  •  Nishon: $hits", w / 2f, h - u * 6f, u * 2.6f, 0xFFB8A06A.toInt())

        if (finished) {
            val win = game.lastResult?.win == true
            Ui.glassPanel(canvas, RectF(w / 2f - u * 28f, h * 0.36f, w / 2f + u * 28f, h * 0.54f), u * 2f, 0xCC)
            Ui.centerText(canvas, if (win) "G'ALABA!" else "MAG'LUBIYAT", w / 2f, h * 0.43f, u * 8f,
                if (win) Palette.GOLD_LIGHT else Palette.RED, true)
            Ui.centerText(canvas, "Qolgan kuch: ${pMorale.toInt()}%   Nishonga: $hits o'q", w / 2f, h * 0.50f, u * 3f, Palette.PARCHMENT)
            drawButtons(canvas)
        }
    }

    private fun drawBar(canvas: Canvas, name: String, frac: Float, color: Int,
                        left: Float, top: Float, right: Float, u: Float, alignLeft: Boolean) {
        val bar = RectF(left, top + u * 3f, right, top + u * 6f)
        Ui.fill(canvas, bar, 0xFF2A1E10.toInt(), u * 0.8f)
        val fw = (bar.width() - u * 0.8f) * frac.coerceIn(0f, 1f)
        Ui.fill(canvas, RectF(bar.left + u * 0.4f, bar.top + u * 0.4f, bar.left + u * 0.4f + fw, bar.bottom - u * 0.4f), color, u * 0.6f)
        Ui.border(canvas, bar, Palette.GOLD, u * 0.3f, u * 0.8f)
        val nx = if (alignLeft) left else right - Ui.measure(name, u * 2.8f)
        Ui.leftText(canvas, name, nx, top + u * 2f, u * 2.8f, Palette.PARCHMENT, true)
    }
}
