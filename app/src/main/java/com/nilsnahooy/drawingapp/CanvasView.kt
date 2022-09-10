package com.nilsnahooy.drawingapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class CanvasView(context: Context, attrs : AttributeSet): View(context, attrs) {
    private var mDrawPath: DrawPath? = null
    private var mBitmap: Bitmap? = null
    private var mDrawPaint: Paint? = null
    private var mCanvasPaint: Paint? = null
    private var mBrushSize: Float = 0.0f
    private var mColor: Int = Color.BLACK
    private var mCanvas: Canvas? = null
    private val mPaths: ArrayList<DrawPath> = ArrayList()

    init {
        setupDrawing()
    }

    private fun setupDrawing() {
        mDrawPaint = Paint()
        mDrawPath = DrawPath(mColor, mBrushSize)
        mDrawPaint!!.color = mColor
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap!!)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(mBitmap!!,0f, 0f, mCanvasPaint)
        for(p in mPaths) {
            mDrawPaint!!.strokeWidth = p.brushSize
            mDrawPaint!!.color = p.color
            canvas?.drawPath(p, mDrawPaint!!)
        }
        if(!mDrawPath!!.isEmpty) {
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushSize
            mDrawPaint!!.color = mDrawPath!!.color
            canvas?.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = mColor
                mDrawPath!!.brushSize = mBrushSize
                mDrawPath!!.reset()
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.moveTo(touchX, touchY)
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.lineTo(touchX, touchY)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                mPaths.add(mDrawPath!!)
                mDrawPath = DrawPath(mColor, mBrushSize)
            }
            else -> return false
        }
        invalidate()
        return true
    }

    fun setBrushSize(newSize: Float) {
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
        newSize, resources.displayMetrics)
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    fun setBrushColor(newColor: String) {
        mColor = Color.parseColor(newColor)
        mDrawPath = DrawPath(mColor, mBrushSize)
        mDrawPaint!!.color = mColor
    }

    fun undo() {
        if (mPaths.size > 0) {
            mPaths.remove(mPaths[mPaths.size-1])
            invalidate()
        }
    }

    internal inner class DrawPath(var color: Int, var brushSize: Float): Path()
}