package com.example.if3210_2024_android_ppl.ui.bill

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
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
        view.let { super.onViewCreated(it, savedInstanceState) }
        val args: BillFragmentArgs by navArgs()
        val multiBill = arguments?.getParcelable<MultiBill>("arrBil")
        val billItems = multiBill?.items ?: listOf()

        setupRecyclerView(billItems)
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
