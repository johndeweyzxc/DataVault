package com.example.datavault.views

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.johndeweydev.datavault.R
import com.example.datavault.adapters.VpAboutApp
import com.google.android.material.tabs.TabLayout
import com.johndeweydev.datavault.databinding.FragmentAboutAppBinding

class AboutAppFragment : Fragment() {

    private lateinit var binding: FragmentAboutAppBinding
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager2: ViewPager2
    private lateinit var adapter: VpAboutApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        // When this fragment enter, slide to the left
        enterTransition = inflater.inflateTransition(R.transition.fragment_slide_right)
        // When this fragment exit, slide to the right
        exitTransition = inflater.inflateTransition(R.transition.fragment_slide_right)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAboutAppBinding.inflate(layoutInflater)
        tabLayout = binding.aboutAppTabLayout
        viewPager2 = binding.aboutAppViewPager
        adapter = VpAboutApp(childFragmentManager, lifecycle)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.aboutAppToolBar.setOnClickListener { parentFragmentManager.popBackStack() }

        tabLayout.addTab(tabLayout.newTab().setText("About"))
        tabLayout.addTab(tabLayout.newTab().setText("Privacy"))
        tabLayout.addTab(tabLayout.newTab().setText("Terms"))

        viewPager2.adapter = adapter

        // When a tab item is clicked set the specific page on view pager
        tabLayout.addOnTabSelectedListener(
            object: TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (tab != null) {
                        viewPager2.currentItem = tab.position
                    }
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            }
        )

        // Notify tab layout about the page selected on view pager
        viewPager2.registerOnPageChangeCallback(
            object: ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    tabLayout.selectTab(tabLayout.getTabAt(position))
                }
            }
        )
    }
}