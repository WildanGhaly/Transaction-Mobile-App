package com.example.if3210_2024_android_ppl.ui.graph

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.if3210_2024_android_ppl.databinding.FragmentGraphBinding
import com.example.if3210_2024_android_ppl.database.transaction.TransactionDatabase
import com.example.if3210_2024_android_ppl.database.user.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GraphFragment : Fragment() {

    private var _binding: FragmentGraphBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val db by lazy { TransactionDatabase(requireContext()) }
    private lateinit var userViewModel: UserViewModel
    lateinit var pieChart: PieChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentGraphBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textGraph
//        graphViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        showPieChart()

        return root
    }

    fun showPieChart() {
        pieChart = binding.pieChart

        pieChart.setUsePercentValues(true)
        pieChart.getDescription().setEnabled(false)
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)
        pieChart.setDragDecelerationFrictionCoef(0.95f)
        pieChart.setDrawHoleEnabled(true)
        pieChart.setHoleColor(Color.WHITE)
        pieChart.setDrawCenterText(true)

        pieChart.setRotationAngle(0f)
        pieChart.setRotationEnabled(true)
        pieChart.setHighlightPerTapEnabled(true)

        pieChart.animateY(1400, Easing.EaseInOutQuad)

        pieChart.legend.isEnabled = true
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTextSize(12f)

        userViewModel.getActiveUserEmail { email ->
            CoroutineScope(Dispatchers.IO).launch {
                val transactions = db.transactionDao().getTransactions(email)
                val categoryMap = mutableMapOf<String?, Float>()
                transactions.forEach { transaction ->
                    val category = transaction.category
                    val price = transaction.price?.toFloat() ?: 0f // Provide a default value if price is null
                    categoryMap[category] = categoryMap.getOrDefault(category, 0f) + price
                }

                // Create pie entries
                val entries = mutableListOf<PieEntry>()
                categoryMap.forEach { (category, price) ->
                    entries.add(PieEntry(price, category))
                }

                // Create pie data set
                val dataSet = PieDataSet(entries, "Transactions")
                dataSet.colors = mutableListOf(
                    Color.parseColor("#FFBE8046"),
                    Color.parseColor("#FF751615")
                )

                // Create pie data
                val data = PieData(dataSet)
                data.setValueTextSize(12f)
                data.setValueTextColor(Color.WHITE)
                data.setValueFormatter(PercentFormatter(pieChart))

                // Update the UI on the main thread
                launch(Dispatchers.Main) {
                    pieChart.data = data
                    pieChart.invalidate()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}