package com.nilsnahooy.drawingapp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.iterator
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private var canvasView: CanvasView? = null
    private var progressDialog: Dialog? = null

    private val permissionResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()){
            result ->
            for (r in result) {
                val name = r.key
                val isAllowed = r.value
                if(isAllowed) {
                   Toast.makeText(this, getString(R.string.sb_perm_allowed, name),
                    Toast.LENGTH_LONG).show()
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

    private fun saveDrawingToMedia(isShare: Boolean) {
        showProgressDialog()
        lifecycleScope.launch {
            val flDrawingView: FrameLayout = findViewById(R.id.fl_canvas_wrapper)
            saveMediaToStorage(getBitmapFromView(flDrawingView), isShare)
        }
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
        val btnSave:ImageButton = findViewById(R.id.ib_save)
        val btnShare:ImageButton = findViewById(R.id.ib_share)

        canvasView = findViewById(R.id.cv_main_canvas)
        canvasView?.setBrushSize(20.0f)

        if (!isExternalStorageAvailable()) {
            btnBrowseForImage.isEnabled = false
        }

        btnBrowseForImage.setOnClickListener {
            if (isReadStorageAllowed()) getStoragePermission()
            browseForImageAndSetBackground()
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

        btnSave.setOnClickListener {
            if (!isWriteStorageAllowed()) getStoragePermission()
            saveDrawingToMedia(false)
        }

        btnShare.setOnClickListener {
            if (!isWriteStorageAllowed()) getStoragePermission()
            saveDrawingToMedia(true)
        }
    }

    private fun getStoragePermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(
                Manifest.permission.READ_EXTERNAL_STORAGE)
                && shouldShowRequestPermissionRationale(
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            showRationaleDialog(getString(R.string.perm_ext_storage_rat_title),
            getString(R.string.perm_ext_storage_rat_msg))

            permissionResultLauncher.launch(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE))
        }
    }

    private fun isReadStorageAllowed(): Boolean {
        val result = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun isWriteStorageAllowed(): Boolean {
        val result = ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
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

    private fun getBitmapFromView(v: View): Bitmap {
        val result: Bitmap = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val bgDrawable = v.background
        if(bgDrawable != null){
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        v.draw(canvas)
        return result
    }

    private suspend fun saveMediaToStorage(mBitmap: Bitmap?, isShare: Boolean): String {
        var result = ""

        withContext(Dispatchers.IO) {
            if (mBitmap != null) {
                try {
                    val filename = "DrawApp_Image_${System.currentTimeMillis()}.png"
                    var fos: OutputStream? = null

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !isShare) {
                        this@MainActivity.contentResolver?.also { resolver ->
                            val contentValues = ContentValues().apply {
                                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                                put(MediaStore.MediaColumns.RELATIVE_PATH,
                                    Environment.DIRECTORY_PICTURES)
                            }
                            val imageUri: Uri? =
                                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    contentValues)
                            fos = imageUri?.let { resolver.openOutputStream(it) }
                        }
                        result = filename
                    } else {
                        val imagesDir =
                            Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES)
                        val image = File(imagesDir, filename)
                        fos = FileOutputStream(image)
                        result = image.absolutePath
                    }

                    fos.use {
                        mBitmap.compress(Bitmap.CompressFormat.PNG, 90, it)
                    }

                    runOnUiThread {
                        if (result.isNotEmpty()) {
                            if (isShare){
                                shareImage(result)
                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    getString(R.string.res_save_file, result),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Toast.makeText(this@MainActivity, R.string.res_save_failed,
                                Toast.LENGTH_LONG).show()
                        }
                        progressDialog?.dismiss()
                    }
                } catch (e: Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    private fun showProgressDialog() {
        progressDialog = Dialog(this)
        progressDialog?.setContentView(R.layout.customs_progress_dialog)
        progressDialog?.show()
    }

    private fun shareImage(uriString: String?) {
        if(uriString != null && uriString.isNotEmpty()) {
            MediaScannerConnection.scanFile(this, arrayOf(uriString), null) {
                    _, uri ->
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                shareIntent.type = "image/png"
                startActivity(Intent.createChooser(shareIntent, getString(R.string.title_share_intent)))
            }
        } else {
            Toast.makeText(this, getString(R.string.warn_no_share_path), Toast.LENGTH_LONG)
                .show()
        }
    }
}