package com.amirtemur.buyukyurishlar

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.math.sin

class MapScreen(game: Game) : Screen(game) {

    private val bMenu = Button("‹ MENYU")
    private val bStart = Button("BOSHLASH")
    private val bClose = Button("ORQAGA")
    private var t = 0f
    private var card: Campaign? = null

    private val regions = listOf(
        Triple("XORAZM", 0.30f, 0.36f),
        Triple("SAMARQAND", 0.47f, 0.50f),
        Triple("BUXORO", 0.40f, 0.46f),
        Triple("ERON", 0.30f, 0.58f),
        Triple("HINDISTON", 0.64f, 0.72f),
        Triple("ANQARA", 0.10f, 0.40f),
        Triple("DASHTI QIPCHOQ", 0.44f, 0.18f),
        Triple("XITOY YO'LI", 0.58f, 0.26f)
    )

    init {
        bMenu.onClick = { game.setScreen(MenuScreen(game)) }
        bClose.onClick = { card = null }
        bStart.onClick = {
            val sel = card
            if (sel != null) { game.sound.confirm(); startCampaign(sel) }
        }
        buttons.addAll(listOf(bMenu, bStart, bClose))
    }

    override fun onShow() { card = null }

    override fun onResize(w: Int, h: Int) {
        val u = game.unit
        bMenu.set(u * 3f, u * 3f, u * 22f, u * 11f)
        // karta tugmalari
        val cardW = w * 0.7f
        val cx = w / 2f
        bStart.set(cx + u * 1f, h * 0.72f, cx + cardW / 2f - u * 3f, h * 0.72f + u * 10f)
        bClose.set(cx - cardW / 2f + u * 3f, h * 0.72f, cx - u * 1f, h * 0.72f + u * 10f)
    }

    private fun nodeRadius() = game.unit * 3.2f

    private fun isUnlocked(c: Campaign) = c.id <= game.progress.unlocked
    private fun isDone(c: Campaign) = game.progress.stars(c.id) > 0

    override fun update(dt: Float) { t += dt }

    override fun onUp(x: Float, y: Float) {
        if (card != null) {
            // faqat karta tugmalari ishlaydi
            val hit = buttons.filter { it == bStart || it == bClose }.firstOrNull { it.pressed && it.contains(x, y) }
            buttons.forEach { it.pressed = false }
            if (hit != null) { game.sound.click(); hit.onClick() }
            return
        }
        val hitBtn = buttons.firstOrNull { it.pressed && it.contains(x, y) }
        buttons.forEach { it.pressed = false }
        if (hitBtn != null) { game.sound.click(); hitBtn.onClick(); return }
        // tugun tanlash
        val r = nodeRadius() * 1.8f
        for (c in Data.campaigns) {
            val nx = c.mapX * game.width
            val ny = c.mapY * game.height
            if ((x - nx) * (x - nx) + (y - ny) * (y - ny) <= r * r) {
                if (isUnlocked(c)) { game.sound.click(); card = c }
                else game.sound.defeat()
                return
            }
        }
    }

    override fun onDown(x: Float, y: Float) {
        if (card != null) {
            listOf(bStart, bClose).forEach { it.pressed = it.contains(x, y) }
        } else buttons.forEach { it.pressed = it.contains(x, y) }
    }

    override fun onBack(): Boolean {
        if (card != null) { card = null; return true }
        game.setScreen(MenuScreen(game)); return true
    }

    private fun startCampaign(c: Campaign) {
        game.selectedCampaign = c
        game.setScreen(CutsceneScreen(game, c.cutscene,
            if (c.isFinal) "YAKUN" else "QO'SHIN TO'PLASH", c.title) {
            if (c.isFinal) game.setScreen(EndingScreen(game))
            else { game.playerArmy = Army(); game.setScreen(ArmyScreen(game)) }
        })
    }

    override fun draw(canvas: Canvas) {
        val w = game.width; val h = game.height; val u = game.unit
        drawMap(canvas, w, h, u)

        // marshrut chiziqlari
        val p = Ui.paint
        p.style = Paint.Style.STROKE
        for (i in 0 until Data.campaigns.size - 1) {
            val a = Data.campaigns[i]; val b = Data.campaigns[i + 1]
            val done = isDone(b)
            p.color = if (done) Palette.GOLD else 0x553B2A18
            p.strokeWidth = u * 0.7f
            p.pathEffect = DashPathEffect(floatArrayOf(u * 1.5f, u * 1.5f), 0f)
            canvas.drawLine(a.mapX * w, a.mapY * h, b.mapX * w, b.mapY * h, p)
        }
        p.pathEffect = null

        // tugunlar
        for (c in Data.campaigns) drawNode(canvas, c, u)

        // sarlavha
        Ui.centerText(canvas, "BUYUK YURISHLAR XARITASI", w / 2f, u * 7f, u * 4.6f, Palette.GOLD_LIGHT)
        drawButtons(canvas)
        // progress
        val total = Data.campaigns.size
        Ui.centerText(canvas, "Bosqich: ${game.progress.unlocked.coerceAtMost(total)} / $total",
            w - u * 16f, u * 6f, u * 3.2f, Palette.GOLD)

        if (card != null) drawCard(canvas, card!!, w, h, u)
    }

    private fun drawNode(canvas: Canvas, c: Campaign, u: Float) {
        val x = c.mapX * game.width; val y = c.mapY * game.height
        val rad = nodeRadius()
        val p = Ui.paint
        p.style = Paint.Style.FILL
        when {
            isDone(c) -> p.color = Palette.GREEN
            isUnlocked(c) -> {
                val pulse = 0.5f + 0.5f * sin(t * 3f)
                p.style = Paint.Style.STROKE
                p.strokeWidth = u * 0.6f
                p.color = Palette.GOLD_LIGHT
                p.alpha = (90 + pulse * 120).toInt().coerceIn(0, 255)
                canvas.drawCircle(x, y, rad + u * (1.5f + pulse * 1.5f), p)
                p.alpha = 255
                p.style = Paint.Style.FILL
                p.color = Palette.GOLD
            }
            else -> p.color = Palette.BROWN_DARK
        }
        canvas.drawCircle(x, y, rad, p)
        p.alpha = 255
        Ui.border(canvas, RectF(x - rad, y - rad, x + rad, y + rad), Palette.PARCHMENT, u * 0.4f, rad)

        // belgilar
        when {
            !isUnlocked(c) -> drawLock(canvas, x, y, rad * 0.9f, u)
            isDone(c) -> Ui.centerText(canvas, "✓", x, y, rad * 1.3f, Palette.PARCHMENT)
            else -> Ui.centerText(canvas, if (c.isFinal) "★" else "${c.id}", x, y, rad * 1.1f, Palette.BLACK)
        }
        // nom
        Ui.centerText(canvas, shortName(c), x, y + rad + u * 3.2f, u * 2.6f,
            if (isUnlocked(c)) Palette.PARCHMENT else 0xFF6B5A33.toInt())
        if (isDone(c)) Ui.stars(canvas, x, y - rad - u * 2.5f, u * 2.4f, game.progress.stars(c.id))
    }

    private fun drawLock(canvas: Canvas, cx: Float, cy: Float, s: Float, u: Float) {
        val p = Ui.paint
        p.style = Paint.Style.STROKE
        p.strokeWidth = u * 0.5f
        p.color = Palette.PARCHMENT
        // shakl (qulf yoyi)
        canvas.drawArc(RectF(cx - s * 0.35f, cy - s * 0.7f, cx + s * 0.35f, cy), 180f, 180f, false, p)
        p.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(cx - s * 0.5f, cy - s * 0.2f, cx + s * 0.5f, cy + s * 0.55f), u * 0.6f, u * 0.6f, p)
    }

    private fun shortName(c: Campaign): String = when (c.id) {
        0 -> "Kesh"; 1 -> "Movarounnahr"; 2 -> "Xorazm"; 3 -> "Oltin O'rda"
        4 -> "Hindiston"; 5 -> "Anqara"; else -> "So'nggi yurish"
    }

    private fun drawMap(canvas: Canvas, w: Int, h: Int, u: Float) {
        val map = RectF(u * 2f, u * 13f, w - u * 2f, h - u * 2f)
        val p = Ui.paint
        p.style = Paint.Style.FILL
        p.color = 0xFF161009.toInt()
        canvas.drawRoundRect(map, u * 2f, u * 2f, p)
        Ui.border(canvas, map, Palette.BROWN_LIGHT, u * 0.5f, u * 2f)
        // hudud nomlari (fon)
        for (r in regions) {
            Ui.centerText(canvas, r.first, r.second * w, r.third * h, u * 2.8f, 0x55C9A227)
        }
    }

    private fun drawCard(canvas: Canvas, c: Campaign, w: Int, h: Int, u: Float) {
        // qorong'i fon
        Ui.paint.style = Paint.Style.FILL
        Ui.paint.color = 0xCC000000.toInt()
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), Ui.paint)

        val cardW = w * 0.7f
        val rect = RectF(w / 2f - cardW / 2f, h * 0.10f, w / 2f + cardW / 2f, h * 0.86f)
        Ui.panel(canvas, rect, u * 2.5f)

        val left = rect.left + u * 5f
        var y = rect.top + u * 8f
        Ui.centerText(canvas, c.title, w / 2f, y, u * 4.4f, Palette.GOLD_LIGHT); y += u * 7f
        val rows = listOf(
            "Yil: ${c.years}",
            "Hudud: ${c.region}",
            "Dushman: ${c.enemy}",
            "Hudud turi: ${c.terrain.uz}"
        )
        for (r in rows) {
            Ui.leftText(canvas, r, left, y, u * 3.3f, Palette.PARCHMENT); y += u * 5f
        }
        y += u * 1f
        Ui.leftText(canvas, "Tarixiy sabab:", left, y, u * 3.3f, Palette.GOLD); y += u * 4.5f
        for (ln in Ui.wrap(c.reason, u * 3.1f, cardW - u * 10f)) {
            Ui.leftText(canvas, ln, left, y, u * 3.1f, Palette.PARCHMENT); y += u * 4.2f
        }
        y += u * 1f
        if (!c.isFinal) {
            Ui.leftText(canvas, "Tavsiya etilgan strategiya: ${c.bestStrategy.uz}",
                left, y, u * 3.1f, Palette.GOLD)
        }

        drawButtons(canvas)
    }
}
