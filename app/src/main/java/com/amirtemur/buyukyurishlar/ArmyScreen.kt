package com.amirtemur.buyukyurishlar

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

class ArmyScreen(game: Game) : Screen(game) {

    private val c = game.selectedCampaign!!
    private val army = game.playerArmy
    private val troops = TroopType.values()

    private val bBack = Button("‹ XARITA")
    private val bAuto = Button("AVTO TO'LDIRISH")
    private val bStart = Button("JANGGA! ⚔")
    private val minus = troops.map { Button("−") }
    private val plus = troops.map { Button("+") }
    private val strat = Strategy.values().map { s -> Button(s.uz) }

    init {
        game.chosenStrategy = c.bestStrategy
        bBack.onClick = { game.setScreen(MapScreen(game)) }
        bAuto.onClick = { autoFill(); game.sound.confirm() }
        bStart.onClick = {
            if (army.totalSoldiers() > 0) {
                game.sound.march()
                game.setScreen(BattleScreen(game))
            } else game.sound.defeat()
        }
        troops.forEachIndexed { i, t ->
            minus[i].onClick = { changeTroop(t, -step(t)) }
            plus[i].onClick = { changeTroop(t, step(t)) }
        }
        Strategy.values().forEachIndexed { i, s ->
            strat[i].onClick = { game.chosenStrategy = s; game.sound.click() }
        }
        buttons.add(bBack); buttons.add(bAuto); buttons.add(bStart)
        buttons.addAll(minus); buttons.addAll(plus); buttons.addAll(strat)
    }

    private fun step(t: TroopType) = maxOf(1, c.budget / (t.cost * 12))
    private fun spent() = army.spentGold()
    private fun remaining() = c.budget - spent()

    private fun changeTroop(t: TroopType, d: Int) {
        if (d > 0) {
            val affordable = remaining() / t.cost
            if (affordable <= 0) { game.sound.defeat(); return }
            army.add(t, minOf(d, affordable))
        } else army.add(t, d)
        game.sound.click()
    }

    private fun autoFill() {
        TroopType.values().forEach { army.set(it, 0) }
        val weights: Map<TroopType, Float> = when (game.chosenStrategy) {
            Strategy.FRONTAL -> mapOf(TroopType.OTLIQ to 0.45f, TroopType.ZIRH to 0.30f, TroopType.PIYODA to 0.15f, TroopType.KAMONCHI to 0.10f)
            Strategy.YASHIRIN -> mapOf(TroopType.OTLIQ to 0.55f, TroopType.KAMONCHI to 0.30f, TroopType.PIYODA to 0.15f)
            Strategy.QANOT -> mapOf(TroopType.OTLIQ to 0.55f, TroopType.KAMONCHI to 0.25f, TroopType.PIYODA to 0.20f)
            Strategy.MUDOFAA -> mapOf(TroopType.ZIRH to 0.40f, TroopType.KAMONCHI to 0.35f, TroopType.PIYODA to 0.25f)
            Strategy.QAMAL -> mapOf(TroopType.QAMAL to 0.40f, TroopType.PIYODA to 0.35f, TroopType.ZIRH to 0.25f)
        }
        for ((t, wgt) in weights) {
            val gold = (c.budget * wgt).toInt()
            army.set(t, gold / t.cost)
        }
        // qoldiqni piyodaga
        val rem = remaining()
        if (rem > 0) army.add(TroopType.PIYODA, rem / TroopType.PIYODA.cost)
    }

    override fun onResize(w: Int, h: Int) {
        val u = game.unit
        bBack.set(u * 3f, u * 3f, u * 22f, u * 10.5f)

        // chap panel: qo'shin
        val pl = RectF(u * 3f, u * 13f, w * 0.55f, h - u * 3f)
        val rowH = (pl.height() - u * 6f) / troops.size
        troops.forEachIndexed { i, _ ->
            val cy = pl.top + u * 3f + i * rowH + rowH / 2f
            val sz = rowH * 0.55f
            minus[i].set(pl.right - u * 26f, cy - sz / 2f, pl.right - u * 26f + sz, cy + sz / 2f)
            plus[i].set(pl.right - u * 9f, cy - sz / 2f, pl.right - u * 9f + sz, cy + sz / 2f)
        }

        // o'ng panel: strategiya
        val pr = RectF(w * 0.57f, u * 13f, w - u * 3f, h - u * 3f)
        val sH = u * 8.5f
        Strategy.values().indices.forEach { i ->
            val y = pr.top + u * 8f + i * (sH + u * 1.5f)
            strat[i].set(pr.left + u * 3f, y, pr.left + pr.width() * 0.62f, y + sH)
        }
        bAuto.set(pr.left + pr.width() * 0.64f, pr.top + u * 8f, pr.right - u * 2f, pr.top + u * 8f + sH)
        bStart.set(pr.left + pr.width() * 0.64f, h - u * 16f, pr.right - u * 2f, h - u * 6f)
    }

    override fun onBack(): Boolean { game.setScreen(MapScreen(game)); return true }

    override fun draw(canvas: Canvas) {
        val w = game.width; val h = game.height; val u = game.unit

        Ui.centerText(canvas, "QO'SHIN TO'PLASH", w / 2f, u * 7f, u * 4.6f, Palette.GOLD_LIGHT)
        val rem = remaining()
        Ui.centerText(canvas, "Oltin: ${spent()} / ${c.budget}  (qoldi: $rem)",
            w / 2f, u * 11.2f, u * 3.3f, if (rem >= 0) Palette.GOLD else Palette.RED)

        // chap panel
        val pl = RectF(u * 3f, u * 13f, w * 0.55f, h - u * 3f)
        Ui.panel(canvas, pl, u * 2f)
        val rowH = (pl.height() - u * 6f) / troops.size
        troops.forEachIndexed { i, t ->
            val top = pl.top + u * 3f + i * rowH
            val cy = top + rowH / 2f
            Ui.leftText(canvas, t.uz, pl.left + u * 4f, cy - u * 1.2f, u * 3.4f, Palette.PARCHMENT, true)
            Ui.leftText(canvas, "narx: ${t.cost} oltin  •  hujum ${t.attack.toInt()} / himoya ${t.defense.toInt()}",
                pl.left + u * 4f, cy + u * 3.2f, u * 2.5f, 0xFFB8A06A.toInt())
            // son
            Ui.centerText(canvas, "${army.count(t)}",
                (minus[i].rect.right + plus[i].rect.left) / 2f, cy, u * 4.2f, Palette.GOLD_LIGHT)
            Ui.woodButton(canvas, minus[i], u)
            Ui.woodButton(canvas, plus[i], u)
        }

        // o'ng panel
        val pr = RectF(w * 0.57f, u * 13f, w - u * 3f, h - u * 3f)
        Ui.panel(canvas, pr, u * 2f)
        Ui.leftText(canvas, "Strategiya tanlang:", pr.left + u * 3f, pr.top + u * 5f, u * 3.4f, Palette.GOLD)

        Strategy.values().forEachIndexed { i, s ->
            val b = strat[i]
            // tanlangan strategiyani belgilash
            if (s == game.chosenStrategy) {
                Ui.fill(canvas, RectF(b.rect.left - u, b.rect.top - u, b.rect.right + u, b.rect.bottom + u),
                    0x33C9A227, u * 1.6f)
                Ui.border(canvas, RectF(b.rect.left - u, b.rect.top - u, b.rect.right + u, b.rect.bottom + u),
                    Palette.GOLD_LIGHT, u * 0.5f, u * 1.6f)
            }
            Ui.woodButton(canvas, b, u)
            if (s == c.bestStrategy) {
                Ui.leftText(canvas, "★ tavsiya", b.rect.right + u * 1.5f, b.rect.centerY() + u, u * 2.5f, Palette.GOLD)
            }
        }

        // strategiya tavsifi + kuch bahosi
        val descTop = strat.last().rect.bottom + u * 4f
        val descRect = RectF(pr.left + u * 3f, descTop, pr.right - u * 3f, h - u * 18f)
        Ui.fill(canvas, descRect, 0x55000000, u * 1.5f)
        var ty = descRect.top + u * 4f
        for (ln in Ui.wrap(game.chosenStrategy.tavsif, u * 2.9f, descRect.width() - u * 4f)) {
            Ui.leftText(canvas, ln, descRect.left + u * 2.5f, ty, u * 2.9f, Palette.PARCHMENT); ty += u * 3.8f
        }
        val myPower = army.power(game.chosenStrategy, c.terrain)
        val ratio = if (c.enemyPower > 0) myPower / c.enemyPower else 1f
        val assess = when {
            ratio >= 1.15f -> "Baho: kuchli ustunlik" to Palette.GREEN
            ratio >= 0.95f -> "Baho: teng kuch" to Palette.GOLD
            else -> "Baho: zaif — qo'shin/strategiyani o'zgartiring" to Palette.RED
        }
        ty += u * 1f
        Ui.leftText(canvas, "Kuchingiz: ${myPower.toInt()}   Dushman: ${c.enemyPower.toInt()}",
            descRect.left + u * 2.5f, ty, u * 3f, Palette.PARCHMENT); ty += u * 4f
        Ui.leftText(canvas, assess.first, descRect.left + u * 2.5f, ty, u * 3f, assess.second)

        bAuto.label = "AVTO TO'LDIRISH"
        Ui.woodButton(canvas, bAuto, u)
        bStart.enabled = army.totalSoldiers() > 0
        Ui.woodButton(canvas, bStart, u)
        Ui.woodButton(canvas, bBack, u)
    }
}
