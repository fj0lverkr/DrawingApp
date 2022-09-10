package com.nilsnahooy.drawingapp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.iterator


class MainActivity : AppCompatActivity() {

    private var canvasView: CanvasView? = null

    private val permissionResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()){
            result ->
            for (r in result) {
                val name = r.key
                val isAllowed = r.value
                if(isAllowed) {
                   browseForImageAndSetBackground()
                }else {

                    Toast.makeText(this, getString(R.string.sb_perm_denied, name),
                        Toast.LENGTH_LONG).show()
                }
            }

        }

    private var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
            result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val iv: ImageView = findViewById(R.id.iv_background)
                iv.setImageURI(data?.data)
        }
    }

    private fun browseForImageAndSetBackground(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    private fun isExternalStorageAvailable(): Boolean {
        val extStorageState = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == extStorageState
    }

    private fun showRationaleDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(getString(R.string.label_cancel)){ dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnBrushSize: ImageButton = findViewById(R.id.ib_open_brush_size_dialog)
        val btnBrushColor: ImageButton = findViewById(R.id.ib_open_brush_color_dialog)
        val btnBrowseForImage: ImageButton = findViewById(R.id.ib_open_gallery)
        val btnUndo:ImageButton = findViewById(R.id.ib_undo)
        val btnRedo:ImageButton = findViewById(R.id.ib_redo)

        canvasView = findViewById(R.id.cv_main_canvas)
        canvasView?.setBrushSize(20.0f)

        if (!isExternalStorageAvailable()) {
            btnBrowseForImage.isEnabled = false
        }

        btnBrowseForImage.setOnClickListener {
            getImageFromStorage()
        }

        btnBrushSize.setOnClickListener {
            showBrushSizeDialog()
        }

        btnBrushColor.setOnClickListener {
            showBrushColorDialog()
        }

        btnUndo.setOnClickListener {
            canvasView?.undo()
        }

        btnRedo.setOnClickListener {
            canvasView?.redo()
        }
    }

    private fun getImageFromStorage() {
        //handle permissions first
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(
                Manifest.permission.READ_EXTERNAL_STORAGE
        )) {
            showRationaleDialog(getString(R.string.perm_ext_storage_rat_title),
            getString(R.string.perm_ext_storage_rat_msg))
        }else{
            permissionResultLauncher.launch(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }

    private fun showBrushColorDialog() {
        val brushColorDialog = Dialog(this)
        brushColorDialog.setContentView(R.layout.dialog_brush_color)
        brushColorDialog.setTitle(R.string.brush_color_dialog_title)

        val llPalette: LinearLayoutCompat = brushColorDialog.findViewById(R.id.ll_palette)
        for(v in llPalette) {
            val ib = v as ImageButton
            ib.setOnClickListener {
                canvasView?.setBrushColor(it.tag.toString())
                brushColorDialog.dismiss()
            }
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