package it.cinellialessia.yahtzee.db

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import it.cinellialessia.yahtzee.R
import it.cinellialessia.yahtzee.databinding.ActicityBarChartBinding

class BarChartActivity: AppCompatActivity() {

    private val sharedPrefFile = "PreferenceKotlin"
    private lateinit var binding: ActicityBarChartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActicityBarChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setBarChart()
    }

    private fun setBarChart() {
        //the total number of games played for each mode is recovered
        val sharedPreferences =  this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val gameSingle = sharedPreferences.getInt("NGameSINGLE", 0)
        val gameMulti = sharedPreferences.getInt("NGameMULTI", 0)
        val gameIA = sharedPreferences.getInt("NGameIA", 0)
        val gameBlue = sharedPreferences.getInt("NGameBLUE", 0)
        val gameOnly = sharedPreferences.getInt("NGameONLY", 0)
        val gameOnly2 = sharedPreferences.getInt("NGameONLY2", 0)

        //the overall score made for each mode is recovered
        val totalSingle = sharedPreferences.getInt("SINGLEPoint", 0)
        val totalMulti = sharedPreferences.getInt("MULTIPOINT", 0)
        val totalIAA = sharedPreferences.getInt("IAPoint", 0)
        val totalBlue = sharedPreferences.getInt("BLUEPOINT", 0)
        val totalOnly = sharedPreferences.getInt("ONLYPoint", 0)
        val totalOnly2 = sharedPreferences.getInt("ONLY2POINT", 0)

        //array with the total number of games played for each mode
        val values = ArrayList<String>()
        values.add(gameSingle.toString())
        values.add(gameMulti.toString())
        values.add(gameIA.toString())
        values.add(gameBlue.toString())
        values.add(gameOnly.toString())
        values.add(gameOnly2.toString())

        //array of color
        val color = ArrayList<Int>()

        val xAxis = binding.barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textSize = 12f
        xAxis.setDrawGridLines(false)
        binding.barChart.axisLeft.textSize = 8f
        binding.barChart.axisRight.setDrawLabels(false)
        binding.barChart.axisLeft.mAxisMaximum = 375f
        binding.barChart.axisRight.mAxisMaximum = 375f

        //I add the average of the values between games and the overall score for each mode
        val barChartEntry = ArrayList<BarEntry>()

        if (gameSingle != 0) {
            val single = totalSingle.toFloat()/gameSingle.toFloat()
            color.add(getColor(R.color.red))
            barChartEntry.add(BarEntry(single, 0))
        }
        if (gameMulti != 0) {
            val multi = totalMulti.toFloat()/gameMulti.toFloat()
            color.add(getColor(R.color.purple))
            barChartEntry.add(BarEntry(multi, 1))
        }
        if (gameIA != 0) {
            val ia = totalIAA.toFloat()/gameIA.toFloat()
            color.add(getColor(R.color.anti_flash))
            barChartEntry.add(BarEntry(ia, 2))
        }
        if (gameBlue != 0) {
            val blue = totalBlue.toFloat()/gameBlue.toFloat()
            color.add(getColor(R.color.button))
            barChartEntry.add(BarEntry(blue, 3))
        }
        if (gameOnly != 0) {
            val only = totalOnly.toFloat()/gameOnly.toFloat()
            color.add(getColor(R.color.play))
            barChartEntry.add(BarEntry(only, 4))
        }
        if (gameOnly2 != 0) {
            val only2 = totalOnly2.toFloat()/gameOnly2.toFloat()
            color.add(getColor(R.color.yellow))
            barChartEntry.add(BarEntry(only2, 5))
        }

        val barDataSet = BarDataSet(barChartEntry,"")

        val data = BarData(values, barDataSet)
        binding.barChart.data = data
        barDataSet.colors = color
        barDataSet.valueTextSize = 12f
        binding.barChart.setDescription("")
        binding.barChart.setTouchEnabled(false)

        binding.barChart.setScaleEnabled(false)
        binding.barChart.setBackgroundResource(R.color.backgroundMenu)
        binding.barChart.animateXY(1000,1000)

        binding.barChart.legend.isEnabled = false
    }
}
