package com.amirtemur.buyukyurishlar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View

/** Asosiy ranglar (qora, oltin, jigarrang medieval uslub) */
object Palette {
    val BLACK = 0xFF0D0B07.toInt()
    val DARK = 0xFF15110A.toInt()
    val GOLD = 0xFFC9A227.toInt()
    val GOLD_LIGHT = 0xFFE6C04A.toInt()
    val BROWN = 0xFF3B2A18.toInt()
    val BROWN_LIGHT = 0xFF5A4019.toInt()
    val BROWN_DARK = 0xFF241608.toInt()
    val PARCHMENT = 0xFFEDE3C2.toInt()
    val RED = 0xFF9C2B1B.toInt()
    val GREEN = 0xFF4E7A33.toInt()
    val BLUE = 0xFF2C4A6E.toInt()
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
        stroke.color = color
        stroke.strokeWidth = width
        if (radius > 0) canvas.drawRoundRect(rect, radius, radius, stroke)
        else canvas.drawRect(rect, stroke)
    }

    /** Fon: qora-jigarrang radial gradient */
    fun background(canvas: Canvas, w: Int, h: Int) {
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            w * 0.5f, h * 0.42f, maxOf(w, h) * 0.85f,
            intArrayOf(Palette.DARK, Palette.BLACK), floatArrayOf(0f, 1f), Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null
    }

    /** Yog'och uslubidagi panel */
    fun panel(canvas: Canvas, rect: RectF, radius: Float) {
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            rect.left, rect.top, rect.left, rect.bottom,
            intArrayOf(Palette.BROWN_LIGHT, Palette.BROWN_DARK),
            floatArrayOf(0f, 1f), Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.shader = null
        border(canvas, rect, Palette.GOLD, radius * 0.25f + 2f, radius)
    }

    fun woodButton(canvas: Canvas, b: Button, unit: Float) {
        if (!b.visible) return
        val r = b.rect
        val rad = unit * 1.4f
        paint.style = Paint.Style.FILL
        val top = if (b.pressed) Palette.BROWN_DARK else Palette.BROWN_LIGHT
        val bot = if (b.pressed) Palette.BROWN_LIGHT else Palette.BROWN_DARK
        paint.shader = LinearGradient(r.left, r.top, r.left, r.bottom,
            intArrayOf(top, bot), floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
        canvas.drawRoundRect(r, rad, rad, paint)
        paint.shader = null
        val edge = if (b.enabled) Palette.GOLD else 0xFF6B5A33.toInt()
        border(canvas, r, edge, unit * 0.45f, rad)
        val tc = if (b.enabled) Palette.GOLD_LIGHT else 0xFF8C7A4F.toInt()
        centerText(canvas, b.label, r.centerX(), r.centerY(), unit * 4.6f, tc)
    }

    fun centerText(canvas: Canvas, s: String, cx: Float, cy: Float, size: Float, color: Int) {
        text.textSize = size
        text.color = color
        text.textAlign = Paint.Align.CENTER
        val fm = text.fontMetrics
        canvas.drawText(s, cx, cy - (fm.ascent + fm.descent) / 2f, text)
    }

    fun leftText(canvas: Canvas, s: String, x: Float, baselineY: Float, size: Float, color: Int, bold: Boolean = false) {
        text.textSize = size
        text.color = color
        text.typeface = Typeface.create(Typeface.SERIF, if (bold) Typeface.BOLD else Typeface.NORMAL)
        text.textAlign = Paint.Align.LEFT
        canvas.drawText(s, x, baselineY, text)
        text.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }

    /** Matnni belgilangan kenglikka qarab qatorlarga ajratish */
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

    fun stars(canvas: Canvas, cx: Float, cy: Float, size: Float, filled: Int, total: Int = 3) {
        val gap = size * 1.4f
        val startX = cx - gap * (total - 1) / 2f
        for (i in 0 until total) {
            starShape(canvas, startX + i * gap, cy, size / 2f,
                if (i < filled) Palette.GOLD_LIGHT else 0xFF4A4030.toInt())
        }
    }

    private fun starShape(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        val path = Path()
        for (i in 0 until 10) {
            val rr = if (i % 2 == 0) r else r * 0.45f
            val a = Math.PI / 2 + i * Math.PI / 5
            val x = cx + (rr * Math.cos(a)).toFloat()
            val y = cy - (rr * Math.sin(a)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        paint.style = Paint.Style.FILL
        paint.color = color
        canvas.drawPath(path, paint)
    }
}

class Game(val context: Context, var width: Int = 0, var height: Int = 0) {
    val progress = Progress(context)
    val sound = Sound(progress.soundOn)

    /** 1 birlik = ekran balandligining 1% i (responsiv o'lcham) */
    val unit: Float get() = height / 100f

    var screen: Screen = MenuScreen(this)
        private set

    // Jang oqimi uchun umumiy holat
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
    fun update(dt: Float) { screen.update(dt) }
    fun draw(canvas: Canvas) {
        Ui.background(canvas, width, height)
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
