package com.amirtemur.buyukyurishlar

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.sin

class EndingScreen(game: Game) : Screen(game) {

    private val bMenu = Button("MENYUGA QAYTISH")
    private var t = 0f
    private var revealed = 0f

    init {
        game.progress.setStars(6, 3)
        game.progress.unlocked = Data.campaigns.size
        bMenu.onClick = { game.setScreen(MenuScreen(game)) }
        buttons.add(bMenu)
        game.sound.victory()
    }

    override fun onResize(w: Int, h: Int) {
        val u = game.unit
        bMenu.set(w / 2f - u * 22f, h - u * 12f, w / 2f + u * 22f, h - u * 2.5f)
    }

    override fun update(dt: Float) { t += dt; revealed += dt * 0.6f }
    override fun onBack(): Boolean { game.setScreen(MenuScreen(game)); return true }

    override fun draw(canvas: Canvas) {
        val w = game.width; val h = game.height; val u = game.unit

        Ui.centerText(canvas, "AMIR TEMUR IMPERIYASI", w / 2f, u * 7f, u * 5.5f, Palette.GOLD_LIGHT)

        // imperiya xaritasi
        val map = RectF(u * 4f, u * 12f, w - u * 4f, h * 0.56f)
        Ui.paint.style = Paint.Style.FILL
        Ui.paint.color = 0xFF161009.toInt()
        canvas.drawRoundRect(map, u * 2f, u * 2f, Ui.paint)
        Ui.border(canvas, map, Palette.GOLD, u * 0.5f, u * 2f)

        // zabt etilgan hududlar (oltin nur bilan)
        val pulse = 0.5f + 0.5f * sin(t * 2f)
        for (c in Data.campaigns) {
            val x = map.left + c.mapX * map.width()
            val y = map.top + c.mapY * map.height()
            Ui.paint.style = Paint.Style.FILL
            Ui.paint.color = Palette.GOLD
            Ui.paint.alpha = (120 + pulse * 100).toInt().coerceIn(0, 255)
            canvas.drawCircle(x, y, u * (2.4f + pulse), Ui.paint)
            Ui.paint.alpha = 255
        }
        Ui.centerText(canvas, "Samarqand — Buyuk imperiyaning poytaxti",
            map.centerX(), map.bottom - u * 4f, u * 3f, Palette.GOLD)

        // yakuniy matn
        val panel = RectF(u * 8f, h * 0.59f, w - u * 8f, h - u * 14f)
        Ui.fill(canvas, panel, 0xAA0A0805.toInt(), u * 1.5f)
        var y = panel.top + u * 5f
        val show = revealed.toInt().coerceAtMost(Data.endingLines.size)
        for (i in 0 until show) {
            val color = if (i == Data.endingLines.size - 1) Palette.GOLD_LIGHT else Palette.PARCHMENT
            Ui.centerText(canvas, Data.endingLines[i], w / 2f, y, u * 3.2f, color)
            y += u * 5f
        }

        drawButtons(canvas)
    }
}
