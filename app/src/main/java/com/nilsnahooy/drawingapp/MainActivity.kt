package com.nilsnahooy.drawingapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {

    private var canvasView: CanvasView? = null
    private var btnBrushSize: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        canvasView = findViewById(R.id.cv_main_canvas)
        btnBrushSize = findViewById(R.id.ib_open_brush_size_dialog)
        canvasView?.setBrushSize(20.0f)

        btnBrushSize?.setOnClickListener {
            showBrushSizeDialog()
        }
    }

    private fun showBrushSizeDialog() {
        val brushSizeDialog = Dialog(this)
        brushSizeDialog.setContentView(R.layout.dialog_brush_size)
        brushSizeDialog.setTitle(R.string.brush_size_dialog_title)

        val btnSmall: ImageButton = brushSizeDialog.findViewById(R.id.ib_brush_size_small)
        val btnMedium: ImageButton = brushSizeDialog.findViewById(R.id.ib_brush_size_medium)
        val btnLarge: ImageButton = brushSizeDialog.findViewById(R.id.ib_brush_size_large)

        btnSmall.setOnClickListener {
           canvasView?.setBrushSize(10f)
           brushSizeDialog.dismiss()
       }

        btnMedium.setOnClickListener {
            canvasView?.setBrushSize(15f)
            brushSizeDialog.dismiss()
        }

        btnLarge.setOnClickListener {
            canvasView?.setBrushSize(20f)
            brushSizeDialog.dismiss()
        }

        brushSizeDialog.show()
    }
}