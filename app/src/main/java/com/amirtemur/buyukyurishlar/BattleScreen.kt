package com.amirtemur.buyukyurishlar

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.math.sin
import kotlin.random.Random

class BattleScreen(game: Game) : Screen(game) {

    private val c = game.selectedCampaign!!
    private val army = game.playerArmy
    private val result: BattleResult

    private val approach = 1.0f
    private val clash = 2.8f
    private val total = approach + clash
    private var t = 0f
    private var done = false
    private var clashTick = 0f

    private val bContinue = Button("NATIJA ▸")

    private val finalPlayerHp: Float
    private val finalEnemyHp: Float

    init {
        // natijani hisoblash
        val myPower = army.power(game.chosenStrategy, c.terrain)
        val roll = 0.85f + Random.nextFloat() * 0.3f
        val effective = myPower * roll
        val enemy = c.enemyPower.coerceAtLeast(1f)
        val ratio = effective / enemy
        val win = effective >= enemy
        val lossPct = (enemy / (effective + 1f) * 45f).toInt().coerceIn(5, 95)
        val stars = when {
            !win -> 0
            ratio >= 1.4f -> 3
            ratio >= 1.1f -> 2
            else -> 1
        }
        result = BattleResult(win, effective, enemy, if (win) lossPct.coerceAtMost(60) else lossPct,
            game.chosenStrategy, stars)
        game.lastResult = result

        finalPlayerHp = if (win) (1f - result.playerLossPct / 100f).coerceIn(0.25f, 0.95f) else 0.18f
        finalEnemyHp = if (win) 0.06f else 0.45f

        bContinue.onClick = {
            game.sound.confirm()
            game.setScreen(ResultScreen(game))
        }
        buttons.add(bContinue)
        bContinue.visible = false
    }

    override fun onResize(w: Int, h: Int) {
        val u = game.unit
        bContinue.set(w / 2f - u * 20f, h - u * 13f, w / 2f + u * 20f, h - u * 3f)
    }

    override fun update(dt: Float) {
        t += dt
        if (t >= total && !done) { done = true; bContinue.visible = true; game.sound.let { if (result.win) it.victory() else it.defeat() } }
        if (t in approach..total) {
            clashTick -= dt
            if (clashTick <= 0f) { game.sound.clash(); clashTick = 0.4f }
        }
    }

    override fun onUp(x: Float, y: Float) {
        if (done) { super.onUp(x, y) }
    }

    override fun onBack(): Boolean { if (done) { game.setScreen(ResultScreen(game)); return true }; return true }

    private fun hpNow(finalHp: Float): Float {
        if (t < approach) return 1f
        val k = ((t - approach) / clash).coerceIn(0f, 1f)
        return 1f + (finalHp - 1f) * easeOut(k)
    }

    private fun easeOut(x: Float) = 1f - (1f - x) * (1f - x)

    override fun draw(canvas: Canvas) {
        val w = game.width; val h = game.height; val u = game.unit

        // jang maydoni (yer)
        Ui.paint.style = Paint.Style.FILL
        Ui.paint.color = 0xFF1C140A.toInt()
        canvas.drawRect(0f, h * 0.62f, w.toFloat(), h.toFloat(), Ui.paint)

        // sarlavha
        Ui.centerText(canvas, c.title, w / 2f, u * 6f, u * 4f, Palette.GOLD_LIGHT)
        Ui.centerText(canvas, "Strategiya: ${game.chosenStrategy.uz}", w / 2f, u * 11f, u * 3.2f, Palette.GOLD)

        val approachK = (t / approach).coerceIn(0f, 1f)
        val centerGap = w * 0.30f * (1f - approachK)

        val playerHp = hpNow(finalPlayerHp)
        val enemyHp = hpNow(finalEnemyHp)

        // ekran silkinishi (clash paytida)
        canvas.save()
        if (t in approach..total && !done) {
            val shake = u * 0.8f * sin(t * 60f)
            canvas.translate(shake, 0f)
        }

        // o'yinchi qo'shini (chap, oltin)
        val pBaseX = w * 0.30f - centerGap
        drawArmy(canvas, pBaseX, h * 0.50f, u, Palette.GOLD, army.totalSoldiers(), playerHp, true)
        // dushman (o'ng, qizil)
        val eBaseX = w * 0.70f + centerGap
        val enemyCount = (c.enemyPower / 30f).toInt().coerceAtLeast(20)
        drawArmy(canvas, eBaseX, h * 0.50f, u, Palette.RED, enemyCount, enemyHp, false)

        // clash uchqunlari
        if (t in approach..total) {
            val p = Ui.paint
            p.style = Paint.Style.FILL
            for (i in 0 until 18) {
                val sx = w / 2f + Random.nextFloat() * u * 16f - u * 8f
                val sy = h * 0.50f + Random.nextFloat() * u * 16f - u * 8f
                p.color = if (i % 2 == 0) Palette.GOLD_LIGHT else 0xFFFF884D.toInt()
                canvas.drawCircle(sx, sy, u * (0.4f + Random.nextFloat() * 1.0f), p)
            }
        }
        canvas.restore()

        // bayroqlar + sog'liq panellari
        drawBanner(canvas, "AMIR TEMUR QO'SHINI", playerHp, Palette.GOLD, u * 4f, u * 16f, w * 0.40f, u, true)
        drawBanner(canvas, c.enemy, enemyHp, Palette.RED, w * 0.60f, u * 16f, w - u * 4f, u, false)

        if (done) {
            val msg = if (result.win) "G'ALABA!" else "MAG'LUBIYAT"
            Ui.centerText(canvas, msg, w / 2f, h * 0.44f, u * 9f,
                if (result.win) Palette.GOLD_LIGHT else Palette.RED)
            drawButtons(canvas)
        } else {
            Ui.centerText(canvas, "Jang davom etmoqda...", w / 2f, h - u * 6f, u * 3f, 0xFF8C7A4F.toInt())
        }
    }

    private fun drawArmy(canvas: Canvas, cx: Float, cy: Float, u: Float, color: Int,
                         count: Int, hp: Float, faceRight: Boolean) {
        val icons = (count.coerceAtMost(2000) / 60 + 6).coerceIn(6, 36)
        val alive = (icons * hp).toInt().coerceAtLeast(if (hp > 0.02f) 1 else 0)
        val cols = 6
        val sp = u * 4.2f
        val p = Ui.paint
        p.style = Paint.Style.FILL
        for (i in 0 until alive) {
            val col = i % cols
            val row = i / cols
            val dir = if (faceRight) 1 else -1
            val x = cx + dir * col * sp - (if (faceRight) 0f else 0f)
            val y = cy + row * sp - (alive / cols) * sp / 2f
            // tana
            p.color = color
            canvas.drawRoundRect(RectF(x - u * 1.2f, y, x + u * 1.2f, y + u * 4f), u * 0.6f, u * 0.6f, p)
            // bosh
            p.color = Palette.PARCHMENT
            canvas.drawCircle(x, y - u * 1.2f, u * 1.1f, p)
            // qilich/nayza
            p.color = Palette.GOLD_LIGHT
            p.strokeWidth = u * 0.4f
            canvas.drawLine(x + dir * u * 1.5f, y - u * 2f, x + dir * u * 1.5f, y + u * 3f, p.apply { style = Paint.Style.STROKE })
            p.style = Paint.Style.FILL
        }
    }

    private fun drawBanner(canvas: Canvas, name: String, hp: Float, color: Int,
                           left: Float, top: Float, right: Float, u: Float, alignLeft: Boolean) {
        val barRect = RectF(left, top + u * 5f, right, top + u * 8.5f)
        Ui.fill(canvas, barRect, 0xFF2A1E10.toInt(), u * 0.8f)
        val fill = RectF(barRect.left + u * 0.4f, barRect.top + u * 0.4f,
            barRect.left + u * 0.4f + (barRect.width() - u * 0.8f) * hp.coerceIn(0f, 1f),
            barRect.bottom - u * 0.4f)
        Ui.fill(canvas, fill, color, u * 0.6f)
        Ui.border(canvas, barRect, Palette.GOLD, u * 0.35f, u * 0.8f)
        val nx = if (alignLeft) left else right - Ui.run { text.textSize = u * 3.2f; text.measureText(name) }
        Ui.leftText(canvas, name, nx, top + u * 3.5f, u * 3.2f, Palette.PARCHMENT, true)
    }
}
