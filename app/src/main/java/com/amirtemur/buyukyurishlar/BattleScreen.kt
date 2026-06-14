package com.amirtemur.buyukyurishlar

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.random.Random

/**
 * Bosqichli (round) taktik jang: har bosqichda o'yinchi taktika tanlaydi,
 * taktikalar bir-birini "counter" qiladi. Jang dushman yoki o'yinchi
 * ruhiyati (morale) tugaguncha davom etadi — bu o'yinni strategik cho'zadi.
 */
class BattleScreen(game: Game) : Screen(game) {

    private val c = game.selectedCampaign!!
    private val army = game.playerArmy
    private val strategy = game.chosenStrategy

    private enum class Phase { INTRO, CHOOSE, CLASH, ROUNDEND, FINISH }
    private var phase = Phase.INTRO
    private var phaseT = 0f

    private var pMorale = 100f
    private var eMorale = 100f
    private var round = 1
    private val maxRounds = 6
    private val ratio: Float

    private var chosen: Tactic? = null
    private var enemyTactic: Tactic = Tactic.HUJUM
    private var dmgE = 0f
    private var dmgP = 0f
    private var applied = false
    private var resultStored = false

    private var clashTick = 0f

    private data class Slot(val type: TroopType, val depth: Int, val row: Int)
    private val rows = 4
    private val cap = 22
    private val playerSlots: List<Slot>
    private val enemySlots: List<Slot>

    private class Proj(var x0: Float, var y0: Float, var x1: Float, var y1: Float, var age: Float, val dur: Float)
    private val arrows = ArrayList<Proj>()

    private val tacticButtons = Tactic.values().map { Button(it.uz) }
    private val bContinue = Button("NATIJA ▸")

    init {
        ratio = (army.power(strategy, c.terrain) / c.enemyPower.coerceAtLeast(1f)).coerceIn(0.5f, 2.2f)
        playerSlots = buildFormation(visibleCounts(army.units))
        enemySlots = buildFormation(visibleCounts(enemyComposition()))

        Tactic.values().forEachIndexed { i, tac -> tacticButtons[i].onClick = { chooseTactic(tac) } }
        bContinue.onClick = { game.sound.confirm(); game.setScreen(ResultScreen(game)) }
        buttons.addAll(tacticButtons); buttons.add(bContinue)
        tacticButtons.forEach { it.visible = false }
        bContinue.visible = false
        game.sound.gong()
    }

    override fun onResize(w: Int, h: Int) {
        val u = game.unit
        val n = tacticButtons.size
        val pad = u * 2f
        val totalW = w - pad * 2f
        val bw = (totalW - pad * (n - 1)) / n
        val bh = u * 13f
        val y = h - bh - u * 2f
        tacticButtons.forEachIndexed { i, b -> b.set(pad + i * (bw + pad), y, pad + i * (bw + pad) + bw, y + bh) }
        bContinue.set(w / 2f - u * 22f, h - u * 13f, w / 2f + u * 22f, h - u * 3f)
    }

    // ---------------- formatsiya ----------------
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
            val nn = src[type] ?: 0
            if (nn <= 0) continue
            var v = Math.round(nn.toFloat() / total * cap); if (v < 1) v = 1
            out[type] = v; sum += v
        }
        while (sum > cap) { val k = out.maxByOrNull { it.value }?.key ?: break; out[k] = out[k]!! - 1; if (out[k] == 0) out.remove(k); sum-- }
        return out
    }
    private fun buildFormation(counts: Map<TroopType, Int>): List<Slot> {
        val ordered = ArrayList<TroopType>()
        for (type in listOf(TroopType.OTLIQ, TroopType.PIYODA, TroopType.ZIRH, TroopType.KAMONCHI, TroopType.QAMAL))
            repeat(counts[type] ?: 0) { ordered.add(type) }
        return ordered.mapIndexed { i, type -> Slot(type, i / rows, i % rows) }
    }
    private fun aliveCount(slots: List<Slot>, frac: Float): Int {
        if (slots.isEmpty()) return 0
        return (slots.size * frac).toInt().coerceIn(if (frac > 0.02f) 1 else 0, slots.size)
    }

    // ---------------- taktika ----------------
    private fun chooseTactic(tac: Tactic) {
        if (phase != Phase.CHOOSE) return
        chosen = tac
        enemyTactic = pickEnemyTactic()
        computeDamage(tac, enemyTactic)
        applied = false
        phase = Phase.CLASH; phaseT = 0f
        when (tac) {
            Tactic.HUJUM, Tactic.ZARBA, Tactic.QANOT -> game.sound.horn()
            Tactic.OQ -> game.sound.volley()
            Tactic.MUDOFAA -> game.sound.march()
        }
        if (tac == Tactic.OQ || enemyTactic == Tactic.OQ) spawnArrows()
    }

    private fun pickEnemyTactic(): Tactic {
        val r = Random.nextFloat()
        return when {
            r < 0.30f -> Tactic.HUJUM
            r < 0.50f -> Tactic.MUDOFAA
            r < 0.70f -> Tactic.QANOT
            r < 0.85f -> Tactic.OQ
            else -> Tactic.ZARBA
        }
    }

    private fun counter(att: Tactic, def: Tactic): Float = when {
        att == Tactic.QANOT && def == Tactic.HUJUM -> 1.35f
        att == Tactic.OQ && def == Tactic.HUJUM -> 1.25f
        att == Tactic.ZARBA && def == Tactic.MUDOFAA -> 1.20f
        att == Tactic.HUJUM && def == Tactic.QANOT -> 0.85f
        att == Tactic.HUJUM && def == Tactic.OQ -> 0.85f
        else -> 1f
    }

    private fun computeDamage(pt: Tactic, et: Tactic) {
        val atk = 22f
        val edgeP = 2f * ratio / (ratio + 1f)
        val edgeE = 2f / (ratio + 1f)
        dmgE = atk * edgeP * pt.atk * et.taken * counter(pt, et) * (0.85f + Random.nextFloat() * 0.3f)
        dmgP = atk * 0.92f * edgeE * et.atk * pt.taken * counter(et, pt) * (0.85f + Random.nextFloat() * 0.3f)
    }

    private fun applyDamage() {
        eMorale = (eMorale - dmgE).coerceAtLeast(0f)
        pMorale = (pMorale - dmgP + (chosen?.heal ?: 0f)).coerceIn(0f, 100f)
        game.sound.clash()
    }

    private fun spawnArrows() {
        val w = game.width; val h = game.height; val midY = h * 0.55f
        repeat(3) {
            arrows.add(Proj(frontXp() - game.unit * 12f, midY + (Random.nextFloat() - 0.5f) * h * 0.4f,
                frontXe() + game.unit * 6f, midY + (Random.nextFloat() - 0.5f) * h * 0.35f, 0f, 0.6f))
            arrows.add(Proj(frontXe() + game.unit * 12f, midY + (Random.nextFloat() - 0.5f) * h * 0.4f,
                frontXp() - game.unit * 6f, midY + (Random.nextFloat() - 0.5f) * h * 0.35f, 0f, 0.6f))
        }
    }

    private fun enterFinish() {
        phase = Phase.FINISH; phaseT = 0f
        if (!resultStored) {
            resultStored = true
            val win = pMorale > 0f && (eMorale <= 0f || pMorale >= eMorale)
            val lossPct = (100f - pMorale).toInt().coerceIn(0, 100)
            val rating = when { !win -> 0; pMorale >= 60 -> 3; pMorale >= 30 -> 2; else -> 1 }
            val res = BattleResult(win, army.power(strategy, c.terrain), c.enemyPower, lossPct, strategy, rating)
            game.lastResult = res
            if (win) game.sound.fanfare() else game.sound.defeat()
        }
        bContinue.visible = true
    }

    override fun update(dt: Float) {
        phaseT += dt
        when (phase) {
            Phase.INTRO -> if (phaseT > 1.0f) { phase = Phase.CHOOSE; phaseT = 0f }
            Phase.CHOOSE -> {}
            Phase.CLASH -> {
                clashTick -= dt
                if (clashTick <= 0f) { game.sound.clash(); clashTick = 0.5f }
                if (phaseT >= 1.2f && !applied) { applied = true; applyDamage() }
                if (phaseT >= 2.6f) { phase = Phase.ROUNDEND; phaseT = 0f }
            }
            Phase.ROUNDEND -> if (phaseT > 1.2f) {
                if (eMorale <= 0f || pMorale <= 0f || round >= maxRounds) enterFinish()
                else { round++; phase = Phase.CHOOSE; phaseT = 0f }
            }
            Phase.FINISH -> {}
        }
        tacticButtons.forEach { it.visible = phase == Phase.CHOOSE }
        bContinue.visible = phase == Phase.FINISH
        val it = arrows.iterator()
        while (it.hasNext()) { val a = it.next(); a.age += dt; if (a.age > a.dur) it.remove() }
    }

    override fun onUp(x: Float, y: Float) { super.onUp(x, y) }
    override fun onBack(): Boolean { if (phase == Phase.FINISH) game.setScreen(ResultScreen(game)); return true }

    private fun ease(x: Float) = 1f - (1f - x) * (1f - x)
    private fun clashAdvance(): Float {
        if (phase != Phase.CLASH) return 0f
        val k = (phaseT / 2.6f).coerceIn(0f, 1f)
        return sin(k * Math.PI).toFloat() * game.width * 0.15f
    }
    private fun frontXp() = game.width * 0.31f + clashAdvance()
    private fun frontXe() = game.width * 0.69f - clashAdvance()

    override fun draw(canvas: Canvas) {
        val w = game.width; val h = game.height; val u = game.unit
        val midY = h * 0.55f

        // osmon va yer
        Ui.paint.shader = LinearGradient(0f, 0f, 0f, h.toFloat(),
            intArrayOf(0xFF2A2418.toInt(), 0xFF17110A.toInt()), floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
        Ui.paint.style = Paint.Style.FILL
        canvas.drawRect(0f, h * 0.28f, w.toFloat(), h * 0.62f, Ui.paint)
        Ui.paint.shader = null
        Ui.paint.color = 0xFF221A0E.toInt(); canvas.drawRect(0f, h * 0.62f, w.toFloat(), h.toFloat(), Ui.paint)
        Ui.paint.color = 0xFF2E2414.toInt(); canvas.drawRect(0f, h * 0.62f, w.toFloat(), h * 0.635f, Ui.paint)

        Ui.centerText(canvas, c.title, w / 2f, u * 5.5f, u * 3.8f, Palette.GOLD_LIGHT, true)
        Ui.centerText(canvas, "Bosqich $round / $maxRounds  •  Strategiya: ${strategy.uz}", w / 2f, u * 10f, u * 2.9f, Palette.GOLD)

        val pAlive = aliveCount(playerSlots, pMorale / 100f)
        val eAlive = aliveCount(enemySlots, eMorale / 100f)

        canvas.save()
        if (phase == Phase.CLASH && phaseT in 1.0f..2.4f) canvas.translate(sin(phaseT * 55f) * u * 0.7f, 0f)
        val s = u * 4.4f
        drawFormation(canvas, playerSlots, pAlive, frontXp(), midY, s, u * 6.2f, u * 7.2f, +1, Palette.ROYAL)
        drawFormation(canvas, enemySlots, eAlive, frontXe(), midY, s, u * 6.2f, u * 7.2f, -1, Palette.RED)

        for (a in arrows) {
            val k = (a.age / a.dur).coerceIn(0f, 1f)
            val x = a.x0 + (a.x1 - a.x0) * k
            val y = a.y0 + (a.y1 - a.y0) * k - sin(k * Math.PI).toFloat() * u * 10f
            val k2 = (k + 0.02f)
            val nx = a.x0 + (a.x1 - a.x0) * k2
            val ny = a.y0 + (a.y1 - a.y0) * k2 - sin(k2 * Math.PI).toFloat() * u * 10f
            Art.drawArrow(canvas, x, y, atan2(ny - y, nx - x), u * 4f, Palette.PARCHMENT)
        }

        if (phase == Phase.CLASH && phaseT > 1.0f) {
            Ui.paint.style = Paint.Style.FILL
            val clx = (frontXp() + frontXe()) / 2f
            for (i in 0 until 20) {
                val sx = clx + (Random.nextFloat() - 0.5f) * u * 18f
                val sy = midY + (Random.nextFloat() - 0.5f) * h * 0.42f
                Ui.paint.color = if (i % 3 == 0) Palette.GOLD_LIGHT else if (i % 3 == 1) 0xFFFF8A3D.toInt() else 0xAA8A6A3A.toInt()
                canvas.drawCircle(sx, sy, u * (0.4f + Random.nextFloat() * 1.1f), Ui.paint)
            }
        }
        canvas.restore()

        // bayroqlar (morale)
        drawBanner(canvas, "AMIR TEMUR QO'SHINI", pMorale / 100f, Palette.ROYAL, u * 4f, u * 13.5f, w * 0.42f, u, true)
        drawBanner(canvas, c.enemy, eMorale / 100f, Palette.RED, w * 0.58f, u * 13.5f, w - u * 4f, u, false)

        when (phase) {
            Phase.INTRO -> Ui.centerText(canvas, "$round-bosqich!", w / 2f, midY, u * 8f, Palette.GOLD_LIGHT, true)
            Phase.CHOOSE -> drawChoose(canvas, w, h, u)
            Phase.CLASH -> drawClashInfo(canvas, w, h, u)
            Phase.ROUNDEND -> Ui.centerText(canvas, "Bosqich yakunlandi", w / 2f, h - u * 18f, u * 3.4f, Palette.GOLD)
            Phase.FINISH -> drawFinish(canvas, w, h, u)
        }
    }

    private fun drawChoose(canvas: Canvas, w: Int, h: Int, u: Float) {
        Ui.glassPanel(canvas, RectF(w * 0.18f, h - u * 30f, w * 0.82f, h - u * 16.5f), u * 1.5f, 0xAA)
        Ui.centerText(canvas, "Taktikani tanlang", w / 2f, h - u * 26f, u * 3.4f, Palette.GOLD_LIGHT)
        Ui.centerText(canvas, "Maslahat: Qanot — dushman hujumini, O'q — hujumchini yaxshi yengadi.",
            w / 2f, h - u * 21.5f, u * 2.7f, Palette.PARCHMENT)
        Ui.centerText(canvas, "Maglubiyatdan saqlaning: Mudofaa kam zarar oladi va qo'shinni tiklaydi.",
            w / 2f, h - u * 18f, u * 2.7f, 0xFFB8A06A.toInt())
        // taktika tugmalari (kichik tavsif bilan)
        tacticButtons.forEachIndexed { i, b ->
            Ui.woodButton(canvas, b, u)
        }
    }

    private fun drawClashInfo(canvas: Canvas, w: Int, h: Int, u: Float) {
        Ui.centerText(canvas, "Siz: ${chosen?.uz}   ⚔   Dushman: ${enemyTactic.uz}",
            w / 2f, h - u * 6f, u * 3f, Palette.PARCHMENT)
        if (applied) {
            val rise = ((phaseT - 1.2f) * u * 5f)
            Ui.centerText(canvas, "-${dmgE.toInt()}", w * 0.70f, h * 0.30f - rise, u * 4.5f, Palette.GOLD_LIGHT, true)
            Ui.centerText(canvas, "-${dmgP.toInt()}", w * 0.30f, h * 0.30f - rise, u * 4.5f, Palette.RED, true)
        }
    }

    private fun drawFinish(canvas: Canvas, w: Int, h: Int, u: Float) {
        val win = game.lastResult?.win == true
        Ui.glassPanel(canvas, RectF(w / 2f - u * 28f, h * 0.34f, w / 2f + u * 28f, h * 0.52f), u * 2f, 0xCC)
        Ui.centerText(canvas, if (win) "G'ALABA!" else "MAG'LUBIYAT", w / 2f, h * 0.40f, u * 8f,
            if (win) Palette.GOLD_LIGHT else Palette.RED, true)
        Ui.centerText(canvas, "Qolgan ruhiyat: ${pMorale.toInt()}%", w / 2f, h * 0.475f, u * 3.2f, Palette.PARCHMENT)
        drawButtons(canvas)
    }

    private fun drawFormation(canvas: Canvas, slots: List<Slot>, alive: Int, frontX: Float, midY: Float,
                              s: Float, spX: Float, spY: Float, dir: Int, cloth: Int) {
        val maxDepth = (slots.maxOfOrNull { it.depth } ?: 0)
        for (d in maxDepth downTo 0) {
            for (i in slots.indices) {
                val slot = slots[i]
                if (slot.depth != d) continue
                val x = frontX - dir * slot.depth * spX
                val y = midY + (slot.row - (rows - 1) / 2f) * spY + slot.depth * s * 0.15f
                Art.drawUnit(canvas, slot.type, x, y, s, cloth, dir, i < alive)
            }
        }
    }

    private fun drawBanner(canvas: Canvas, name: String, frac: Float, color: Int,
                           left: Float, top: Float, right: Float, u: Float, alignLeft: Boolean) {
        val barRect = RectF(left, top + u * 5f, right, top + u * 8.5f)
        Ui.fill(canvas, barRect, 0xFF2A1E10.toInt(), u * 0.8f)
        val fillW = (barRect.width() - u * 0.8f) * frac.coerceIn(0f, 1f)
        Ui.fill(canvas, RectF(barRect.left + u * 0.4f, barRect.top + u * 0.4f, barRect.left + u * 0.4f + fillW, barRect.bottom - u * 0.4f), color, u * 0.6f)
        Ui.border(canvas, barRect, Palette.GOLD, u * 0.35f, u * 0.8f)
        val nx = if (alignLeft) left else right - Ui.measure(name, u * 3.2f)
        Ui.leftText(canvas, name, nx, top + u * 3.5f, u * 3.2f, Palette.PARCHMENT, true)
        Ui.centerText(canvas, "${(frac * 100).toInt()}%", barRect.centerX(), barRect.centerY(), u * 2.6f, Palette.PARCHMENT)
    }
}
