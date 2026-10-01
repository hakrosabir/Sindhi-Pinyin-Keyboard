package org.sindhipinyin.keyboard.ime

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

/** Small monochrome icons, drawn locally so appearance does not depend on emoji fonts. */
internal class KeyboardIcon(private val symbol: String, color: Int) : Drawable() {
    private val pen = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color; style = Paint.Style.STROKE; strokeWidth = 1.7f
        strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
    }
    override fun draw(canvas: Canvas) {
        canvas.save()
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
        canvas.scale(bounds.width() / 24f, bounds.height() / 24f)
        fun line(x: Float, y: Float, xx: Float, yy: Float) = canvas.drawLine(x, y, xx, yy, pen)
        fun path(vararg points: Float) {
            val p = Path(); p.moveTo(points[0], points[1])
            for (i in 2 until points.size step 2) p.lineTo(points[i], points[i + 1])
            canvas.drawPath(p, pen)
        }
        when (symbol) {
            "🌐" -> {
                canvas.drawCircle(12f, 12f, 9f, pen)
                canvas.drawOval(8f, 3f, 16f, 21f, pen)
                line(3f, 12f, 21f, 12f)
            }
            "⚙" -> {
                line(4f, 6f, 20f, 6f); line(4f, 12f, 20f, 12f); line(4f, 18f, 20f, 18f)
                line(8f, 3f, 8f, 9f); line(16f, 9f, 16f, 15f); line(10f, 15f, 10f, 21f)
            }
            "⌫" -> { path(9f, 5f, 21f, 5f, 21f, 19f, 9f, 19f, 2f, 12f, 9f, 5f); line(11f, 9f, 17f, 15f); line(17f, 9f, 11f, 15f) }
            "⇧" -> path(12f, 3f, 22f, 13f, 16f, 13f, 16f, 21f, 8f, 21f, 8f, 13f, 2f, 13f, 12f, 3f)
            "▾" -> path(6f, 9f, 12f, 15f, 18f, 9f)
            "▴" -> path(6f, 15f, 12f, 9f, 18f, 15f)
        }
        canvas.restore()
    }
    override fun setAlpha(alpha: Int) { pen.alpha = alpha; invalidateSelf() }
    override fun setColorFilter(filter: ColorFilter?) { pen.colorFilter = filter; invalidateSelf() }
    @Suppress("DEPRECATION") override fun getOpacity() = PixelFormat.TRANSLUCENT
}
