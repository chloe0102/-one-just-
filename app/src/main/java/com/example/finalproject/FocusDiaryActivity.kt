package com.example.finalproject

import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.finalproject.databinding.FocusdiaryBinding
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class FocusDiaryActivity : AppCompatActivity() {

    private lateinit var binding: FocusdiaryBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var barChart: BarChart
    private val db = FirebaseFirestore.getInstance()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化 View Binding
        binding = FocusdiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化 BarChart
        barChart = binding.chartFocusTime

        // 初始化 Firebase Auth
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // 驗證登入
        if (currentUser == null) {
            finish() // 未登入，直接返回
            return
        }
        val userId = currentUser.uid

        // 返回按鈕設定
        binding.backButton.setOnClickListener {
            finish()
        }

        // 預設載入當天資料
        val todayDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        setSelectedButton(binding.btnDay, listOf(binding.btnWeek, binding.btnMonth, binding.btnSeason, binding.btnYear))
        fetchFocusData(userId, todayDate)

        // 按鈕點擊事件
        binding.btnDay.setOnClickListener {
            setSelectedButton(binding.btnDay, listOf(binding.btnWeek, binding.btnMonth, binding.btnSeason, binding.btnYear))
            fetchFocusData(userId, todayDate)
        }
        binding.btnWeek.setOnClickListener {
            setSelectedButton(binding.btnWeek, listOf(binding.btnDay, binding.btnMonth, binding.btnSeason, binding.btnYear))
            updateUI("本周數據", generateMockData())
        }
        binding.btnMonth.setOnClickListener {
            setSelectedButton(binding.btnMonth, listOf(binding.btnDay, binding.btnWeek, binding.btnSeason, binding.btnYear))
            updateUI("本月數據", generateMockData())
        }
        binding.btnSeason.setOnClickListener {
            setSelectedButton(binding.btnSeason, listOf(binding.btnDay, binding.btnWeek, binding.btnMonth, binding.btnYear))
            updateUI("本季數據", generateMockData())
        }
        binding.btnYear.setOnClickListener {
            setSelectedButton(binding.btnYear, listOf(binding.btnDay, binding.btnWeek, binding.btnMonth, binding.btnSeason))
            updateUI("本年數據", generateMockData())
        }
    }

    private fun fetchFocusData(userId: String, date: String) {
        db.collection("users").document(userId)
            .collection("concentrateTime").document(date)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val totalFocusTime = document.getLong("totalFocusTime") ?: 0L
                    val longestFocusTime = document.getLong("longestFocus") ?: 0L
                    val startFocusTime = document.getTimestamp("earliestStartTime")
                    val endFocusTime = document.getTimestamp("latestEndTime")

                    // 更新 UI
                    binding.tvTotalFocusTime.text = formatDuration(totalFocusTime)
                    binding.tvLongestFocusTime.text = formatDuration(longestFocusTime)
                    binding.tvStartFocusTime.text = timestampToString(startFocusTime)
                    binding.tvEndFocusTime.text = timestampToString(endFocusTime)

                    // 加載專注會話數據
                    fetchFocusSessions(userId, date)
                } else {
                    resetUI()
                }
            }
            .addOnFailureListener { e ->
                println("讀取失敗：$e")
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchFocusSessions(userId: String, date: String) {
        db.collection("users").document(userId)
            .collection("concentrateTime").document(date)
            .collection("focusSessions")
            .get()
            .addOnSuccessListener { result ->
                val entries = mutableListOf<BarEntry>() // 用於存儲每個小時的專注分鐘數

                for (document in result) {
                    val startTime = document.getTimestamp("startTime") ?: continue
                    val durationInSeconds = document.getLong("duration") ?: continue

                    // 將 startTime 轉為 LocalDateTime 並提取小時
                    val startHour = ZonedDateTime.ofInstant(
                        Instant.ofEpochSecond(startTime.seconds),
                        ZoneId.systemDefault() // 使用設備的時區
                    ).hour

                    val durationInMinutes = durationInSeconds / 60

                    // 累加到對應小時
                    val existingEntry = entries.find { it.x == startHour.toFloat() }
                    if (existingEntry != null) {
                        existingEntry.y += durationInMinutes.toFloat()
                    } else {
                        entries.add(BarEntry(startHour.toFloat(), durationInMinutes.toFloat()))
                    }
                }

                println("圖表數據: $entries") // 打印檢查數據是否正確
                updateChart(entries)
            }
            .addOnFailureListener { e ->
                println("讀取 focusSessions 失敗：$e")
            }
    }



    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
    }

    private fun updateChart(entries: MutableList<BarEntry>) {
        if (entries.isEmpty()) {
            barChart.clear()
            barChart.invalidate()
            println("圖表數據為空")
            return
        }

        val dataSet = BarDataSet(entries, "專注時長 (分鐘)")
        dataSet.color = getColor(R.color.blue)
        dataSet.setDrawValues(true)

        val barData = BarData(dataSet)
        barData.barWidth = 0.3f

        barChart.data = barData
        configureBarChart()
        barChart.invalidate()
    }



    private fun configureBarChart() {
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false

        val xAxis = barChart.xAxis
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = 24f
        xAxis.labelCount = 24
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value.toInt() % 6 == 0) "${value.toInt()}:00" else ""
            }
        }

        val yAxisLeft = barChart.axisLeft
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.axisMaximum = 60f // Y 軸最大值為 60 分鐘
        yAxisLeft.labelCount = 4
        yAxisLeft.setDrawGridLines(false)

        barChart.axisRight.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setScaleEnabled(false)
        barChart.setPinchZoom(false)
        barChart.setExtraOffsets(0f, 10f, 0f, 10f)
        barChart.setFitBars(true)
    }

    private fun timestampToString(timestamp: Timestamp?): String {
        return timestamp?.toDate()?.let {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
        } ?: "N/A"
    }

    private fun resetUI() {
        binding.tvTotalFocusTime.text = "N/A"
        binding.tvLongestFocusTime.text = "N/A"
        binding.tvStartFocusTime.text = "N/A"
        binding.tvEndFocusTime.text = "N/A"
        barChart.clear()
        barChart.invalidate()
    }

    private fun setSelectedButton(selected: Button, others: List<Button>) {
        selected.isSelected = true
        others.forEach { it.isSelected = false }
    }

    private fun updateUI(title: String, data: MutableList<BarEntry>) {
        binding.tvTotalFocusTime.text = title
        updateChart(data)
    }

    private fun generateMockData(): MutableList<BarEntry> {
        return mutableListOf(
            BarEntry(5f, 30f), BarEntry(11f, 50f), BarEntry(17f, 20f), BarEntry(23f, 40f)
        )
    }
}
