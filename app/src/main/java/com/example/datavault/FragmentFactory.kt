package com.example.datavault

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.example.datavault.adapters.SeedAdapter
import com.example.datavault.fragments.HomeFragment
import com.example.datavault.fragments.MainFragment

class HomeFragmentFactory(private val seedAdapter: SeedAdapter) : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            MainFragment::class.java.name -> {
                Log.i("DEV.LOG.INFO", "Instantiating MainFragment with seedAdapter argument")
                MainFragment(seedAdapter)
            }
            HomeFragment::class.java.name -> {
                Log.i("DEV.LOG.INFO", "Instantiating HomeFragment with seedAdapter argument")
                HomeFragment(seedAdapter)
            }
            else -> super.instantiate(classLoader, className)
        }
    }
}