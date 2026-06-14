package com.amirtemur.buyukyurishlar

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.cos
import kotlin.math.sin

/**
 * Barcha grafika dasturiy (vektor) chiziladi — tashqi rasmlarsiz,
 * copyright muammosiz va offline. Amir Temur siymosi va jang unitlari shu yerda.
 */
object Art {

    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sp = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
    private val path = Path()

    private fun poly(c: Canvas, color: Int, vararg pts: Float) {
        path.reset(); path.moveTo(pts[0], pts[1])
        var i = 2; while (i < pts.size) { path.lineTo(pts[i], pts[i + 1]); i += 2 }
        path.close()
        p.shader = null; p.style = Paint.Style.FILL; p.color = color
        c.drawPath(path, p)
    }
    private fun circle(c: Canvas, color: Int, cx: Float, cy: Float, r: Float) {
        p.shader = null; p.style = Paint.Style.FILL; p.color = color; c.drawCircle(cx, cy, r, p)
    }
    private fun rrect(c: Canvas, color: Int, l: Float, t: Float, r: Float, b: Float, rad: Float) {
        p.shader = null; p.style = Paint.Style.FILL; p.color = color; c.drawRoundRect(RectF(l, t, r, b), rad, rad, p)
    }
    private fun line(c: Canvas, color: Int, w: Float, x1: Float, y1: Float, x2: Float, y2: Float) {
        sp.shader = null; sp.color = color; sp.strokeWidth = w; c.drawLine(x1, y1, x2, y2, sp)
    }
    private fun ellipse(c: Canvas, color: Int, cx: Float, cy: Float, rx: Float, ry: Float) {
        p.shader = null; p.style = Paint.Style.FILL; p.color = color
        c.drawOval(RectF(cx - rx, cy - ry, cx + rx, cy + ry), p)
    }

    fun glow(c: Canvas, cx: Float, cy: Float, r: Float, color: Int, intensity: Int = 110) {
        p.style = Paint.Style.FILL
        p.shader = RadialGradient(cx, cy, r,
            intArrayOf((intensity shl 24) or (color and 0xFFFFFF), 0x00000000),
            floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
        c.drawCircle(cx, cy, r, p)
        p.shader = null
    }

    // ===================== AMIR TEMUR SIYMOSI =====================
    fun drawTemur(c: Canvas, cx: Float, footY: Float, s: Float, t: Float, alpha: Float = 1f) {
        val sway = sin(t * 1.4f) * s * 0.015f
        val plumeSway = sin(t * 2.2f) * s * 0.03f
        val a = (alpha.coerceIn(0f, 1f) * 255).toInt()

        // aura
        glow(c, cx, footY - s * 0.55f, s * 0.62f, Palette.GOLD, (60 * alpha).toInt())

        val headR = s * 0.072f
        val faceY = footY - s * 0.80f
        val headTop = footY - s * 0.875f
        val shoulderY = footY - s * 0.68f
        val shHalf = s * 0.19f
        val chestY = footY - s * 0.60f
        val waistY = footY - s * 0.46f
        val hemY = footY
        val hemHalf = s * 0.27f

        p.alpha = a; sp.alpha = a

        // cape
        poly(c, Palette.RED_DARK,
            cx - shHalf * 1.05f, shoulderY,
            cx - hemHalf * 1.25f + sway, hemY + s * 0.01f,
            cx + hemHalf * 1.25f + sway, hemY + s * 0.01f,
            cx + shHalf * 1.05f, shoulderY)

        // robe
        poly(c, Palette.ROYAL,
            cx - shHalf, shoulderY, cx - hemHalf, hemY, cx + hemHalf, hemY, cx + shHalf, shoulderY)
        // robe gold trim center
        line(c, Palette.GOLD, s * 0.012f, cx, waistY, cx, hemY)
        // hem band
        rrect(c, Palette.GOLD_DEEP, cx - hemHalf, hemY - s * 0.03f, cx + hemHalf, hemY, s * 0.01f)

        // cuirass (chest armor)
        poly(c, Palette.STEEL,
            cx - shHalf * 0.85f, shoulderY + s * 0.005f,
            cx - s * 0.105f, waistY, cx + s * 0.105f, waistY,
            cx + shHalf * 0.85f, shoulderY + s * 0.005f)
        // lamellar lines
        for (i in 1..3) {
            val yy = shoulderY + (waistY - shoulderY) * i / 4f
            line(c, Palette.GOLD_DEEP, s * 0.008f, cx - s * 0.09f, yy, cx + s * 0.09f, yy)
        }
        // belt
        rrect(c, Palette.GOLD, cx - hemHalf * 0.62f, waistY, cx + hemHalf * 0.62f, waistY + s * 0.04f, s * 0.01f)
        circle(c, Palette.GOLD_LIGHT, cx, waistY + s * 0.02f, s * 0.022f)

        // pauldrons
        ellipse(c, Palette.STEEL_DARK, cx - shHalf, shoulderY, s * 0.075f, s * 0.05f)
        ellipse(c, Palette.STEEL_DARK, cx + shHalf, shoulderY, s * 0.075f, s * 0.05f)
        ellipse(c, Palette.STEEL, cx - shHalf, shoulderY - s * 0.005f, s * 0.06f, s * 0.038f)
        ellipse(c, Palette.STEEL, cx + shHalf, shoulderY - s * 0.005f, s * 0.06f, s * 0.038f)

        // arms (sleeves) to hands on pommel
        val handX = cx; val handY = chestY + s * 0.02f
        line(c, Palette.ROYAL, s * 0.07f, cx - shHalf, shoulderY + s * 0.02f, handX - s * 0.03f, handY)
        line(c, Palette.ROYAL, s * 0.07f, cx + shHalf, shoulderY + s * 0.02f, handX + s * 0.03f, handY)

        // SWORD (point down, hands on pommel)
        poly(c, Palette.STEEL,
            cx - s * 0.028f, waistY, cx + s * 0.028f, waistY,
            cx + s * 0.012f, footY - s * 0.02f, cx, footY, cx - s * 0.012f, footY - s * 0.02f)
        line(c, 0x66FFFFFF, s * 0.006f, cx, waistY, cx, footY - s * 0.03f)
        rrect(c, Palette.GOLD, cx - s * 0.12f, waistY - s * 0.014f, cx + s * 0.12f, waistY + s * 0.014f, s * 0.01f) // crossguard
        rrect(c, Palette.GOLD_DEEP, cx - s * 0.014f, chestY, cx + s * 0.014f, waistY, s * 0.006f) // grip
        circle(c, Palette.GOLD_LIGHT, cx, chestY - s * 0.005f, s * 0.03f) // pommel
        // hands
        circle(c, Palette.SKIN, cx - s * 0.028f, handY, s * 0.026f)
        circle(c, Palette.SKIN, cx + s * 0.028f, handY, s * 0.026f)

        // neck + head
        rrect(c, Palette.SKIN, cx - s * 0.03f, faceY + headR * 0.4f, cx + s * 0.03f, shoulderY, s * 0.01f)
        circle(c, Palette.SKIN, cx, faceY, headR)
        // beard
        poly(c, 0xFF241608.toInt(),
            cx - headR * 0.92f, faceY + headR * 0.15f,
            cx - headR * 0.55f, faceY + headR * 1.55f,
            cx, faceY + headR * 1.95f,
            cx + headR * 0.55f, faceY + headR * 1.55f,
            cx + headR * 0.92f, faceY + headR * 0.15f)
        // eyes
        circle(c, 0xFF1A120A.toInt(), cx - headR * 0.34f, faceY - headR * 0.02f, headR * 0.1f)
        circle(c, 0xFF1A120A.toInt(), cx + headR * 0.34f, faceY - headR * 0.02f, headR * 0.1f)
        line(c, 0xFF241608.toInt(), headR * 0.12f, cx - headR * 0.5f, faceY - headR * 0.28f, cx - headR * 0.16f, faceY - headR * 0.22f)
        line(c, 0xFF241608.toInt(), headR * 0.12f, cx + headR * 0.5f, faceY - headR * 0.28f, cx + headR * 0.16f, faceY - headR * 0.22f)

        // helmet dome
        poly(c, Palette.GOLD,
            cx - headR * 1.12f, faceY - headR * 0.12f,
            cx - headR * 0.6f, headTop - s * 0.015f,
            cx, headTop - s * 0.055f,
            cx + headR * 0.6f, headTop - s * 0.015f,
            cx + headR * 1.12f, faceY - headR * 0.12f)
        rrect(c, Palette.GOLD_DEEP, cx - headR * 1.12f, faceY - headR * 0.42f, cx + headR * 1.12f, faceY - headR * 0.12f, s * 0.008f)
        circle(c, Palette.GOLD_LIGHT, cx, faceY - headR * 0.27f, headR * 0.16f) // brow jewel
        // finial + plume
        line(c, Palette.GOLD_LIGHT, s * 0.012f, cx, headTop - s * 0.055f, cx, headTop - s * 0.085f)
        for (k in 0..2) {
            val off = (k - 1) * s * 0.02f
            poly(c, if (k == 1) Palette.RED else Palette.RED_DARK,
                cx, headTop - s * 0.08f,
                cx + off + plumeSway, headTop - s * 0.14f,
                cx + off * 1.6f + plumeSway, headTop - s * 0.2f,
                cx + off * 0.6f + plumeSway, headTop - s * 0.13f)
        }

        p.alpha = 255; sp.alpha = 255
    }

    // ===================== JANG UNITLARI =====================
    /** type ga qarab mos ikona chizadi. dir=+1 o'ngga, -1 chapga qaraydi */
    fun drawUnit(c: Canvas, type: TroopType, x: Float, baseY: Float, s: Float, cloth: Int, dir: Int, alive: Boolean) {
        if (!alive) { fallen(c, x, baseY, s); return }
        when (type) {
            TroopType.OTLIQ -> cavalry(c, x, baseY, s, cloth, dir)
            TroopType.PIYODA -> infantry(c, x, baseY, s, cloth, dir)
            TroopType.KAMONCHI -> archer(c, x, baseY, s, cloth, dir)
            TroopType.ZIRH -> heavy(c, x, baseY, s, cloth, dir)
            TroopType.QAMAL -> siege(c, x, baseY, s, dir)
        }
    }

    private fun fallen(c: Canvas, x: Float, baseY: Float, s: Float) {
        sp.shader = null; sp.color = 0x55000000; sp.strokeWidth = s * 0.12f
        c.drawLine(x - s * 0.3f, baseY - s * 0.05f, x + s * 0.3f, baseY - s * 0.05f, sp)
    }

    private fun infantry(c: Canvas, x: Float, baseY: Float, s: Float, cloth: Int, dir: Int) {
        val headR = s * 0.16f
        val topY = baseY - s
        // legs
        line(c, Palette.BROWN_DARK, s * 0.1f, x - s * 0.08f, baseY - s * 0.3f, x - s * 0.12f, baseY)
        line(c, Palette.BROWN_DARK, s * 0.1f, x + s * 0.08f, baseY - s * 0.3f, x + s * 0.12f, baseY)
        // body
        rrect(c, cloth, x - s * 0.16f, baseY - s * 0.62f, x + s * 0.16f, baseY - s * 0.26f, s * 0.06f)
        // head + helmet
        circle(c, Palette.SKIN, x, baseY - s * 0.74f, headR)
        poly(c, Palette.STEEL, x - headR * 1.1f, baseY - s * 0.74f, x, baseY - s * 0.96f, x + headR * 1.1f, baseY - s * 0.74f)
        // spear
        line(c, Palette.BROWN_LIGHT, s * 0.05f, x + dir * s * 0.2f, baseY - s * 1.05f, x + dir * s * 0.2f, baseY - s * 0.1f)
        poly(c, Palette.STEEL, x + dir * s * 0.2f - s * 0.05f, baseY - s * 1.02f, x + dir * s * 0.2f, baseY - s * 1.15f, x + dir * s * 0.2f + s * 0.05f, baseY - s * 1.02f)
        // round shield (front)
        circle(c, cloth, x + dir * s * 0.18f, baseY - s * 0.45f, s * 0.17f)
        sp.color = Palette.GOLD; sp.strokeWidth = s * 0.03f; c.drawCircle(x + dir * s * 0.18f, baseY - s * 0.45f, s * 0.17f, sp)
        circle(c, Palette.GOLD, x + dir * s * 0.18f, baseY - s * 0.45f, s * 0.04f)
    }

    private fun archer(c: Canvas, x: Float, baseY: Float, s: Float, cloth: Int, dir: Int) {
        line(c, Palette.BROWN_DARK, s * 0.1f, x - s * 0.08f, baseY - s * 0.3f, x - s * 0.12f, baseY)
        line(c, Palette.BROWN_DARK, s * 0.1f, x + s * 0.08f, baseY - s * 0.3f, x + s * 0.12f, baseY)
        rrect(c, cloth, x - s * 0.15f, baseY - s * 0.62f, x + s * 0.15f, baseY - s * 0.26f, s * 0.06f)
        circle(c, Palette.SKIN, x, baseY - s * 0.74f, s * 0.15f)
        // cap
        poly(c, Palette.BROWN_LIGHT, x - s * 0.16f, baseY - s * 0.78f, x, baseY - s * 0.95f, x + s * 0.16f, baseY - s * 0.78f)
        // bow (front, vertical arc)
        path.reset()
        val bx = x + dir * s * 0.22f
        path.moveTo(bx, baseY - s * 0.95f)
        path.quadTo(bx + dir * s * 0.18f, baseY - s * 0.5f, bx, baseY - s * 0.05f)
        sp.shader = null; sp.color = Palette.BROWN_LIGHT; sp.strokeWidth = s * 0.045f; c.drawPath(path, sp)
        line(c, 0xFFEFE3C0.toInt(), s * 0.015f, bx, baseY - s * 0.95f, bx, baseY - s * 0.05f) // string
        // nocked arrow
        line(c, Palette.PARCHMENT, s * 0.02f, bx, baseY - s * 0.5f, bx + dir * s * 0.28f, baseY - s * 0.5f)
        poly(c, Palette.STEEL, bx + dir * s * 0.28f, baseY - s * 0.54f, bx + dir * s * 0.36f, baseY - s * 0.5f, bx + dir * s * 0.28f, baseY - s * 0.46f)
    }

    private fun heavy(c: Canvas, x: Float, baseY: Float, s: Float, cloth: Int, dir: Int) {
        line(c, Palette.STEEL_DARK, s * 0.13f, x - s * 0.1f, baseY - s * 0.32f, x - s * 0.13f, baseY)
        line(c, Palette.STEEL_DARK, s * 0.13f, x + s * 0.1f, baseY - s * 0.32f, x + s * 0.13f, baseY)
        // bulky armored torso
        rrect(c, Palette.STEEL, x - s * 0.22f, baseY - s * 0.66f, x + s * 0.22f, baseY - s * 0.28f, s * 0.07f)
        rrect(c, cloth, x - s * 0.22f, baseY - s * 0.66f, x + s * 0.22f, baseY - s * 0.58f, s * 0.04f)
        // crested helmet
        circle(c, Palette.STEEL, x, baseY - s * 0.78f, s * 0.17f)
        poly(c, Palette.RED, x - s * 0.03f, baseY - s * 0.92f, x, baseY - s * 1.05f, x + s * 0.03f, baseY - s * 0.92f)
        // large shield
        rrect(c, Palette.STEEL_DARK, x + dir * s * 0.22f - s * 0.07f, baseY - s * 0.66f, x + dir * s * 0.22f + s * 0.07f, baseY - s * 0.18f, s * 0.05f)
        rrect(c, cloth, x + dir * s * 0.22f - s * 0.05f, baseY - s * 0.6f, x + dir * s * 0.22f + s * 0.05f, baseY - s * 0.24f, s * 0.03f)
        circle(c, Palette.GOLD, x + dir * s * 0.22f, baseY - s * 0.42f, s * 0.045f)
        // mace
        line(c, Palette.BROWN_LIGHT, s * 0.05f, x - dir * s * 0.22f, baseY - s * 0.7f, x - dir * s * 0.22f, baseY - s * 0.3f)
        circle(c, Palette.STEEL_DARK, x - dir * s * 0.22f, baseY - s * 0.78f, s * 0.08f)
    }

    private fun cavalry(c: Canvas, x: Float, baseY: Float, s: Float, cloth: Int, dir: Int) {
        val horse = 0xFF5A3A1E.toInt()
        // legs
        for (lx in floatArrayOf(-0.35f, -0.18f, 0.18f, 0.35f)) {
            line(c, horse, s * 0.07f, x + lx * s, baseY - s * 0.45f, x + lx * s, baseY)
        }
        // body
        ellipse(c, horse, x, baseY - s * 0.55f, s * 0.42f, s * 0.2f)
        // tail
        line(c, 0xFF3A2410.toInt(), s * 0.06f, x - dir * s * 0.42f, baseY - s * 0.6f, x - dir * s * 0.55f, baseY - s * 0.3f)
        // neck + head
        poly(c, horse, x + dir * s * 0.3f, baseY - s * 0.62f, x + dir * s * 0.55f, baseY - s * 0.95f, x + dir * s * 0.66f, baseY - s * 0.92f, x + dir * s * 0.42f, baseY - s * 0.55f)
        ellipse(c, horse, x + dir * s * 0.62f, baseY - s * 0.92f, s * 0.12f, s * 0.08f)
        // rider torso
        rrect(c, cloth, x - s * 0.1f, baseY - s * 1.0f, x + s * 0.12f, baseY - s * 0.62f, s * 0.05f)
        circle(c, Palette.SKIN, x, baseY - s * 1.08f, s * 0.13f)
        poly(c, Palette.STEEL, x - s * 0.14f, baseY - s * 1.08f, x, baseY - s * 1.24f, x + s * 0.14f, baseY - s * 1.08f)
        // lance
        line(c, Palette.BROWN_LIGHT, s * 0.04f, x - dir * s * 0.1f, baseY - s * 0.95f, x + dir * s * 0.85f, baseY - s * 1.1f)
        poly(c, Palette.STEEL, x + dir * s * 0.85f, baseY - s * 1.14f, x + dir * s * 0.96f, baseY - s * 1.1f, x + dir * s * 0.85f, baseY - s * 1.06f)
        rrect(c, Palette.RED, x + dir * s * 0.62f, baseY - s * 1.12f, x + dir * s * 0.74f, baseY - s * 1.04f, s * 0.01f) // pennant
    }

    private fun siege(c: Canvas, x: Float, baseY: Float, s: Float, dir: Int) {
        val wood = Palette.BROWN_LIGHT
        // wheels
        circle(c, Palette.BROWN_DARK, x - s * 0.3f, baseY - s * 0.12f, s * 0.16f)
        circle(c, Palette.BROWN_DARK, x + s * 0.3f, baseY - s * 0.12f, s * 0.16f)
        circle(c, wood, x - s * 0.3f, baseY - s * 0.12f, s * 0.08f)
        circle(c, wood, x + s * 0.3f, baseY - s * 0.12f, s * 0.08f)
        // base frame
        rrect(c, wood, x - s * 0.42f, baseY - s * 0.34f, x + s * 0.42f, baseY - s * 0.22f, s * 0.03f)
        // A-frame
        poly(c, wood, x - s * 0.05f, baseY - s * 0.34f, x - s * 0.2f, baseY - s * 0.85f, x - s * 0.08f, baseY - s * 0.85f, x + s * 0.02f, baseY - s * 0.34f)
        // throwing arm
        line(c, 0xFF3A2410.toInt(), s * 0.06f, x - s * 0.15f, baseY - s * 0.8f, x + dir * s * 0.5f, baseY - s * 1.1f)
        circle(c, Palette.STEEL_DARK, x + dir * s * 0.5f, baseY - s * 1.12f, s * 0.1f) // projectile/bucket
        line(c, 0xFF6B5A33.toInt(), s * 0.02f, x - s * 0.15f, baseY - s * 0.8f, x - s * 0.3f, baseY - s * 0.34f) // rope
    }

    // ===================== O'Q (ARROW) =====================
    fun drawArrow(c: Canvas, x: Float, y: Float, angle: Float, len: Float, color: Int) {
        val dx = cos(angle); val dy = sin(angle)
        line(c, color, len * 0.06f, x - dx * len / 2, y - dy * len / 2, x + dx * len / 2, y + dy * len / 2)
        val tipx = x + dx * len / 2; val tipy = y + dy * len / 2
        val a1 = angle + 2.6f; val a2 = angle - 2.6f
        poly(c, Palette.STEEL, tipx, tipy,
            tipx + cos(a1) * len * 0.22f, tipy + sin(a1) * len * 0.22f,
            tipx + cos(a2) * len * 0.22f, tipy + sin(a2) * len * 0.22f)
    }
}
