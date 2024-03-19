package com.example.datavault.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.datavault.views.AboutFragment
import com.example.datavault.views.PrivacyPolicyFragment
import com.example.datavault.views.TermsFragment

class VpAboutApp(
    fragmentManager: FragmentManager, lifeCycle: androidx.lifecycle.Lifecycle
) : FragmentStateAdapter(fragmentManager, lifeCycle) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AboutFragment()
            1 -> PrivacyPolicyFragment()
            2 -> TermsFragment()
            else -> throw IllegalStateException("Invalid position")
        }
    }
}