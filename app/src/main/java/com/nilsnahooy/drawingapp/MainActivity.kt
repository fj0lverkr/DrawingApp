package com.nilsnahooy.drawingapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {

    private var canvasView: CanvasView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnBrushSize: ImageButton = findViewById(R.id.ib_open_brush_size_dialog)
        val btnBrushColor: ImageButton = findViewById(R.id.ib_open_brush_color_dialog)
        canvasView = findViewById(R.id.cv_main_canvas)
        canvasView?.setBrushSize(20.0f)

        btnBrushSize.setOnClickListener {
            showBrushSizeDialog()
        }

        btnBrushColor.setOnClickListener {
            showBrushColorDialog()
        }
    }

    private fun showBrushColorDialog() {
        val brushColorDialog = Dialog(this)
        brushColorDialog.setContentView(R.layout.dialog_brush_color)
        brushColorDialog.setTitle(R.string.brush_color_dialog_title)

        val btnBlack: ImageButton = brushColorDialog.findViewById(R.id.ib_brush_color_black)
        val btnWhite: ImageButton = brushColorDialog.findViewById(R.id.ib_brush_color_white)
        val btnGrey: ImageButton = brushColorDialog.findViewById(R.id.ib_brush_color_grey)
        val btnRed: ImageButton = brushColorDialog.findViewById(R.id.ib_brush_color_red)
        val btnGreen: ImageButton = brushColorDialog.findViewById(R.id.ib_brush_color_green)
        val btnBlue: ImageButton = brushColorDialog.findViewById(R.id.ib_brush_color_blue)
        val btnYellow: ImageButton = brushColorDialog.findViewById(R.id.ib_brush_color_yellow)
        val btnPurple: ImageButton = brushColorDialog.findViewById(R.id.ib_brush_color_purple)
        val btnCoffee: ImageButton = brushColorDialog.findViewById(R.id.ib_brush_color_coffee)

        btnBlack.setOnClickListener {
            canvasView?.setBrushColor(R.color.black)
            brushColorDialog.dismiss()
        }

        btnWhite.setOnClickListener {
            canvasView?.setBrushColor(R.color.white)
            brushColorDialog.dismiss()
        }

        btnGrey.setOnClickListener {
            canvasView?.setBrushColor(R.color.grey)
            brushColorDialog.dismiss()
        }

        btnRed.setOnClickListener {
            canvasView?.setBrushColor(R.color.red)
            brushColorDialog.dismiss()
        }

        btnGreen.setOnClickListener {
            canvasView?.setBrushColor(R.color.green)
            brushColorDialog.dismiss()
        }

        btnBlue.setOnClickListener {
            canvasView?.setBrushColor(R.color.blue)
            brushColorDialog.dismiss()
        }

        btnYellow.setOnClickListener {
            canvasView?.setBrushColor(R.color.yellow)
            brushColorDialog.dismiss()
        }

        btnPurple.setOnClickListener {
            canvasView?.setBrushColor(R.color.purple)
            brushColorDialog.dismiss()
        }

        btnCoffee.setOnClickListener {
            canvasView?.setBrushColor(R.color.coffee)
            brushColorDialog.dismiss()
        }

        brushColorDialog.show()
    }

    private fun showBrushSizeDialog() {
        val brushSizeDialog = Dialog(this)
        brushSizeDialog.setContentView(R.layout.dialog_brush_size)
        brushSizeDialog.setTitle(R.string.brush_size_dialog_title)

        val btnXSmall: ImageButton = brushSizeDialog.findViewById(R.id.ib_brush_size_extra_small)
        val btnSmall: ImageButton = brushSizeDialog.findViewById(R.id.ib_brush_size_small)
        val btnMedium: ImageButton = brushSizeDialog.findViewById(R.id.ib_brush_size_medium)
        val btnLarge: ImageButton = brushSizeDialog.findViewById(R.id.ib_brush_size_large)

        btnXSmall.setOnClickListener {
            canvasView?.setBrushSize(5f)
            brushSizeDialog.dismiss()
        }

        btnSmall.setOnClickListener {
           canvasView?.setBrushSize(10f)
           brushSizeDialog.dismiss()
       }

        btnMedium.setOnClickListener {
            canvasView?.setBrushSize(20f)
            brushSizeDialog.dismiss()
        }

        btnLarge.setOnClickListener {
            canvasView?.setBrushSize(30f)
            brushSizeDialog.dismiss()
        }

        brushSizeDialog.show()
    }
}