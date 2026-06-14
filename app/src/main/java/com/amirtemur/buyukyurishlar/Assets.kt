package com.amirtemur.buyukyurishlar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

/**
 * Ixtiyoriy rasm yuklovchi.
 * Agar `app/src/main/res/drawable/` ichiga quyidagi nomlar bilan PNG/JPG qo'shsangiz,
 * o'yin ularni AVTOMATIK ishlatadi (kodni o'zgartirmasdan):
 *   - temur.png      -> Amir Temur siymosi (Splash, Menyu, Yakun)
 *   - ending.png     -> Yakuniy fon (ixtiyoriy)
 * Rasm bo'lmasa, dasturiy (vektor) grafika ishlatiladi.
 *
 * Eslatma: bitta "birlashgan" rasmni bo'laklarga ajratish uchun uning joylashuvi
 * (qaysi bo'lak qayerda) kerak — alohida nomli PNG lar bilan yuklash eng oson yo'l.
 */
object Assets {
    private val cache = HashMap<String, Bitmap?>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }

    fun bmp(ctx: Context, name: String): Bitmap? {
        if (cache.containsKey(name)) return cache[name]
        val b = try {
            val id = ctx.resources.getIdentifier(name, "drawable", ctx.packageName)
            if (id != 0) BitmapFactory.decodeResource(ctx.resources, id) else null
        } catch (_: Throwable) { null }
        cache[name] = b
        return b
    }

    /** Balandligi bo'yicha moslab, pastki markazi (cx, footY) bo'lgan holda chizadi */
    fun drawFitHeight(c: Canvas, bmp: Bitmap, cx: Float, footY: Float, targetH: Float, alpha: Float) {
        val r = bmp.width.toFloat() / bmp.height.toFloat().coerceAtLeast(1f)
        val ww = targetH * r
        val dst = RectF(cx - ww / 2f, footY - targetH, cx + ww / 2f, footY)
        paint.alpha = (alpha.coerceIn(0f, 1f) * 255).toInt()
        c.drawBitmap(bmp, null, dst, paint)
        paint.alpha = 255
    }

    /** Belgilangan to'rtburchakni to'liq qoplaydi */
    fun drawCover(c: Canvas, bmp: Bitmap, dst: RectF, alpha: Float) {
        paint.alpha = (alpha.coerceIn(0f, 1f) * 255).toInt()
        c.drawBitmap(bmp, null, dst, paint)
        paint.alpha = 255
    }
}
