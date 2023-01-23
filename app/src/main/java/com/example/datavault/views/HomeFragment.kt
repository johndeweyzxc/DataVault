package com.example.datavault.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datavault.MainActivity
import com.example.datavault.R
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class HomeFragment(private val fab: ExtendedFloatingActionButton) : Fragment() {

    private var isExtended = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val scrollableView: RecyclerView = view.findViewById(R.id.rvMainScrollableView)
        fab.shrink()

        scrollableView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == (activity as MainActivity).adapterItemCount() - 1) {
                    fab.extend()
                    isExtended = true
                } else {
                    if (isExtended) {
                        fab.shrink()
                        isExtended = false
                    }
                }
            }
        })

        scrollableView.adapter = (activity as MainActivity).homeAdapter()
        scrollableView.layoutManager = LinearLayoutManager(context)
    }
}
