package com.example.if3210_2024_android_ppl.ui.scan

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.if3210_2024_android_ppl.databinding.FragmentScanBinding
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.if3210_2024_android_ppl.R
import com.example.if3210_2024_android_ppl.api.BillResponse
import com.example.if3210_2024_android_ppl.api.KeystoreHelper
import com.example.if3210_2024_android_ppl.api.RetrofitInstance
import com.example.if3210_2024_android_ppl.ui.bill.BillFragment
import com.google.common.util.concurrent.ListenableFuture
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!
    private lateinit var cameraController: LifecycleCameraController
    private lateinit var imageCapture: ImageCapture
    private var pickedPhoto: Uri?=null

    companion object {
        private val CAMERAX_PERMISSION = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        private const val TAG = "ScanFragment"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 1
        private const val GALLERY_REQUEST_CODE = 2

    }

    private val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> by lazy {
        ProcessCameraProvider.getInstance(requireContext())
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri: Uri? = result.data?.data
                loadSelectedImage(selectedImageUri)
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        val root: View = binding.root
        imageCapture = ImageCapture.Builder().build()

        if (!hasRequiredPermission()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                CAMERAX_PERMISSION,
                0
            )
        }

        binding.shutterBut.setOnClickListener {
            Toast.makeText(context, "Button Clicked", Toast.LENGTH_SHORT).show()
            if (imageCapture != null) {
                imageCapture?.takePicture(
                    ContextCompat.getMainExecutor(requireContext()),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(imageProxy: ImageProxy) {
                            val buffer = imageProxy.planes[0].buffer
                            val bytes = ByteArray(buffer.capacity())
                            buffer.get(bytes)
                            val bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            showImagePreviewDialog(bitmapImage)
                            imageProxy.close()
                        }
                        override fun onError(exception: ImageCaptureException) {
                            Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                        }
                    }
                )
            }
        }
        binding.flashBut.setOnClickListener {
            Toast.makeText(context, "Flash Clicked", Toast.LENGTH_SHORT).show()
        }
        binding.pictBut.setOnClickListener {
            Toast.makeText(context, "Picture Clicked", Toast.LENGTH_SHORT).show()
            openGallery()
        }
        initCamera()

        return root
    }

    private fun openGallery() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        galleryLauncher.launch(photoPickerIntent)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val pickedPhoto = data.data
            loadSelectedImage(pickedPhoto)
        }
    }

    private fun loadSelectedImage(uri: Uri?) {
        uri?.let { pickedPhoto ->
            try {
                val source = ImageDecoder.createSource(requireContext().contentResolver, pickedPhoto)
                val pickedBitmap = ImageDecoder.decodeBitmap(source)
                val rotated = rotateBitmapfile(pickedBitmap)

                rotated?.let { bitmap ->
                    showImagePreviewDialog(bitmap)
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error loading selected image: ${e.message}", e)
            }
        }
    }

    private fun initCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(binding.prev.surfaceProvider) }

            imageCapture = ImageCapture.Builder()
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun hasRequiredPermission(): Boolean {
        return CAMERAX_PERMISSION.all {
            ContextCompat.checkSelfPermission(
                requireContext(),
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun showImagePreviewDialog(bitmapImage: Bitmap) {
        Log.d(tag, "Showing image preview dialog...")
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_image_preview, null)
        val imagePreview = dialogView.findViewById<ImageView>(R.id.imagePreview)
        val rotatedBitmap = rotateBitmap(bitmapImage)
        imagePreview.setImageBitmap(rotatedBitmap)
        imagePreview.scaleType = ImageView.ScaleType.FIT_CENTER

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Upload") { dialog, _ ->
                Log.d(tag, "Upload button clicked.")

                val imageFile = bitmapToFile(rotatedBitmap, requireContext())

                val keystoreHelper = KeystoreHelper(requireContext())
                val userToken = keystoreHelper.getToken()?:"invalidToken"

                uploadBill(imageFile, userToken)

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                Log.d(tag, "Cancel button clicked.")
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
        Log.d(tag, "Image preview dialog shown.")
    }

    private fun rotateBitmap(bitmap: Bitmap): Bitmap {
        val rotationMatrix = Matrix()
        rotationMatrix.postRotate(90f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, rotationMatrix, true)
    }

    private fun rotateBitmapfile(bitmap: Bitmap): Bitmap {
        val rotationMatrix = Matrix()
        rotationMatrix.postRotate(270f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, rotationMatrix, true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun uploadBill(file: File, userToken: String) {
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val call = RetrofitInstance.api.uploadBill(body, "Bearer $userToken")
        call.enqueue(object : retrofit2.Callback<BillResponse> {
            override fun onResponse(call: retrofit2.Call<BillResponse>, response: retrofit2.Response<BillResponse>) {
                if (response.isSuccessful) {
                    val billResponse = response.body()
                    val itemsList = ArrayList(billResponse?.items?.items ?: listOf())
                    val itemsFragment = BillFragment.newInstance(itemsList)
                    Log.d("ScanFrag", itemsList.toString())
                    activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.nav_host_fragment_activity_main, itemsFragment)?.addToBackStack(null)?.commit()
                } else {
                    Log.d("ScanFrag","Request error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: retrofit2.Call<BillResponse>, t: Throwable) {
                // TODO: Handle failure
                Log.d("ScanFrag","Failed to make request: ${t.message}")
            }
        })
    }

    private fun bitmapToFile(bitmap: Bitmap, context: Context): File {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width / 2, bitmap.height / 2, true)
        val file = File(context.cacheDir, "tempImage" + System.currentTimeMillis() + ".jpg")
        file.createNewFile()

        val bos = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos)
        val bitmapData = bos.toByteArray()

        FileOutputStream(file).apply {
            write(bitmapData)
            flush()
            close()
        }

        return file
    }
}