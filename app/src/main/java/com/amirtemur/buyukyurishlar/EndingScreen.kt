package com.amirtemur.buyukyurishlar

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.sin

/** Ta'sirli yakuniy sahna: imperiya bosqichma-bosqich yorishadi, matn paydo bo'ladi */
class EndingScreen(game: Game) : Screen(game) {

    private val bMenu = Button("MENYUGA QAYTISH")
    private var t = 0f
    private var litCount = 0
    private var fanfarePlayed = false

    private val lightStart = 1.2f
    private val lightStep = 0.55f

    init {
        game.progress.setStars(6, 3)
        game.progress.unlocked = Data.campaigns.size
        bMenu.onClick = { game.setScreen(MenuScreen(game)) }
        buttons.add(bMenu)
        bMenu.visible = false
        game.sound.victory()
    }

    override fun onResize(w: Int, h: Int) {
        val u = game.unit
        bMenu.set(w / 2f - u * 22f, h - u * 11f, w / 2f + u * 22f, h - u * 2.5f)
    }

    override fun update(dt: Float) {
        t += dt
        val target = (((t - lightStart) / lightStep).toInt()).coerceIn(0, Data.campaigns.size)
        if (target > litCount) { litCount = target; game.sound.gong() }
        if (litCount >= Data.campaigns.size && !fanfarePlayed) { fanfarePlayed = true; game.sound.fanfare() }
        bMenu.visible = t > textStart() + Data.endingLines.size * 0.9f + 0.5f
    }

    private fun textStart() = lightStart + Data.campaigns.size * lightStep + 0.4f
    override fun onBack(): Boolean { game.setScreen(MenuScreen(game)); return true }

    override fun draw(canvas: Canvas) {
        val w = game.width; val h = game.height; val u = game.unit

        // climax glow
        val climax = ((litCount.toFloat() / Data.campaigns.size)).coerceIn(0f, 1f)
        Art.glow(canvas, w / 2f, h * 0.42f, maxOf(w, h) * 0.55f, Palette.GOLD, (40 * climax).toInt())

        // Amir Temur siymosi (chap, asta paydo bo'ladi)
        val figA = (t / 1.6f).coerceIn(0f, 1f)
        val temur = Assets.bmp(game.context, "temur")
        if (temur != null) Assets.drawFitHeight(canvas, temur, w * 0.14f, h * 0.96f, h * 0.7f, figA)
        else Art.drawTemur(canvas, w * 0.14f, h * 0.97f, h * 0.78f, t, figA)

        Ui.centerText(canvas, "AMIR TEMUR IMPERIYASI", w * 0.58f, u * 9f, u * 6f, Palette.GOLD_LIGHT, true)

        // imperiya xaritasi
        val map = RectF(w * 0.30f, u * 15f, w - u * 4f, h * 0.56f)
        Ui.paint.shader = null; Ui.paint.style = Paint.Style.FILL
        Ui.paint.color = 0xFF161009.toInt()
        canvas.drawRoundRect(map, u * 2f, u * 2f, Ui.paint)
        Ui.border(canvas, map, Palette.GOLD, u * 0.5f, u * 2f)

        val pulse = 0.5f + 0.5f * sin(t * 2.5f)
        Data.campaigns.forEachIndexed { i, cmp ->
            val x = map.left + cmp.mapX * map.width()
            val y = map.top + cmp.mapY * map.height()
            if (i < litCount) {
                Art.glow(canvas, x, y, u * (5f + pulse * 2f), Palette.GOLD, 150)
                Ui.paint.color = Palette.GOLD_LIGHT
                canvas.drawCircle(x, y, u * 2.2f, Ui.paint)
            } else {
                Ui.paint.color = 0xFF3A2E18.toInt()
                canvas.drawCircle(x, y, u * 1.6f, Ui.paint)
            }
        }
        Ui.centerText(canvas, "Samarqand — buyuk imperiyaning poytaxti", map.centerX(), map.bottom - u * 3.5f, u * 2.8f, Palette.GOLD)

        // yakuniy matn (bosqichma-bosqich)
        val ts = textStart()
        var y = h * 0.60f
        Data.endingLines.forEachIndexed { i, line ->
            val appear = t - (ts + i * 0.9f)
            if (appear > 0f) {
                val a = (appear / 0.6f).coerceIn(0f, 1f)
                Ui.text.alpha = (a * 255).toInt()
                val last = i == Data.endingLines.size - 1
                Ui.centerText(canvas, line, w / 2f, y, if (last) u * 4.2f else u * 3.2f,
                    if (last) Palette.GOLD_LIGHT else Palette.PARCHMENT, last)
                Ui.text.alpha = 255
            }
            y += u * 5.2f
        }

        drawButtons(canvas)
    }
}
