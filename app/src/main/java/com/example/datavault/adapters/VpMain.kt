package com.example.datavault.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.datavault.views.*
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

// Adapter for view pager in MainFragment to show HomeFragment, CreateFragment and FavoritesFragment
class VpMain(
    fragmentManager: FragmentManager, lifeCycle: androidx.lifecycle.Lifecycle,
    private val fab: ExtendedFloatingActionButton
) : FragmentStateAdapter(fragmentManager, lifeCycle) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> { HomeFragment(fab) }
            1 -> { FavoritesFragment() }
            else -> throw IllegalStateException("Invalid position")
        }
    }
}