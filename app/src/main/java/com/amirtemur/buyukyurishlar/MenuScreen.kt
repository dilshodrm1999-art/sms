package com.amirtemur.buyukyurishlar

import android.graphics.Canvas

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

    override fun onShow() { bStart.label = if (game.progress.unlocked > 0) "DAVOM ETISH" else "BOSHLASH" }

    private fun updateSoundLabel() {
        bSound.label = "OVOZ: " + if (game.sound.enabled) "YONIQ" else "O'CHIQ"
    }

    override fun onResize(w: Int, h: Int) {
        val u = game.unit
        val colL = w * 0.50f
        val colR = w * 0.94f
        val bh = u * 11f
        val gap = u * 3.4f
        var y = h * 0.40f
        for (b in listOf(bStart, bSound, bReset, bExit)) {
            b.set(colL, y, colR, y + bh)
            y += bh + gap
        }
    }

    override fun update(dt: Float) { t += dt }

    override fun draw(canvas: Canvas) {
        val w = game.width; val h = game.height; val u = game.unit

        // Amir Temur siymosi (chap tomonda) — rasm bo'lsa o'shani, bo'lmasa vektor
        val temur = Assets.bmp(game.context, "temur")
        if (temur != null) Assets.drawFitHeight(canvas, temur, w * 0.24f, h * 0.99f, h * 0.92f, 1f)
        else Art.drawTemur(canvas, w * 0.24f, h * 0.99f, h * 0.92f, t)

        // sarlavha
        Ui.centerText(canvas, "AMIR TEMUR", w * 0.72f, h * 0.16f, u * 8.5f, Palette.GOLD_LIGHT, true)
        Ui.centerText(canvas, "BUYUK YURISHLAR", w * 0.72f, h * 0.255f, u * 4.6f, Palette.GOLD, true)

        drawButtons(canvas)

        // progress indikatori
        if (game.progress.unlocked > 0) {
            val done = game.progress.unlocked.coerceAtMost(Data.campaigns.size)
            Ui.centerText(canvas, "Bosqich: $done / ${Data.campaigns.size}",
                w * 0.72f, h * 0.335f, u * 3.2f, Palette.PARCHMENT)
        }

        Ui.centerText(canvas, "2D Tarixiy Strategiya  •  To'liq o'zbekcha  •  Offline",
            w / 2f, h - u * 3.5f, u * 3f, 0xFF8C7A4F.toInt())
    }
}
