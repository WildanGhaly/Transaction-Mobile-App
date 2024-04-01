package com.example.if3210_2024_android_ppl.ui.scan

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.if3210_2024_android_ppl.databinding.FragmentScanBinding
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Camera
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
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
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
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

//        create onclick listener for button
        binding.shutterBut.setOnClickListener {
            Toast.makeText(context, "Button Clicked", Toast.LENGTH_SHORT).show()
            if (imageCapture != null) {
                // Capture image
                imageCapture?.takePicture(
                    ContextCompat.getMainExecutor(requireContext()),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(imageProxy: ImageProxy) {
                            // Convert captured image to bitmap
                            val buffer = imageProxy.planes[0].buffer
                            val bytes = ByteArray(buffer.capacity())
                            buffer.get(bytes)
                            val bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            // Show image preview dialog
                            showImagePreviewDialog(bitmapImage)
                            // Close the imageProxy to release resources
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
            //create a toast
            Toast.makeText(context, "Flash Clicked", Toast.LENGTH_SHORT).show()

        }
        binding.pictBut.setOnClickListener {
            // Create a toast
            Toast.makeText(context, "Picture Clicked", Toast.LENGTH_SHORT).show()

                // Permission granted, open gallery
                selectImageFromGallery()

            }
        initCamera()

        return root
    }

    private fun selectImageFromGallery() {
        // Check if permission to read external storage is granted
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission if not granted
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission granted, open gallery
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            // Get the URI of the selected image
            val pickedPhoto = data.data
            // Load the selected image
            loadSelectedImage(pickedPhoto)
        }
    }

    private fun loadSelectedImage(uri: Uri?) {
        uri?.let { pickedPhoto ->
            try {
                val source = ImageDecoder.createSource(requireContext().contentResolver, pickedPhoto)
                val pickedBitmap = ImageDecoder.decodeBitmap(source)

                // Show image preview dialog with the picked bitmap
                pickedBitmap?.let { bitmap ->
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

            // Set up the preview use case
            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(binding.prev.surfaceProvider) }

            // Set up the image capture use case
            imageCapture = ImageCapture.Builder()
                .build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind the use cases to the lifecycle of the fragment
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

        // Rotate the bitmap based on its orientation
        val rotatedBitmap = rotateBitmap(bitmapImage)

        imagePreview.setImageBitmap(rotatedBitmap)
//        imagePreview.setImageBitmap(bitmapImage)

        // Adjusting the image view's scale type to fit the entire image within the view
        imagePreview.scaleType = ImageView.ScaleType.FIT_CENTER

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Upload") { dialog, _ ->
                Log.d(tag, "Upload button clicked.")
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




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}