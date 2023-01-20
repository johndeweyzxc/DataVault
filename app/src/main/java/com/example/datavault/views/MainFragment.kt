package com.example.datavault.views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.datavault.MainActivity
import com.example.datavault.R
import com.example.datavault.adapters.VPMainAdapter
import com.example.datavault.databinding.FragmentMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMainBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setNavigationDrawer()
        setViewPager()
    }

    private fun setViewPager() {
        // val adapter = (activity as MainActivity).MainFragmentInterface().getAdapter()
        binding.viewPager.adapter = VPMainAdapter(childFragmentManager, lifecycle)

        // Set a listener to detect which page is selected on the view pager
        binding.viewPager.registerOnPageChangeCallback(viewPagerPageChange)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottomNavMenuHome -> {
                    binding.viewPager.setCurrentItem(0, true)
                    true
                }
                R.id.bottomNavCreateData -> {
                    binding.viewPager.setCurrentItem(1, true)
                    true
                }
                R.id.bottomNavFavorites -> {
                    binding.viewPager.setCurrentItem(2, true)
                    true
                }
                else -> false
            }
        }
    }

    private val viewPagerPageChange = object: ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            when (position) {
                0 -> {
                    binding.mainToolBar.title = "Home"
                    binding.bottomNavigation.selectedItemId = R.id.bottomNavMenuHome
                }
                1 -> {
                    binding.mainToolBar.title = "Create new data"
                    binding.bottomNavigation.selectedItemId = R.id.bottomNavCreateData
                }
                2 -> {
                    binding.mainToolBar.title = "Favorites"
                    binding.bottomNavigation.selectedItemId = R.id.bottomNavFavorites
                }
            }
        }
    }

    private fun setNavigationDrawer() {
        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val headerView: View = binding.navView.getHeaderView(0)
        val avatar: CircleImageView = headerView.findViewById(R.id.ivNavHeaderAvatar)
        val username: TextView = headerView.findViewById(R.id.tvNavHeaderUsername)
        val email: TextView = headerView.findViewById(R.id.tvNavHeaderUserEmail)

        val mainInterface = (activity as MainActivity).MainFragmentInterface()

        Glide.with(this).load(mainInterface.getUserPhoto()).into(avatar)
        username.text = mainInterface.getUserName()
        email.text = mainInterface.getUserEmail()

        binding.mainToolBar.setNavigationOnClickListener {
            binding.drawerLayout.open()
        }

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            // Handle menu item selected
            when (menuItem.itemId) {
                R.id.menuAbout -> {
                    Log.i("DEV.LOG.INFO", "About is pressed")
                    binding.drawerLayout.close()
                    true
                }
                R.id.menuHome -> {
                    // Add click listener
                    true
                }
                R.id.menuPrivacyPolicy -> {
                    // Add click listener
                    true
                }
                R.id.menuTermsOfUse -> {
                    // Add click listener
                    true
                }
                R.id.menuSettings -> {
                    // Add click listener
                    true
                }
                R.id.menuDarkMode -> {
                    // Add click listener
                    true
                }
                R.id.menuSignOut -> {
                    FirebaseAuth.getInstance().signOut()
                    GoogleSignIn.getClient(requireActivity(), gso).signOut()
                    true
                }
                else -> false
            }
        }
    }
}