package com.example.if3210_2024_android_ppl.ui.scan

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.fragment.app.Fragment
import com.example.if3210_2024_android_ppl.R
import com.example.if3210_2024_android_ppl.databinding.FragmentScanBinding
import com.google.common.util.concurrent.ListenableFuture
import java.io.IOException
import kotlin.math.log

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null

    private val binding get() = _binding!!
    private lateinit var cameraController: LifecycleCameraController
    private lateinit var imageCapture: ImageCapture
    private var pickedPhoto: Uri?=null
    private var isFrame=false

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
                // Do something with the selected image URI
                // For example, display it in an ImageView
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

        binding.frame.visibility = View.GONE

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
        binding.frameBut.setOnClickListener {
            //create a toast
            Toast.makeText(context, "frame Clicked", Toast.LENGTH_SHORT).show()
            if(isFrame){
                binding.frame.visibility = View.GONE
                isFrame=false
            }else{
                binding.frame.visibility = View.VISIBLE
                isFrame=true
            }

        }
        binding.pictBut.setOnClickListener {
            // Create a toast
            Toast.makeText(context, "Picture Clicked", Toast.LENGTH_SHORT).show()

                // Permission granted, open gallery
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
                val rotated = rotateBitmapfile(pickedBitmap)

                // Show image preview dialog with the picked bitmap
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
        if (!isFrame) {
            Log.d(tag, "Showing image preview dialog...")
            val dialogView =
                LayoutInflater.from(requireContext()).inflate(R.layout.dialog_image_preview, null)
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
        }else{
            Log.d(tag, "Showing image preview dialog...")
            val dialogView =
                LayoutInflater.from(requireContext()).inflate(R.layout.dialog_image_preview, null)
            val imagePreview = dialogView.findViewById<ImageView>(R.id.imagePreview)

            // Rotate the bitmap based on its orientation
            val rotatedBitmap = rotateBitmap(bitmapImage)

//          put an overlay on the rotated bitmap
            Log.d(tag, "otw to overlaying bitmap")
//            val overlay = BitmapFactory.decodeResource(resources, R.drawable.frame)
            Log.d(tag, "otw2 to overlaying bitmap")
            val res= overlayBitmap(rotatedBitmap)

            imagePreview.setImageBitmap(res)
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
    private fun overlayBitmap(bitmap: Bitmap): Bitmap {

        val overlay = BitmapFactory.decodeResource(resources, R.drawable.minum_java)
        val combinedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val canvas = Canvas(combinedBitmap)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        canvas.drawBitmap(overlay, 0f, 0f, null)
        return combinedBitmap
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}