package com.amirtemur.buyukyurishlar

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.sin

/**
 * Bir shahardan ikkinchisiga yurish — 1 daqiqali (60s) teskari sanoq,
 * tarixiy yil/ma'lumot va yo'lda harakatlanuvchi qo'shin.
 */
class TravelScreen(game: Game) : Screen(game) {

    private val c = game.selectedCampaign!!
    private val countdown = 60f
    private var t = 0f
    private var hoof = 0f
    private val fromCity = if (c.id <= 0) "Vatan — Kesh" else Data.campaigns[c.id - 1].region
    private val toCity = c.region

    private val bSkip = Button("O'TKAZIB YUBORISH")
    private val bGo = Button("JANGGA TAYYORLANISH ▸")

    init {
        bSkip.onClick = { proceed() }
        bGo.onClick = { proceed() }
        buttons.add(bSkip); buttons.add(bGo)
        bGo.visible = false
        game.sound.march()
    }

    private fun proceed() {
        game.sound.confirm()
        game.playerArmy = Army()
        game.setScreen(ArmyScreen(game))
    }

    override fun onResize(w: Int, h: Int) {
        val u = game.unit
        bSkip.set(u * 3f, h - u * 12f, u * 30f, h - u * 3f)
        bGo.set(w - u * 38f, h - u * 12f, w - u * 3f, h - u * 3f)
    }

    override fun update(dt: Float) {
        t += dt
        hoof -= dt
        if (hoof <= 0f) { game.sound.march(); hoof = 2.2f }
        bGo.visible = t >= countdown
        if (t >= countdown + 0.01f) { /* avtomatik o'tmaymiz — tugma orqali */ }
    }

    override fun onBack(): Boolean { game.setScreen(MapScreen(game)); return true }

    override fun draw(canvas: Canvas) {
        val w = game.width; val h = game.height; val u = game.unit
        val progress = (t / countdown).coerceIn(0f, 1f)

        Ui.centerText(canvas, "BUYUK YURISH", w / 2f, u * 8f, u * 6f, Palette.GOLD_LIGHT, true)
        Ui.centerText(canvas, "$fromCity  →  $toCity", w / 2f, u * 15f, u * 3.6f, Palette.GOLD)

        // teskari sanoq (MM:SS)
        val remain = (countdown - t).coerceAtLeast(0f)
        val mm = (remain.toInt() / 60); val ss = (remain.toInt() % 60)
        val timeStr = "%02d:%02d".format(mm, ss)
        Ui.glassPanel(canvas, RectF(w / 2f - u * 16f, u * 19f, w / 2f + u * 16f, u * 32f), u * 1.5f, 0xAA)
        Ui.centerText(canvas, timeStr, w / 2f, u * 26f, u * 9f, if (remain > 0f) Palette.PARCHMENT else Palette.GOLD_LIGHT, true)
        Ui.centerText(canvas, if (remain > 0f) "Yo'ldamiz..." else "Manzilga yetib keldik!", w / 2f, u * 31f, u * 2.8f, Palette.GOLD)

        // yo'l chizig'i
        val roadY = h * 0.62f
        Ui.stroke.shader = null; Ui.stroke.color = Palette.BROWN_LIGHT; Ui.stroke.strokeWidth = u * 1.2f
        Ui.stroke.pathEffect = DashPathEffect(floatArrayOf(u * 2f, u * 2f), 0f)
        canvas.drawLine(w * 0.1f, roadY, w * 0.9f, roadY, Ui.stroke)
        Ui.stroke.pathEffect = null
        // shahar nuqtalari
        Ui.fill(canvas, RectF(w * 0.1f - u * 1.5f, roadY - u * 1.5f, w * 0.1f + u * 1.5f, roadY + u * 1.5f), Palette.GOLD, u)
        Ui.fill(canvas, RectF(w * 0.9f - u * 1.5f, roadY - u * 1.5f, w * 0.9f + u * 1.5f, roadY + u * 1.5f), Palette.RED, u)
        Ui.centerText(canvas, fromCity, w * 0.1f, roadY + u * 5f, u * 2.6f, Palette.PARCHMENT)
        Ui.centerText(canvas, toCity, w * 0.9f, roadY + u * 5f, u * 2.6f, Palette.PARCHMENT)
        // harakatlanuvchi qo'shin
        val travelX = w * 0.1f + (w * 0.8f) * progress
        val bob = sin(t * 6f) * u * 0.6f
        Art.trooper(canvas, travelX, roadY + bob, u * 7f, Palette.ROYAL, false, TroopType.OTLIQ)

        // tarixiy ma'lumot
        val info = RectF(u * 6f, h * 0.70f, w - u * 6f, h - u * 14f)
        Ui.glassPanel(canvas, info, u * 1.5f, 0xAA)
        Ui.leftText(canvas, "Tarixiy davr: ${c.years}   •   Hudud: ${c.region}", info.left + u * 3f, info.top + u * 5f, u * 3f, Palette.GOLD, true)
        var y = info.top + u * 9.5f
        for (line in c.info.take(2)) {
            for (ln in Ui.wrap(line, u * 2.8f, info.width() - u * 8f)) {
                Ui.leftText(canvas, ln, info.left + u * 3f, y, u * 2.8f, Palette.PARCHMENT); y += u * 3.6f
            }
        }

        Ui.woodButton(canvas, bSkip, u)
        if (bGo.visible) Ui.woodButton(canvas, bGo, u)
    }
}
