package com.amirtemur.buyukyurishlar

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random

/** Asosiy ranglar (qora, oltin, jigarrang medieval uslub + metall) */
object Palette {
    val BLACK = 0xFF0B0906.toInt()
    val DARK = 0xFF17120B.toInt()
    val NIGHT = 0xFF0F1620.toInt()
    val GOLD = 0xFFC9A227.toInt()
    val GOLD_LIGHT = 0xFFF0D060.toInt()
    val GOLD_DEEP = 0xFF8A6A12.toInt()
    val BROWN = 0xFF3B2A18.toInt()
    val BROWN_LIGHT = 0xFF6A4D24.toInt()
    val BROWN_DARK = 0xFF221509.toInt()
    val PARCHMENT = 0xFFF1E7C8.toInt()
    val RED = 0xFFB0301C.toInt()
    val RED_DARK = 0xFF6E1C12.toInt()
    val GREEN = 0xFF5A8A38.toInt()
    val BLUE = 0xFF2C4A6E.toInt()
    val STEEL = 0xFFB9C0CA.toInt()
    val STEEL_DARK = 0xFF6E7681.toInt()
    val SKIN = 0xFFD9A878.toInt()
    val ROYAL = 0xFF2E5A52.toInt()
    val SHADOW = 0x66000000
}

/** Bosiladigan tugma (yog'och uslubida) */
class Button(
    var label: String,
    var onClick: () -> Unit = {}
) {
    val rect = RectF()
    var enabled = true
    var pressed = false
    var visible = true
    fun set(l: Float, t: Float, r: Float, b: Float) { rect.set(l, t, r, b) }
    fun contains(x: Float, y: Float) = visible && enabled && rect.contains(x, y)
}

abstract class Screen(val game: Game) {
    protected val buttons = ArrayList<Button>()

    open fun onShow() {}
    open fun onResize(w: Int, h: Int) {}
    open fun update(dt: Float) {}
    abstract fun draw(canvas: Canvas)

    open fun onDown(x: Float, y: Float) {
        buttons.forEach { it.pressed = it.contains(x, y) }
    }
    open fun onMove(x: Float, y: Float) {
        buttons.forEach { if (it.pressed && !it.contains(x, y)) it.pressed = false }
    }
    open fun onUp(x: Float, y: Float) {
        val hit = buttons.firstOrNull { it.pressed && it.contains(x, y) }
        buttons.forEach { it.pressed = false }
        if (hit != null) { game.sound.click(); hit.onClick() }
    }
    open fun onBack(): Boolean = false

    protected fun drawButtons(canvas: Canvas) {
        buttons.forEach { Ui.woodButton(canvas, it, game.unit) }
    }
}

/** Fon uchun ko'tarilayotgan uchqunlar (atmosfera) */
class Ambient {
    private class Ember(var x: Float, var y: Float, var vy: Float, var r: Float, var a: Float)
    private val embers = ArrayList<Ember>()
    private var w = 0; private var h = 0

    private fun seed(width: Int, height: Int) {
        w = width; h = height
        embers.clear()
        repeat(40) {
            embers.add(Ember(Random.nextFloat() * width, Random.nextFloat() * height,
                -(8f + Random.nextFloat() * 22f), 1f + Random.nextFloat() * 2.5f,
                0.1f + Random.nextFloat() * 0.4f))
        }
    }

    fun update(dt: Float, width: Int, height: Int) {
        if (width != w || height != h || embers.isEmpty()) seed(width, height)
        for (e in embers) {
            e.y += e.vy * dt * (height / 100f)
            e.x += (Random.nextFloat() - 0.5f) * dt * 10f
            if (e.y < -10) { e.y = height + 10f; e.x = Random.nextFloat() * width }
        }
    }

    fun draw(canvas: Canvas) {
        val p = Ui.paint
        p.shader = null
        p.style = Paint.Style.FILL
        for (e in embers) {
            p.color = Palette.GOLD_LIGHT
            p.alpha = (e.a * 120).toInt().coerceIn(0, 255)
            canvas.drawCircle(e.x, e.y, e.r, p)
        }
        p.alpha = 255
    }
}

/** Chizish yordamchilari */
object Ui {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }
    val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }

    fun fill(canvas: Canvas, rect: RectF, color: Int, radius: Float = 0f) {
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = color
        if (radius > 0) canvas.drawRoundRect(rect, radius, radius, paint)
        else canvas.drawRect(rect, paint)
    }

    fun border(canvas: Canvas, rect: RectF, color: Int, width: Float, radius: Float = 0f) {
        stroke.shader = null
        stroke.color = color
        stroke.strokeWidth = width
        if (radius > 0) canvas.drawRoundRect(rect, radius, radius, stroke)
        else canvas.drawRect(rect, stroke)
    }

    /** Fon: chuqur radial gradient + vinetka */
    fun background(canvas: Canvas, w: Int, h: Int) {
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            w * 0.5f, h * 0.40f, maxOf(w, h) * 0.9f,
            intArrayOf(Palette.DARK, Palette.BLACK), floatArrayOf(0f, 1f), Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null
    }

    /** Zamonaviy panel: soya + gradient + ikki qavat oltin hoshiya */
    fun panel(canvas: Canvas, rect: RectF, radius: Float) {
        // soya
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = Palette.SHADOW
        val sh = RectF(rect.left + radius * 0.2f, rect.top + radius * 0.3f,
            rect.right + radius * 0.2f, rect.bottom + radius * 0.4f)
        canvas.drawRoundRect(sh, radius, radius, paint)
        // tana
        paint.shader = LinearGradient(
            rect.left, rect.top, rect.left, rect.bottom,
            intArrayOf(0xFF2A1E11.toInt(), Palette.BROWN_DARK),
            floatArrayOf(0f, 1f), Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.shader = null
        border(canvas, rect, Palette.GOLD_DEEP, radius * 0.18f + 3f, radius)
        val inner = RectF(rect.left + radius * 0.35f, rect.top + radius * 0.35f,
            rect.right - radius * 0.35f, rect.bottom - radius * 0.35f)
        border(canvas, inner, Palette.GOLD, 1.5f, radius * 0.7f)
    }

    /** Shisha/charm panel (subtitr va ma'lumot uchun) */
    fun glassPanel(canvas: Canvas, rect: RectF, radius: Float, alpha: Int = 0xCC) {
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = (alpha shl 24) or 0x0A0805
        canvas.drawRoundRect(rect, radius, radius, paint)
        border(canvas, rect, Palette.GOLD, 2f, radius)
    }

    fun woodButton(canvas: Canvas, b: Button, unit: Float) {
        if (!b.visible) return
        val r = b.rect
        val rad = unit * 1.6f
        // soya
        paint.shader = null
        paint.style = Paint.Style.FILL
        if (b.enabled) {
            paint.color = Palette.SHADOW
            canvas.drawRoundRect(RectF(r.left, r.top + unit * 0.6f, r.right, r.bottom + unit * 0.9f), rad, rad, paint)
        }
        val top = if (b.pressed) Palette.BROWN_DARK else 0xFF7A5A2C.toInt()
        val bot = if (b.pressed) 0xFF4A3415.toInt() else Palette.BROWN_DARK
        paint.shader = LinearGradient(r.left, r.top, r.left, r.bottom,
            intArrayOf(top, bot), floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
        if (!b.enabled) { paint.shader = null; paint.color = 0xFF2E2616.toInt() }
        canvas.drawRoundRect(r, rad, rad, paint)
        paint.shader = null
        // yuqori yorug'lik chizig'i
        if (b.enabled && !b.pressed) {
            paint.color = 0x33FFFFFF
            canvas.drawRoundRect(RectF(r.left + rad, r.top + unit * 0.5f, r.right - rad, r.top + unit * 1.6f), rad, rad, paint)
        }
        val edge = if (b.enabled) Palette.GOLD else 0xFF5A4D2E.toInt()
        border(canvas, r, edge, unit * 0.5f, rad)
        val tc = if (b.enabled) Palette.GOLD_LIGHT else 0xFF7A6B45.toInt()
        centerText(canvas, b.label, r.centerX(), r.centerY(), unit * 4.4f, tc)
    }

    fun centerText(canvas: Canvas, s: String, cx: Float, cy: Float, size: Float, color: Int, shadow: Boolean = false) {
        text.textSize = size
        text.textAlign = Paint.Align.CENTER
        val fm = text.fontMetrics
        val by = cy - (fm.ascent + fm.descent) / 2f
        if (shadow) { text.color = 0xCC000000.toInt(); canvas.drawText(s, cx + size * 0.04f, by + size * 0.05f, text) }
        text.color = color
        canvas.drawText(s, cx, by, text)
    }

    fun leftText(canvas: Canvas, s: String, x: Float, baselineY: Float, size: Float, color: Int, bold: Boolean = false) {
        text.textSize = size
        text.color = color
        text.typeface = Typeface.create(Typeface.SERIF, if (bold) Typeface.BOLD else Typeface.NORMAL)
        text.textAlign = Paint.Align.LEFT
        canvas.drawText(s, x, baselineY, text)
        text.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }

    fun wrap(s: String, size: Float, maxWidth: Float): List<String> {
        text.textSize = size
        val words = s.split(" ")
        val lines = ArrayList<String>()
        var cur = StringBuilder()
        for (w in words) {
            val test = if (cur.isEmpty()) w else "$cur $w"
            if (text.measureText(test) > maxWidth && cur.isNotEmpty()) {
                lines.add(cur.toString()); cur = StringBuilder(w)
            } else cur = StringBuilder(test)
        }
        if (cur.isNotEmpty()) lines.add(cur.toString())
        return lines
    }

    fun measure(s: String, size: Float): Float { text.textSize = size; return text.measureText(s) }

    fun stars(canvas: Canvas, cx: Float, cy: Float, size: Float, filled: Int, total: Int = 3) {
        val gap = size * 1.5f
        val startX = cx - gap * (total - 1) / 2f
        for (i in 0 until total) {
            starShape(canvas, startX + i * gap, cy, size / 2f,
                if (i < filled) Palette.GOLD_LIGHT else 0xFF4A4030.toInt())
        }
    }

    fun starShape(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        val path = android.graphics.Path()
        for (i in 0 until 10) {
            val rr = if (i % 2 == 0) r else r * 0.45f
            val a = Math.PI / 2 + i * Math.PI / 5
            val x = cx + (rr * Math.cos(a)).toFloat()
            val y = cy - (rr * Math.sin(a)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = color
        canvas.drawPath(path, paint)
    }
}

class Game(val context: Context, var width: Int = 0, var height: Int = 0) {
    val progress = Progress(context)
    val sound = Sound(progress.soundOn)
    val ambient = Ambient()

    val unit: Float get() = height / 100f

    var screen: Screen = SplashScreen(this)
        private set

    var selectedCampaign: Campaign? = null
    var playerArmy: Army = Army()
    var chosenStrategy: Strategy = Strategy.FRONTAL
    var lastResult: BattleResult? = null

    fun setScreen(s: Screen) {
        screen = s
        if (width > 0) s.onResize(width, height)
        s.onShow()
    }

    fun onResize(w: Int, h: Int) { width = w; height = h; screen.onResize(w, h) }
    fun update(dt: Float) { ambient.update(dt, width, height); screen.update(dt) }
    fun draw(canvas: Canvas) {
        Ui.background(canvas, width, height)
        ambient.draw(canvas)
        screen.draw(canvas)
    }
    fun onBackPressed(): Boolean = screen.onBack()
}

class GameView(context: Context) : View(context) {
    val game = Game(context)
    private var last = 0L

    private val callback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (last == 0L) last = frameTimeNanos
            var dt = (frameTimeNanos - last) / 1_000_000_000f
            last = frameTimeNanos
            if (dt > 0.05f) dt = 0.05f
            game.update(dt)
            invalidate()
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    init { isFocusable = true; isFocusableInTouchMode = true }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        last = 0L
        Choreographer.getInstance().postFrameCallback(callback)
    }

    override fun onDetachedFromWindow() {
        Choreographer.getInstance().removeFrameCallback(callback)
        game.sound.release()
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        super.onSizeChanged(w, h, ow, oh)
        game.onResize(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        if (game.width == 0) game.onResize(width, height)
        game.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> game.screen.onDown(event.x, event.y)
            MotionEvent.ACTION_MOVE -> game.screen.onMove(event.x, event.y)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> game.screen.onUp(event.x, event.y)
        }
        return true
    }

    fun handleBack(): Boolean = game.onBackPressed()
}
