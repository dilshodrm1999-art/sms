package com.amirtemur.buyukyurishlar

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF

/**
 * Umumiy sinematik sahna — o'zbekcha subtitrlar yozuv mashinkasi effekti bilan.
 */
class CutsceneScreen(
    game: Game,
    private val lines: List<String>,
    private val doneLabel: String,
    private val title: String? = null,
    private val onDone: () -> Unit
) : Screen(game) {

    private var index = 0
    private var shown = 0f
    private val speed = 38f
    private val bSkip = Button("O'TKAZISH ▸")
    private val bNext = Button(doneLabel)
    private var t = 0f

    init {
        bSkip.onClick = { game.sound.click(); onDone() }
        bNext.onClick = { game.sound.confirm(); onDone() }
        buttons.add(bSkip)
        buttons.add(bNext)
        bNext.visible = false
    }

    override fun onResize(w: Int, h: Int) {
        val u = game.unit
        bSkip.set(w - u * 26f, u * 3f, w - u * 3f, u * 11f)
        bNext.set(w / 2f - u * 22f, h - u * 14f, w / 2f + u * 22f, h - u * 3f)
    }

    private val lineComplete get() = shown >= lines[index].length

    override fun update(dt: Float) {
        t += dt
        if (index < lines.size && !lineComplete) shown += speed * dt
        bNext.visible = index >= lines.size - 1 && lineComplete
    }

    override fun onUp(x: Float, y: Float) {
        // tugmalar ustida bo'lsa avval ularni qayta ishlash
        val hit = buttons.firstOrNull { it.pressed && it.contains(x, y) }
        buttons.forEach { it.pressed = false }
        if (hit != null) { game.sound.click(); hit.onClick(); return }
        // aks holda matnni tezlashtirish / keyingi qator
        if (!lineComplete) {
            shown = lines[index].length.toFloat()
        } else if (index < lines.size - 1) {
            index++; shown = 0f; game.sound.click()
        }
    }

    override fun onBack(): Boolean { onDone(); return true }

    override fun draw(canvas: Canvas) {
        val w = game.width; val h = game.height; val u = game.unit
        drawScene(canvas, w, h, u)

        title?.let {
            Ui.centerText(canvas, it, w / 2f, u * 7f, u * 5.5f, Palette.GOLD_LIGHT)
        }

        // subtitr paneli
        val panel = RectF(u * 6f, h - u * 34f, w - u * 6f, h - u * 16f)
        Ui.paint.style = Paint.Style.FILL
        Ui.paint.color = 0xCC0A0805.toInt()
        canvas.drawRoundRect(panel, u * 2f, u * 2f, Ui.paint)
        Ui.border(canvas, panel, Palette.GOLD, u * 0.4f, u * 2f)

        val full = lines[index]
        val visible = full.substring(0, shown.toInt().coerceIn(0, full.length))
        val size = u * 4.4f
        val wrapped = Ui.wrap(visible, size, panel.width() - u * 8f)
        var ty = panel.top + u * 6f
        for (ln in wrapped) {
            Ui.leftText(canvas, ln, panel.left + u * 4f, ty, size, Palette.PARCHMENT)
            ty += size * 1.25f
        }

        // qator hisoblagich
        Ui.centerText(canvas, "${index + 1} / ${lines.size}", w / 2f, h - u * 16.5f, u * 3f, 0xFF8C7A4F.toInt())

        drawButtons(canvas)
        if (!bNext.visible) {
            Ui.centerText(canvas, "Davom etish uchun bosing",
                w / 2f, h - u * 6f, u * 3f, 0xFF8C7A4F.toInt())
        }
    }

    /** Oddiy medieval ambiental sahna (siluetlar) */
    private fun drawScene(canvas: Canvas, w: Int, h: Int, u: Float) {
        val p = Ui.paint
        p.style = Paint.Style.FILL
        // quyosh/oy
        p.color = 0x33C9A227
        canvas.drawCircle(w * 0.72f, h * 0.30f, u * 16f, p)
        p.color = 0x66C9A227
        canvas.drawCircle(w * 0.72f, h * 0.30f, u * 9f, p)
        // tog' siluetlari
        p.color = Palette.BROWN_DARK
        val horizon = h * 0.62f
        val m = Path().apply {
            moveTo(0f, horizon)
            lineTo(w * 0.18f, horizon - u * 16f)
            lineTo(w * 0.34f, horizon)
            lineTo(w * 0.55f, horizon - u * 22f)
            lineTo(w * 0.78f, horizon)
            lineTo(w * 0.92f, horizon - u * 12f)
            lineTo(w.toFloat(), horizon)
            lineTo(w.toFloat(), h.toFloat())
            lineTo(0f, h.toFloat())
            close()
        }
        canvas.drawPath(m, p)
        // chodir siluetlari
        p.color = 0xFF1A1209.toInt()
        for (i in 0 until 4) {
            val cx = w * (0.12f + i * 0.07f)
            val tent = Path().apply {
                moveTo(cx, horizon)
                lineTo(cx - u * 4f, horizon + u * 6f)
                lineTo(cx + u * 4f, horizon + u * 6f)
                close()
            }
            canvas.drawPath(tent, p)
        }
    }
}
