package com.doi.ecosaver.ui.scan

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.doi.ecosaver.R
import com.doi.ecosaver.data.DataRepository
import com.doi.ecosaver.data.RemoteDataSource
import com.doi.ecosaver.databinding.FragmentHomeBinding
import com.doi.ecosaver.databinding.FragmentScanBinding
import com.doi.ecosaver.utils.ViewModelFactory
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScanFragment : Fragment() {
    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ScanFragmentViewModel

    private val CAMERA_REQ_CODE = 100

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentScanBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val dataRepository = DataRepository(RemoteDataSource())
        val viewModelFactory = ViewModelFactory(dataRepository)
        viewModel = ViewModelProvider(this, viewModelFactory)[ScanFragmentViewModel::class.java]
        binding.apply {
            btnOpenCamera.setOnClickListener {
                openCamera()
            }
            viewModel.apply {
                capturedImage.observe(viewLifecycleOwner) {bitmap ->
                    imagePreview.setImageBitmap(bitmap)
                    sendImageToGemini(bitmap)

                    img.visibility = View.GONE
                    tv.visibility = View.GONE
                    progressBar.visibility = View.VISIBLE
                    viewLifecycleOwner.lifecycleScope.launch {
                        delay(1000L)
                        progressBar.visibility = View.GONE
                        tv.visibility = View.VISIBLE
                        img.visibility = View.VISIBLE
                        tv.text = "Scan Nota Berhasil"
                    }
                }
            }
        }
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                100
            )
        } else {
            launchCamera()
        }
    }

    private fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQ_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAMERA_REQ_CODE && resultCode == Activity.RESULT_OK) {
            val bitmap = data?.extras?.get("data") as? Bitmap
            bitmap?.let { viewModel.setCapturedImage(it) }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}