package com.example.datavault.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.datavault.fragments.FavoritesFragment
import com.example.datavault.fragments.HomeFragment
import com.example.datavault.fragments.SearchFragment

class ViewPagerAdapter(
    fragmentManager: FragmentManager,
    lifeCycle: androidx.lifecycle.Lifecycle,
    private val seedAdapter: SeedAdapter
) : FragmentStateAdapter(
    fragmentManager, lifeCycle
) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> { HomeFragment(seedAdapter) }
            1 -> { FavoritesFragment() }
            2 -> { SearchFragment() }
            else -> {
                HomeFragment(seedAdapter)
            }
        }
    }
}