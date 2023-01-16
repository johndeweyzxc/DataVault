package com.example.datavault

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.example.datavault.adapters.SeedAdapter
import com.example.datavault.fragments.HomeFragment

class HomeFragmentFactory(private val seedAdapter: SeedAdapter): FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when(className) {
            HomeFragment::class.java.name -> HomeFragment(seedAdapter)
            else -> super.instantiate(classLoader, className)
        }
    }
}