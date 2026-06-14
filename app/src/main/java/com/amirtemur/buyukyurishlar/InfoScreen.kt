package com.amirtemur.buyukyurishlar

import android.graphics.Canvas
import android.graphics.RectF

class InfoScreen(game: Game, private val c: Campaign) : Screen(game) {

    private val bBack = Button("‹ ORQAGA")

    init {
        bBack.onClick = { game.setScreen(ResultScreen(game)) }
        buttons.add(bBack)
    }

    override fun onResize(w: Int, h: Int) {
        val u = game.unit
        bBack.set(u * 3f, h - u * 13f, u * 26f, h - u * 3f)
    }

    override fun onBack(): Boolean { game.setScreen(ResultScreen(game)); return true }

    override fun draw(canvas: Canvas) {
        val w = game.width; val h = game.height; val u = game.unit

        Ui.centerText(canvas, "TARIXIY MA'LUMOT", w / 2f, u * 7f, u * 5f, Palette.GOLD_LIGHT)
        Ui.centerText(canvas, "${c.title}  (${c.years})", w / 2f, u * 13f, u * 3.4f, Palette.GOLD)

        val panel = RectF(u * 6f, u * 18f, w - u * 6f, h - u * 16f)
        Ui.panel(canvas, panel, u * 2f)

        var y = panel.top + u * 7f
        for (line in c.info) {
            Ui.leftText(canvas, "•", panel.left + u * 4f, y, u * 3.6f, Palette.GOLD)
            for (ln in Ui.wrap(line, u * 3.3f, panel.width() - u * 14f)) {
                Ui.leftText(canvas, ln, panel.left + u * 8f, y, u * 3.3f, Palette.PARCHMENT)
                y += u * 4.6f
            }
            y += u * 2f
        }

        Ui.centerText(canvas, "Manbalar: tarixiy manbalar asosida (Britannica, Wikipedia)",
            w / 2f, h - u * 15.5f, u * 2.6f, 0xFF8C7A4F.toInt())

        drawButtons(canvas)
    }
}
