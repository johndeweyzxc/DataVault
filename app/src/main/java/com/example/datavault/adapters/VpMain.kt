package com.example.datavault.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.datavault.views.*

// Adapter for view pager in MainFragment to show HomeFragment, CreateFragment and FavoritesFragment
class VpMain(fragmentManager: FragmentManager, lifeCycle: androidx.lifecycle.Lifecycle)
    : FragmentStateAdapter(fragmentManager, lifeCycle) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> { HomeFragment() }
            1 -> { CreateFragment() }
            2 -> { FavoritesFragment() }
            else -> throw IllegalStateException("Invalid position")
        }
    }
}