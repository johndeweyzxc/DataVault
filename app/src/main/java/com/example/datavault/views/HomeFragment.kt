package com.example.datavault.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datavault.MainActivity
import com.johndeweydev.datavault.R
import com.johndeweydev.datavault.databinding.FragmentHomeBinding
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class HomeFragment : Fragment() {

    private var isExtended = false
    private lateinit var binding: FragmentHomeBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var fab: ExtendedFloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        mainActivity = (activity as MainActivity)
        val parentFragment = (parentFragment as MainFragment)
        fab = parentFragment.fab
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val scrollListener = object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                val lastVisibleItem = linearLayoutManager?.findLastCompletelyVisibleItemPosition()
                val lastAdapterItem = mainActivity.viewModel.countSeed() - 1

                if (linearLayoutManager != null && lastVisibleItem == lastAdapterItem) {
                    fab.extend()
                    isExtended = true
                } else {
                    if (isExtended) {
                        fab.shrink()
                        isExtended = false
                    }
                }
            }
        }

        fab.shrink()

        val scrollableView: RecyclerView = view.findViewById(R.id.rvMainScrollableView)
        scrollableView.addOnScrollListener(scrollListener)

        scrollableView.adapter = mainActivity.seedAdapter
        scrollableView.layoutManager = LinearLayoutManager(context)
    }
}
