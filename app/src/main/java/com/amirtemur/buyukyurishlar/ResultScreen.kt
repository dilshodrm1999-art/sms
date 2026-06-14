package com.amirtemur.buyukyurishlar

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

class ResultScreen(game: Game) : Screen(game) {

    private val c = game.selectedCampaign!!
    private val r = game.lastResult!!
    private val next = Nav.nextCampaign(game, c)
    private var bonusShown = false

    private val bInfo = Button("TARIXIY MA'LUMOT")
    private val bReward = Button("BONUS (REKLAMA) ★")
    private val bMain = Button("DAVOM ETISH")
    private val bNext = Button("KEYINGI YURISH ▸")

    init {
        if (r.win) {
            game.progress.setStars(c.id, r.rating)
            game.progress.unlocked = c.id + 1
        }
        bInfo.onClick = { game.setScreen(InfoScreen(game, c)) }
        bReward.onClick = {
            game.setScreen(AdScreen(game, "Mukofotli reklama") {
                bonusShown = true
                game.setScreen(this)
            })
        }
        bMain.onClick = {
            if (r.win) game.setScreen(MapScreen(game))
            else { game.playerArmy = Army(); game.setScreen(ArmyScreen(game)) }
        }
        bNext.onClick = {
            next?.let { Nav.startCampaign(game, it) }
        }
        bMain.label = if (r.win) "XARITAGA" else "QAYTA URINISH"
        buttons.addAll(listOf(bInfo, bReward, bMain, bNext))
        bNext.visible = r.win && next != null
        bReward.visible = r.win && !bonusShown
    }

    override fun onResize(w: Int, h: Int) {
        val u = game.unit
        val bw = w * 0.40f
        val bh = u * 10f
        val cx = w / 2f
        var y = h * 0.58f
        val order = listOfNotNull(
            bInfo,
            if (bReward.visible) bReward else null,
            bMain,
            if (bNext.visible) bNext else null
        )
        // ikki ustun joylashuvi
        val gap = u * 3f
        for ((i, b) in order.withIndex()) {
            val col = i % 2
            val row = i / 2
            val x = cx - bw - gap / 2f + col * (bw + gap)
            b.set(x, y + row * (bh + gap), x + bw, y + row * (bh + gap) + bh)
        }
    }

    override fun onShow() { onResize(game.width, game.height) }
    override fun onBack(): Boolean { game.setScreen(MapScreen(game)); return true }

    override fun draw(canvas: Canvas) {
        val w = game.width; val h = game.height; val u = game.unit
        bReward.visible = r.win && !bonusShown
        bNext.visible = r.win && next != null

        val title = if (r.win) "G'ALABA QOZONILDI" else "JANG MAG'LUBIYATI"
        Ui.centerText(canvas, title, w / 2f, h * 0.13f, u * 7f,
            if (r.win) Palette.GOLD_LIGHT else Palette.RED)
        Ui.centerText(canvas, c.headline, w / 2f, h * 0.21f, u * 3.6f, Palette.GOLD)

        if (r.win) Ui.stars(canvas, w / 2f, h * 0.30f, u * 7f, r.rating)

        // statistik panel
        val pr = RectF(w * 0.28f, h * 0.36f, w * 0.72f, h * 0.52f)
        Ui.panel(canvas, pr, u * 2f)
        Ui.centerText(canvas, "Strategiya: ${r.strategy.uz}", w / 2f, pr.top + u * 5f, u * 3.4f, Palette.PARCHMENT)
        Ui.centerText(canvas, "Sizning kuchingiz: ${r.playerPower.toInt()}   •   Dushman: ${r.enemyPower.toInt()}",
            w / 2f, pr.top + u * 10f, u * 3f, Palette.PARCHMENT)
        Ui.centerText(canvas, "Qo'shin yo'qotishi: ${r.playerLossPct}%",
            w / 2f, pr.top + u * 14.5f, u * 3f, if (r.playerLossPct > 60) Palette.RED else Palette.GOLD)

        if (bonusShown) {
            Ui.centerText(canvas, "★ Bonus: Samarqand xazinasidan oltin qo'shildi! ★",
                w / 2f, h * 0.545f, u * 2.9f, Palette.GREEN)
        }

        drawButtons(canvas)
    }
}
