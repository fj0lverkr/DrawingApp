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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.iterator
import androidx.lifecycle.lifecycleScope
import com.nilsnahooy.drawingapp.databinding.ActivityMainBinding
import com.nilsnahooy.drawingapp.databinding.CustomsProgressDialogBinding
import com.nilsnahooy.drawingapp.databinding.DialogBrushColorBinding
import com.nilsnahooy.drawingapp.databinding.DialogBrushSizeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private var b: ActivityMainBinding? = null
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
            saveMediaToStorage(getBitmapFromView(b?.flCanvasWrapper!!), isShare)
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

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b?.root)

        b?.cvMainCanvas?.setBrushSize(20.0f)

        if (!isExternalStorageAvailable()) {
            b?.ibOpenGallery?.isEnabled = false
        }

        b?.ibOpenGallery?.setOnClickListener {
            if (isReadStorageAllowed()) getStoragePermission()
            browseForImageAndSetBackground()
        }

        b?.ibOpenBrushSizeDialog?.setOnClickListener {
            showBrushSizeDialog()
        }

        b?.ibOpenBrushColorDialog?.setOnClickListener {
            showBrushColorDialog()
        }

        b?.ibUndo?.setOnClickListener {
            b?.cvMainCanvas?.undo()
        }

        b?.ibRedo?.setOnClickListener {
            b?.cvMainCanvas?.redo()
        }

        b?.ibSave?.setOnClickListener {
            if (!isWriteStorageAllowed()) getStoragePermission()
            saveDrawingToMedia(false)
        }

        b?.ibShare?.setOnClickListener {
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
        val dB = DialogBrushColorBinding.inflate(layoutInflater)
        brushColorDialog.setContentView(dB.root)
        brushColorDialog.setTitle(R.string.brush_color_dialog_title)

        for(v in dB.root) {
            val ib = v as ImageButton
            ib.setOnClickListener {
                b?.cvMainCanvas?.setBrushColor(it.tag.toString())
                brushColorDialog.dismiss()
            }
        }
        brushColorDialog.show()
    }

    private fun showBrushSizeDialog() {
        val brushSizeDialog = Dialog(this)
        val dB = DialogBrushSizeBinding.inflate(layoutInflater)
        brushSizeDialog.setContentView(dB.root)
        brushSizeDialog.setTitle(R.string.brush_size_dialog_title)

        dB.ibBrushSizeExtraSmall.setOnClickListener {
            b?.cvMainCanvas?.setBrushSize(5f)
            brushSizeDialog.dismiss()
        }

        dB.ibBrushSizeSmall.setOnClickListener {
           b?.cvMainCanvas?.setBrushSize(10f)
           brushSizeDialog.dismiss()
       }

        dB.ibBrushSizeMedium.setOnClickListener {
            b?.cvMainCanvas?.setBrushSize(20f)
            brushSizeDialog.dismiss()
        }

        dB.ibBrushSizeLarge.setOnClickListener {
            b?.cvMainCanvas?.setBrushSize(30f)
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
        val dB = CustomsProgressDialogBinding.inflate(layoutInflater)
        progressDialog?.setContentView(dB.root)
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