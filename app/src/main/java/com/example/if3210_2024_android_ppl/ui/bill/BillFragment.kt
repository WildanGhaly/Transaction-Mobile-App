package com.example.if3210_2024_android_ppl.ui.bill

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.if3210_2024_android_ppl.R
import com.example.if3210_2024_android_ppl.api.BillItem
import com.example.if3210_2024_android_ppl.api.MultiBill

class BillFragment : Fragment() {

    private var items: List<BillItem>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_bill, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        view.let { super.onViewCreated(it, savedInstanceState) }
        val multiBill = arguments?.getParcelable<MultiBill>("arrBil")
        val billItems = multiBill?.items ?: listOf()

        setupRecyclerView(billItems)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                findNavController().navigate(R.id.action_navigation_bill_to_navigation_scan)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView(items: List<BillItem>) {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.itemsRecyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = BillAdapter(items)
    }

    companion object {
        fun newInstance(items: ArrayList<BillItem>): BillFragment {
            val fragment = BillFragment()
            val args = Bundle()
            args.putParcelableArrayList("items", items)
            fragment.arguments = args
            return fragment
        }
    }

}
