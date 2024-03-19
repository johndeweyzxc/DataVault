package com.example.datavault.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.datavault.views.FavoritesFragment
import com.example.datavault.views.HomeFragment

class VpMain(
    fragmentManager: FragmentManager, lifeCycle: androidx.lifecycle.Lifecycle
) : FragmentStateAdapter(fragmentManager, lifeCycle) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> { HomeFragment() }
            1 -> { FavoritesFragment() }
            else -> throw IllegalStateException("Invalid position")
        }
    }
}