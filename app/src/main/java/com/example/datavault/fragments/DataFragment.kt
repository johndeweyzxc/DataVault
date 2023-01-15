package com.example.datavault.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datavault.R
import com.example.datavault.adapters.DataAdapter

class DataFragmentFactory(private val dataAdapter: DataAdapter): FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when(className) {
            DataFragment::class.java.name -> DataFragment(dataAdapter)
            else -> super.instantiate(classLoader, className)
        }
    }
}

class DataFragment(private val dataAdapter: DataAdapter) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val scrollableView: RecyclerView = view.findViewById(R.id.rvMainScrollableView)
        scrollableView.adapter = dataAdapter
        scrollableView.layoutManager = LinearLayoutManager(context)
    }
}
