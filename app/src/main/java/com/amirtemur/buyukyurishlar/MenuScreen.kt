package com.amirtemur.buyukyurishlar

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.math.cos
import kotlin.math.sin

class MenuScreen(game: Game) : Screen(game) {

    private val bStart = Button("BOSHLASH")
    private val bSound = Button("OVOZ")
    private val bReset = Button("QAYTADAN")
    private val bExit = Button("CHIQISH")
    private var t = 0f

    init {
        bStart.onClick = {
            game.sound.confirm()
            if (game.progress.unlocked == 0) {
                game.setScreen(CutsceneScreen(game, Data.introLines, "DAVOM ETISH") {
                    game.setScreen(MapScreen(game))
                })
            } else game.setScreen(MapScreen(game))
        }
        bSound.onClick = {
            game.sound.enabled = !game.sound.enabled
            game.progress.soundOn = game.sound.enabled
            updateSoundLabel()
        }
        bReset.onClick = {
            game.progress.reset()
            game.sound.enabled = game.progress.soundOn
            updateSoundLabel()
        }
        bExit.onClick = { (game.context as? android.app.Activity)?.finish() }
        buttons.addAll(listOf(bStart, bSound, bReset, bExit))
        updateSoundLabel()
    }

    private fun updateSoundLabel() {
        bSound.label = "OVOZ: " + if (game.sound.enabled) "YONIQ" else "O'CHIQ"
    }

    override fun onResize(w: Int, h: Int) {
        val u = game.unit
        val bw = w * 0.46f
        val bh = u * 11f
        val gap = u * 3.2f
        val cx = w / 2f
        var y = h * 0.46f
        for (b in listOf(bStart, bSound, bReset, bExit)) {
            b.set(cx - bw / 2f, y, cx + bw / 2f, y + bh)
            y += bh + gap
        }
    }

    override fun update(dt: Float) { t += dt }

    override fun draw(canvas: Canvas) {
        val w = game.width; val h = game.height; val u = game.unit

        drawEmblem(canvas, w / 2f, h * 0.20f, u * 11f)

        Ui.centerText(canvas, "AMIR TEMUR", w / 2f, h * 0.345f, u * 9f, Palette.GOLD_LIGHT)
        Ui.centerText(canvas, "BUYUK YURISHLAR", w / 2f, h * 0.41f, u * 5.2f, Palette.GOLD)

        drawButtons(canvas)

        Ui.centerText(canvas, "2D Tarixiy Strategiya  •  To'liq o'zbekcha",
            w / 2f, h - u * 4f, u * 3.1f, 0xFF8C7A4F.toInt())
    }

    /** Toj + qilich emblemasi */
    private fun drawEmblem(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        val p = Ui.paint
        // nurli halqa
        p.style = Paint.Style.STROKE
        p.color = Palette.BROWN_LIGHT
        p.strokeWidth = size * 0.05f
        canvas.drawCircle(cx, cy, size * 1.5f, p)
        p.color = Palette.GOLD
        p.strokeWidth = size * 0.03f
        canvas.drawCircle(cx, cy, size * 1.32f, p)

        // toj
        p.style = Paint.Style.FILL
        p.color = Palette.GOLD_LIGHT
        val cw = size * 1.0f
        val crown = Path().apply {
            moveTo(cx - cw, cy - size * 0.15f)
            lineTo(cx - cw * 0.55f, cy - size * 0.95f)
            lineTo(cx - cw * 0.28f, cy - size * 0.35f)
            lineTo(cx, cy - size * 1.1f)
            lineTo(cx + cw * 0.28f, cy - size * 0.35f)
            lineTo(cx + cw * 0.55f, cy - size * 0.95f)
            lineTo(cx + cw, cy - size * 0.15f)
            close()
        }
        canvas.drawPath(crown, p)
        val band = RectF(cx - cw, cy - size * 0.18f, cx + cw, cy + size * 0.12f)
        canvas.drawRect(band, p)
        p.color = Palette.RED
        canvas.drawCircle(cx, cy - size * 0.03f, size * 0.12f, p)

        // qilich (pastga qaragan)
        p.color = Palette.PARCHMENT
        val blade = Path().apply {
            moveTo(cx - size * 0.12f, cy + size * 0.35f)
            lineTo(cx + size * 0.12f, cy + size * 0.35f)
            lineTo(cx + size * 0.12f, cy + size * 1.25f)
            lineTo(cx, cy + size * 1.5f)
            lineTo(cx - size * 0.12f, cy + size * 1.25f)
            close()
        }
        canvas.drawPath(blade, p)
        p.color = Palette.GOLD
        canvas.drawRect(RectF(cx - size * 0.5f, cy + size * 0.28f, cx + size * 0.5f, cy + size * 0.42f), p)
    }
}
