package com.doi.ecosaver.ui.home.fuel

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.viewModelFactory
import com.doi.ecosaver.R
import com.doi.ecosaver.data.BensinData
import com.doi.ecosaver.data.BensinDetail
import com.doi.ecosaver.data.DataRepository
import com.doi.ecosaver.data.RemoteDataSource
import com.doi.ecosaver.databinding.ActivityFuelBinding
import com.doi.ecosaver.utils.ViewModelFactory
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import java.util.Calendar

class FuelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFuelBinding
    private lateinit var viewModel: FuelActivityViewModel
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFuelBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val dataRepository = DataRepository(RemoteDataSource())
        val viewModelFactory = ViewModelFactory(dataRepository)
        viewModel = ViewModelProvider(this, viewModelFactory)[FuelActivityViewModel::class.java]
        val idUser = auth.currentUser?.uid ?: ""
        val currentMonthYear = getCurrentMonthYear()
        val lastMonthYear = getLastMonthYear()
        binding.apply {
            viewModel.apply {
                var currentBensin: Float? = null
                var lastBensin: Float? = null

                getBensinDetail(idUser, currentMonthYear).observe(this@FuelActivity) { bensin ->
                    currentBensin = bensin?.liter?.toFloat() ?: 0f
                    tvDetail.text = "${currentBensin?.toInt() ?: 0}L"
                }

                getBensinDetail(idUser, lastMonthYear).observe(this@FuelActivity) { bensin ->
                    lastBensin = bensin?.liter?.toFloat() ?: 0f
                    tvDetailVs.text = "vs ${lastBensin?.toInt() ?: 0}L"
                }
            }
        }

        viewModel.bensinDataList.observe(this) { list ->
            showLineChart(list) // fungsi yang sudah kita bahas sebelumnya
            val prompt = "Berikan Tips Hari Ini berdasarkan data $list tanda baca pakai emoticon saja"
            val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = "GEMINIAPIKEY"
            )



            lifecycleScope.launch {
                try {
                    val response = generativeModel.generateContent(prompt)
                    binding.tvDetailFuelTips.text = response.text ?: "Response text is null"
                    Log.d("HomeFragment", response.text ?: "Response text is null")
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Error generating content", e)
                }
            }
        }

        viewModel.loadBensinData()



    }

    private fun showLineChart(data: List<BensinDetail>) {
        val entries = data.mapIndexed { index, item ->
            Entry(index.toFloat(), item.liter)
        }

        val dataSet = LineDataSet(entries, "Liter Bensin per Bulan").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            circleRadius = 5f
            setCircleColor(Color.RED)
            lineWidth = 2f
            valueTextSize = 10f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        binding.chartFuel.apply {
            this.data = LineData(dataSet)
            description.text = "Grafik Pemakaian Bensin"
            xAxis.valueFormatter = IndexAxisValueFormatter(data.map { it.monthYear })
            xAxis.granularity = 1f
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            animateX(1500)
            invalidate()
        }
    }
    private fun getLastMonthYear(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        return "$year-${month.toString().padStart(2, '0')}"
    }
    private fun getCurrentMonthYear(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        return "$year-${month.toString().padStart(2, '0')}"
    }


}