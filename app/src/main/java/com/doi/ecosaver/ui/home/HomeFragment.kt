package com.doi.ecosaver.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.doi.ecosaver.R
import com.doi.ecosaver.data.DataRepository
import com.doi.ecosaver.data.RemoteDataSource
import com.doi.ecosaver.databinding.FragmentHomeBinding
import com.doi.ecosaver.ui.home.electric.ElectricActivity
import com.doi.ecosaver.ui.home.fuel.FuelActivity
import com.doi.ecosaver.ui.home.shopping.ShoppingActivity
import com.doi.ecosaver.ui.home.water.WaterActivity
import com.doi.ecosaver.utils.ViewModelFactory
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeFragmentViewModel
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var currentBensin: Float? = null
    private var lastBensin: Float? = null
    private var currentKwh: Float? = null
    private var lastKwh: Float? = null
    private var currentAir: Float? = null
    private var lastAir: Float? = null
    private var currentBelanja: Float? = null
    private var lastBelanja: Float? = null
    private var totalBensin: Float? = null
    private var totalLastBensin: Float? = null
    private var totalKwh: Float? = null
    private var totalLastKwh: Float? = null
    private var totalAir: Float? = null
    private var totalLastAir: Float? = null

    private var totalBelanja: Float? = null
    private var totalLastBelanja: Float? = null

    private var allDataFetched = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = "GEMINIAPIKEY"
        )

        val prompt = "Berikan Tips Hari Ini Untuk Menghemat Energi tentang listrik, bensin, belanja, dan air untuk tanda baca pakai emoticon saja"

        lifecycleScope.launch {
            try {
                val response = generativeModel.generateContent(prompt)
                binding.tvTips.text = response.text ?: "Response text is null"
                Log.d("HomeFragment", response.text ?: "Response text is null")
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error generating content", e)
            }
        }

        return view
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val dataRepository = DataRepository(RemoteDataSource())
        val viewModelFactory = ViewModelFactory(dataRepository)
        viewModel = ViewModelProvider(this, viewModelFactory)[HomeFragmentViewModel::class.java]

        val idUser = auth.currentUser?.uid ?: ""
        val currentMonthYear = getCurrentMonthYear()
        val lastMonthYear = getLastMonthYear()
        var dataFetchedCount = 0

        fun checkAllDataFetchedAndUpdateSaving() {
            if (++dataFetchedCount >= 4) {
//                val fuelSaved = (lastBensin ?: 0f) - (currentBensin ?: 0f)
//                val electricSaved = (lastKwh ?: 0f) - (currentKwh ?: 0f)
//                val waterSaved = (lastAir ?: 0f) - (currentAir ?: 0f)
//                val shoppingSaved = (lastBelanja ?: 0f) - (currentBelanja ?: 0f)
                val fuelSaved = (totalLastBensin?: 0f) - (totalBensin ?: 0f)
                val electricSaved = (totalLastKwh?: 0f) - (totalKwh ?: 0f)
                val waterSaved = (totalLastAir?: 0f) - (totalAir ?: 0f)
                val shoppingSaved = (totalLastBelanja?: 0f) - (totalBelanja ?: 0f)

                val totalSaved = fuelSaved + electricSaved + waterSaved + shoppingSaved

                val savingText = if (totalSaved > 0f) {
                    "Total Penghematan Bulan Ini: +${String.format("%.1f", totalSaved)}"
                } else {
                    "Pengeluaran Naik Bulan Ini: ${String.format("%.1f", totalSaved)}"
                }

                binding.tvSaving.text = savingText
                binding.tvSaving.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (totalSaved >= 0) R.color.green else R.color.red
                    )
                )
            }
        }

        binding.apply {
            viewModel.apply {
                // --- BENSIN ---
                getBensinDetail(idUser, currentMonthYear).observe(viewLifecycleOwner) { bensin ->
                    currentBensin = bensin?.liter?.toFloat() ?: 0f
                    totalBensin = bensin?.totalHarga?.toFloat() ?: 0f
                    tvFuel.text = "${currentBensin?.toInt() ?: 0}L"
                    updatePercentage(currentBensin, lastBensin, binding.tvPercentageFuel)
                }

                getBensinDetail(idUser, lastMonthYear).observe(viewLifecycleOwner) { bensin ->
                    lastBensin = bensin?.liter?.toFloat() ?: 0f
                    totalLastBensin = bensin?.totalHarga?.toFloat() ?: 0f
                    tvBeforeFuel.text = "vs ${lastBensin?.toInt() ?: 0}L"
                    updatePercentage(currentBensin, lastBensin, binding.tvPercentageFuel)
                    checkAllDataFetchedAndUpdateSaving()
                }

                // --- LISTRIK ---
                getListrikDetail(idUser, currentMonthYear).observe(viewLifecycleOwner) { listrik ->
                    currentKwh = listrik?.kwh?.toFloat() ?: 0f
                    totalKwh = listrik?.totalHarga?.toFloat() ?: 0f
                    tvKwh.text = "${currentKwh?.toInt() ?: 0}kWh"
                    updatePercentage(currentKwh, lastKwh, binding.tvPercentageKwh)
                }

                getListrikDetail(idUser, lastMonthYear).observe(viewLifecycleOwner) { listrik ->
                    lastKwh = listrik?.kwh?.toFloat() ?: 0f
                    totalLastKwh = listrik?.totalHarga?.toFloat() ?: 0f
                    tvBeforeKwh.text = "vs ${lastKwh?.toInt() ?: 0}kWh"
                    updatePercentage(currentKwh, lastKwh, binding.tvPercentageKwh)
                    checkAllDataFetchedAndUpdateSaving()
                }

                // --- AIR ---
                getAirDetail(idUser, currentMonthYear).observe(viewLifecycleOwner) { air ->
                    currentAir = air?.liter?.toFloat() ?: 0f
                    totalAir = air?.totalHarga?.toFloat() ?: 0f
                    tvWater.text = "${currentAir?.toInt() ?: 0}L"
                    updatePercentage(currentAir, lastAir, binding.tvPercentageWater)
                }

                getAirDetail(idUser, lastMonthYear).observe(viewLifecycleOwner) { air ->
                    lastAir = air?.liter?.toFloat() ?: 0f
                    totalLastAir = air?.totalHarga?.toFloat() ?: 0f
                    tvBeforeWater.text = "vs ${lastAir?.toInt() ?: 0}L"
                    updatePercentage(currentAir, lastAir, binding.tvPercentageWater)
                    checkAllDataFetchedAndUpdateSaving()
                }

                // --- BELANJA ---
                getBelanjaDetail(idUser, currentMonthYear).observe(viewLifecycleOwner) { belanja ->
                    currentBelanja = belanja?.total?.toFloat() ?: 0f
                    totalBelanja = belanja?.total?.toFloat() ?: 0f
                    tvShopping.text = "Rp${currentBelanja?.toInt() ?: 0}"
                    updatePercentage(currentBelanja, lastBelanja, binding.tvPercentageShopping)
                }

                getBelanjaDetail(idUser, lastMonthYear).observe(viewLifecycleOwner) { belanja ->
                    lastBelanja = belanja?.total?.toFloat() ?: 0f
                    totalLastBelanja = belanja?.total?.toFloat() ?: 0f
                    tvBeforeShopping.text = "vs Rp${lastBelanja?.toInt() ?: 0}"
                    updatePercentage(currentBelanja, lastBelanja, binding.tvPercentageShopping)
                    checkAllDataFetchedAndUpdateSaving()
                }
            }

            // Navigasi ke halaman detail
            clFuel.setOnClickListener {
                startActivity(Intent(requireContext(), FuelActivity::class.java))
            }
            clWater.setOnClickListener {
                startActivity(Intent(requireContext(), WaterActivity::class.java))
            }
            clShopping.setOnClickListener {
                startActivity(Intent(requireContext(), ShoppingActivity::class.java))
            }
            clElectric.setOnClickListener {
                startActivity(Intent(requireContext(), ElectricActivity::class.java))
            }
        }
    }


//    @Deprecated("Deprecated in Java")
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        val dataRepository = DataRepository(RemoteDataSource())
//        val viewModelFactory = ViewModelFactory(dataRepository)
//        viewModel = ViewModelProvider(this, viewModelFactory)[HomeFragmentViewModel::class.java]
//
//        val idUser = auth.currentUser?.uid ?: ""
//        val currentMonthYear = getCurrentMonthYear()
//        val lastMonthYear = getLastMonthYear()
//        var allDataFetched = 0
//
//        Log.d("HomeFragment", "idUser: $idUser")
//
//        binding.apply {
//            viewModel.apply {
//
//                // Bensin
//                var currentBensin: Float? = null
//                var lastBensin: Float? = null
//                var totalBensin: Float? = null
//                var totalLastBensin: Float? = null
//
//                getBensinDetail(idUser, currentMonthYear).observe(viewLifecycleOwner) { bensin ->
//                    currentBensin = bensin?.liter?.toFloat() ?: 0f
//                    totalBensin = bensin?.totalHarga?.toFloat() ?: 0f
//                    tvFuel.text = "${currentBensin?.toInt() ?: 0}L"
//
//                    updatePercentage(currentBensin, lastBensin, binding.tvPercentageFuel)
//
//                }
//
//                getBensinDetail(idUser, lastMonthYear).observe(viewLifecycleOwner) { bensin ->
//                    lastBensin = bensin?.liter?.toFloat() ?: 0f
//                    totalLastBensin = bensin?.totalHarga?.toFloat() ?: 0f
//                    tvBeforeFuel.text = "vs ${lastBensin?.toInt() ?: 0}L"
//                    updatePercentage(currentBensin, lastBensin, binding.tvPercentageFuel)
//                }
//
//                // Listrik
//                var currentKwh: Float? = null
//                var lastKwh: Float? = null
//                var totalKwh: Float? = null
//                var totalLastKwh: Float? = null
//
//                getListrikDetail(idUser, currentMonthYear).observe(viewLifecycleOwner) { listrik ->
//                    currentKwh = listrik?.kwh?.toFloat() ?: 0f
//                    totalKwh = listrik?.totalHarga?.toFloat() ?: 0f
//                    tvKwh.text = "${currentKwh?.toInt() ?: 0}kWh"
//                    updatePercentage(currentKwh, lastKwh, binding.tvPercentageKwh)
//                }
//
//                getListrikDetail(idUser, lastMonthYear).observe(viewLifecycleOwner) { listrik ->
//                    lastKwh = listrik?.kwh?.toFloat() ?: 0f
//                    totalLastKwh = listrik?.totalHarga?.toFloat() ?: 0f
//                    tvBeforeKwh.text = "vs ${lastKwh?.toInt() ?: 0}kWh"
//                    updatePercentage(currentKwh, lastKwh, binding.tvPercentageKwh)
//                }
//
//                // Air
//                var currentAir: Float? = null
//                var lastAir: Float? = null
//                var totalAir: Float? = null
//                var totalLastAir: Float? = null
//
//                getAirDetail(idUser, currentMonthYear).observe(viewLifecycleOwner) { air ->
//                    currentAir = air?.liter?.toFloat() ?: 0f
//                    totalAir = air?.totalHarga?.toFloat() ?: 0f
//                    tvWater.text = "${currentAir?.toInt() ?: 0}L"
//                    updatePercentage(currentAir, lastAir, binding.tvPercentageWater)
//                }
//
//                getAirDetail(idUser, lastMonthYear).observe(viewLifecycleOwner) { air ->
//                    lastAir = air?.liter?.toFloat() ?: 0f
//                    totalLastAir = air?.totalHarga?.toFloat() ?: 0f
//                    tvBeforeWater.text = "vs ${lastAir?.toInt() ?: 0}L"
//                    updatePercentage(currentAir, lastAir, binding.tvPercentageWater)
//                }
//
//                // Belanja
//                var currentBelanja: Float? = null
//                var lastBelanja: Float? = null
//                var totalBelanja: Float? = null
//                var totalLastBelanja: Float? = null
//
//                getBelanjaDetail(idUser, currentMonthYear).observe(viewLifecycleOwner) { belanja ->
//                    currentBelanja = belanja?.total?.toFloat() ?: 0f
//                    totalBelanja = belanja?.total?.toFloat() ?: 0f
//                    tvShopping.text = "Rp${currentBelanja?.toInt() ?: 0}"
//                    updatePercentage(currentBelanja, lastBelanja, binding.tvPercentageShopping)
//                }
//
//                getBelanjaDetail(idUser, lastMonthYear).observe(viewLifecycleOwner) { belanja ->
//                    lastBelanja = belanja?.total?.toFloat() ?: 0f
//                    totalLastBelanja = belanja?.total?.toFloat() ?: 0f
//
//                    tvBeforeShopping.text = "vs Rp${lastBelanja?.toInt() ?: 0}"
//                    updatePercentage(currentBelanja, lastBelanja, binding.tvPercentageShopping)
//                }
//
//                fun checkAllDataFetchedAndUpdateSaving() {
//                    if (++allDataFetched >= 4) {
//                        val fuelSaved = (lastBensin ?: 0f) - (currentBensin ?: 0f)
//                        val electricSaved = (lastKwh ?: 0f) - (currentKwh ?: 0f)
//                        val waterSaved = (lastAir ?: 0f) - (currentAir ?: 0f)
//                        val shoppingSaved = (lastBelanja ?: 0f) - (currentBelanja ?: 0f)
//
//                        // Untuk contoh, kita jumlahkan semua penghematan dalam satuan masing-masing
//                        val totalSaved = fuelSaved + electricSaved + waterSaved + shoppingSaved
//
//                        // Format teks berdasarkan nilai
//                        val savingText = if (totalSaved > 0f) {
//                            "Total Penghematan Bulan Ini: +${String.format("%.1f", totalSaved)}"
//                        } else {
//                            "Pengeluaran Naik Bulan Ini: ${String.format("%.1f", totalSaved)}"
//                        }
//
//                        binding.tvSaving.text = savingText
//                        binding.tvSaving.setTextColor(
//                            ContextCompat.getColor(
//                                requireContext(),
//                                if (totalSaved >= 0) R.color.green else R.color.red
//                            )
//                        )
//                    }
//                }
//
//            }
//            clFuel.setOnClickListener {
//                val intent = Intent(requireContext(), FuelActivity::class.java)
//                startActivity(intent)
//            }
//            clWater.setOnClickListener {
//                val intent = Intent(requireContext(), WaterActivity::class.java)
//                startActivity(intent)
//            }
//            clShopping.setOnClickListener {
//                val intent = Intent(requireContext(), ShoppingActivity::class.java)
//                startActivity(intent)
//            }
//            clElectric.setOnClickListener {
//                val intent = Intent(requireContext(), ElectricActivity::class.java)
//                startActivity(intent)
//            }
//
//
//        }
//    }

    private fun getLastMonthYear(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        return "$year-${month.toString().padStart(2, '0')}"
    }
    private fun calculatePercentage(current: Float?, previous: Float?): Float {
        if (current == null || previous == null || previous == 0f) return 0f
        return ((current - previous) / previous) * 100f
    }

    private fun updatePercentage(current: Float?, previous: Float?, targetView: TextView) {
        val percentage = calculatePercentage(current, previous)
        val formatted = String.format("%.1f%%", percentage)

        targetView.text = formatted
        targetView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (percentage < 0) R.color.green else R.color.red
            )
        )
    }


    private fun getCurrentMonthYear(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        return "$year-${month.toString().padStart(2, '0')}"
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
