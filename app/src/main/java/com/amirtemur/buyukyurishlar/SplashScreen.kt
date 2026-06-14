package com.amirtemur.buyukyurishlar

import android.graphics.Canvas
import kotlin.math.sin

/** O'yin boshlanishida Amir Temur siymosi gavdalanadi */
class SplashScreen(game: Game) : Screen(game) {

    private var t = 0f

    override fun onUp(x: Float, y: Float) {
        if (t > 0.6f) { game.sound.confirm(); game.setScreen(MenuScreen(game)) }
    }
    override fun onBack(): Boolean { game.setScreen(MenuScreen(game)); return true }

    override fun update(dt: Float) { t += dt }

    private fun ease(x: Float) = 1f - (1f - x) * (1f - x)

    override fun draw(canvas: Canvas) {
        val w = game.width; val h = game.height; val u = game.unit

        val rise = ease((t / 1.3f).coerceIn(0f, 1f))
        val alpha = (t / 1.1f).coerceIn(0f, 1f)
        val figH = h * 0.74f
        val footY = h * 0.90f + (1f - rise) * h * 0.25f

        val temur = Assets.bmp(game.context, "temur")
        if (temur != null) Assets.drawFitHeight(canvas, temur, w / 2f, footY, figH, alpha)
        else Art.drawTemur(canvas, w / 2f, footY, figH, t, alpha)

        // ism
        val nameA = ((t - 1.0f) / 1.0f).coerceIn(0f, 1f)
        if (nameA > 0f) {
            Ui.text.alpha = (nameA * 255).toInt()
            Ui.centerText(canvas, "AMIR TEMUR", w / 2f, h * 0.16f, u * 10f, Palette.GOLD_LIGHT, true)
            Ui.centerText(canvas, "BUYUK YURISHLAR", w / 2f, h * 0.27f, u * 5f, Palette.GOLD, true)
            Ui.text.alpha = 255
        }

        // davom prompt
        if (t > 2.0f) {
            val blink = (0.5f + 0.5f * sin(t * 3f))
            Ui.text.alpha = (blink * 220).toInt()
            Ui.centerText(canvas, "Davom etish uchun ekranni bosing", w / 2f, h - u * 6f, u * 3.4f, Palette.PARCHMENT)
            Ui.text.alpha = 255
        }

        // sana
        Ui.centerText(canvas, "1336 – 1405", w / 2f, h * 0.345f, u * 3.4f, 0xFFB8A06A.toInt())
    }
}
