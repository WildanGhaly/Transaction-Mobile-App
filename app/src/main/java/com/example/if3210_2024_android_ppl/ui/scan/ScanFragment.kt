package com.example.if3210_2024_android_ppl.ui.scan

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
import android.content.pm.PackageManager
import android.graphics.Camera
import android.widget.Button
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var cameraController: LifecycleCameraController


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

        if (!hasRequiredPermission()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                CAMERAX_PERMISSION,
                0
            )
        }

//        create onclick listener for button
        binding.shutterBut.setOnClickListener {

        }
        binding.flashBut.setOnClickListener {
            //create a toast
            Toast.makeText(context, "Flash Clicked", Toast.LENGTH_SHORT).show()

        }
        binding.pictBut.setOnClickListener {
            //create a toast
            Toast.makeText(context, "Picture Clicked", Toast.LENGTH_SHORT).show()

            }
        initCamera()

        return root
    }

    private fun initCamera() {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            cameraController = LifecycleCameraController(requireContext())
            cameraController.bindToLifecycle(viewLifecycleOwner)

            val preview = binding.prev // Access the existing PreviewView
            preview.controller = cameraController

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

    companion object {
        private val CAMERAX_PERMISSION = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}