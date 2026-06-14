package com.amirtemur.buyukyurishlar

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

/**
 * Reklama o'rni — NAMUNA (placeholder).
 * Haqiqiy AdMob (Rewarded / Interstitial) integratsiyasini shu yerga ulash mumkin:
 * o'z AdMob App ID va Ad Unit ID laringizni qo'shing.
 * Spam bo'lmasligi uchun reklama faqat ixtiyoriy "bonus" tugmasi orqali chaqiriladi.
 */
class AdScreen(
    game: Game,
    private val title: String,
    private val onReward: () -> Unit
) : Screen(game) {

    private var time = 0f
    private val duration = 3f
    private val bClaim = Button("MUKOFOTNI OLISH ★")
    private val bClose = Button("✕")

    init {
        bClaim.onClick = { game.sound.confirm(); onReward() }
        bClose.onClick = { onReward() }
        buttons.add(bClaim); buttons.add(bClose)
        bClaim.visible = false
    }

    override fun onResize(w: Int, h: Int) {
        val u = game.unit
        bClaim.set(w / 2f - u * 22f, h - u * 16f, w / 2f + u * 22f, h - u * 5f)
        bClose.set(w - u * 11f, u * 3f, w - u * 3f, u * 11f)
    }

    override fun update(dt: Float) {
        time += dt
        if (time >= duration) { bClaim.visible = true; bClose.visible = true }
        else bClose.visible = false
    }

    override fun onBack(): Boolean { if (time >= duration) onReward(); return true }

    override fun draw(canvas: Canvas) {
        val w = game.width; val h = game.height; val u = game.unit
        Ui.paint.style = Paint.Style.FILL
        Ui.paint.color = 0xFF101820.toInt()
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), Ui.paint)

        Ui.centerText(canvas, "REKLAMA (NAMUNA)", w / 2f, u * 8f, u * 4f, Palette.GOLD_LIGHT)
        Ui.centerText(canvas, title, w / 2f, u * 14f, u * 3.4f, Palette.GOLD)

        // soxta reklama banneri
        val banner = RectF(w * 0.25f, h * 0.30f, w * 0.75f, h * 0.62f)
        Ui.fill(canvas, banner, Palette.BLUE, u * 2f)
        Ui.border(canvas, banner, Palette.GOLD, u * 0.5f, u * 2f)
        Ui.centerText(canvas, "Amir Temur: Buyuk Yurishlar", w / 2f, banner.centerY() - u * 3f, u * 3.6f, Palette.PARCHMENT)
        Ui.centerText(canvas, "Tarixiy strategiya o'yini", w / 2f, banner.centerY() + u * 2f, u * 3f, Palette.PARCHMENT)

        if (time < duration) {
            val left = (duration - time).toInt() + 1
            Ui.centerText(canvas, "Mukofot $left soniyadan keyin...", w / 2f, h - u * 10f, u * 3f, Palette.GOLD)
        }
        drawButtons(canvas)
    }
}
