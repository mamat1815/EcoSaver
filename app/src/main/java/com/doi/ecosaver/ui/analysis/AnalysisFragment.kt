package com.doi.ecosaver.ui.analysis

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.doi.ecosaver.R
import com.doi.ecosaver.data.DataRepository
import com.doi.ecosaver.data.RemoteDataSource
import com.doi.ecosaver.databinding.FragmentAnalysisBinding
import com.doi.ecosaver.databinding.FragmentHomeBinding
import com.doi.ecosaver.ui.home.HomeFragmentViewModel
import com.doi.ecosaver.utils.ViewModelFactory
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.ai.client.generativeai.GenerativeModel
import com.google.android.play.core.integrity.v
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 * Use the [AnalysisFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AnalysisFragment : Fragment() {
    private var _binding: FragmentAnalysisBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AnalysisViewModel
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAnalysisBinding.inflate(inflater, container, false)
        val view = binding.root



        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dataRepository = DataRepository(RemoteDataSource())
        val viewModelFactory = ViewModelFactory(dataRepository)
        viewModel = ViewModelProvider(this, viewModelFactory)[AnalysisViewModel::class.java]
        // Load data dari ViewModel
        viewModel.loadAllData()

        // Observe LiveData dari ViewModel
        viewModel.analisisDataList.observe(viewLifecycleOwner, Observer { dataList ->
            if (dataList.isNullOrEmpty()) {
                // Tampilkan empty state kalau mau
                binding.chart.clear()
                return@Observer
            }

            // Buat entries untuk tiap kategori
            val bensinEntries = dataList.mapIndexed { index, item -> Entry(index.toFloat(), item.bensin) }
            val listrikEntries = dataList.mapIndexed { index, item -> Entry(index.toFloat(), item.listrik) }
            val airEntries = dataList.mapIndexed { index, item -> Entry(index.toFloat(), item.air) }
            val belanjaEntries = dataList.mapIndexed { index, item -> Entry(index.toFloat(), item.belanja) }

            // Dataset untuk tiap kategori
            val bensinDataSet = LineDataSet(bensinEntries, "Bensin").apply {
                color = Color.RED
                circleRadius = 4f
                lineWidth = 2f
            }

            val listrikDataSet = LineDataSet(listrikEntries, "Listrik").apply {
                color = Color.BLUE
                circleRadius = 4f
                lineWidth = 2f
            }

            val airDataSet = LineDataSet(airEntries, "Air").apply {
                color = Color.CYAN
                circleRadius = 4f
                lineWidth = 2f
            }

            val belanjaDataSet = LineDataSet(belanjaEntries, "Belanja").apply {
                color = Color.GREEN
                circleRadius = 4f
                lineWidth = 2f
            }

            val lineData = LineData(bensinDataSet, listrikDataSet, airDataSet, belanjaDataSet)
            binding.chart.data = lineData

            // Setup XAxis untuk tampilkan nama bulan (monthYear)
            val months = dataList.map { it.monthYear }
            val xAxis = binding.chart.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter(months)
            xAxis.granularity = 1f
            xAxis.position = XAxis.XAxisPosition.BOTTOM

            binding.chart.invalidate()

            val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = "GEMINIAPIKEY"
            )

//            val prompt = "Berikan Analisisi dari data ini $dataList secara detail tidak perlu pakai tanda baca tapi mudah dibaca"
//            val prompt = "buatkan tips dan analisis dari data $dataList hasil data ini bensin dan air itu liter, listrik itu kwh, belanja itu harga tolong analisis perubahannya dan hitung penggunaan karbonnya"
            val prompt = """
Analisis data konsumsi energi ini dan berikan:
1. **Analisis Tren** - Identifikasi pola penggunaan bahan bakar (liter), listrik (kWh), air (liter), dan belanja (harga) dari waktu ke waktu
2. **Perkiraan Jejak Karbon** - Hitung perkiraan emisi CO2 berdasarkan:
   - Bahan bakar: 2.3 kg CO2 per liter
   - Listrik: 0.85 kg CO2 per kWh (sesuaikan dengan wilayah jika perlu)
   - Air: 0.3 kg CO2 per liter (pengolahan dan distribusi)
   - Belanja: Sertakan jika ada faktor emisi spesifik
3. **Tips Penghematan** - Berikan 3-5 rekomendasi praktis untuk mengurangi konsumsi di setiap kategori
4. **Deteksi Anomali** - Soroti lonjakan atau penurunan yang tidak biasa
5. **Analisis Komparatif** - Tunjukkan persentase perubahan antar periode

Format respons dengan bagian yang jelas dan poin-poin untuk kemudahan pembacaan.
Fokus pada wawasan praktis daripada hanya mendeskripsikan data.

Data: $dataList
"""

            lifecycleScope.launch {
                try {
                    val response = generativeModel.generateContent(prompt)
                    binding.tvTips.text = response.text ?: "Response text is null"
                    Log.d("HomeFragment", response.text ?: "Response text is null")
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Error generating content", e)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}