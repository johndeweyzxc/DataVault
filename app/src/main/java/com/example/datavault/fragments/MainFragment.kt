package com.example.datavault.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.datavault.R
import com.example.datavault.adapters.SeedAdapter
import com.example.datavault.adapters.VPMainAdapter
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import de.hdodenhof.circleimageview.CircleImageView

class MainFragment(private val seedAdapter: SeedAdapter) : Fragment() {

    private lateinit var firebase: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private var currentOrientation: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewPager: ViewPager2 = view.findViewById(R.id.viewPager)

        val navView: NavigationView = view.findViewById(R.id.navView)
        val toolBar: MaterialToolbar = view.findViewById(R.id.mainToolBar)
        val drawer: DrawerLayout = view.findViewById(R.id.drawerLayout)
        val bottomNav: BottomNavigationView = view.findViewById(R.id.bottomNavigation)

        setFirebase()
        setNavigationDrawer(navView, toolBar, drawer)
        setViewPager(bottomNav, viewPager, toolBar)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        currentOrientation = newConfig.orientation
        super.onConfigurationChanged(newConfig)
    }

    override fun onPause() {
        Log.i("DEV.LOG.INFO", "MainFragment is paused")
        super.onPause()
    }

    private fun setViewPager(bottomNav: BottomNavigationView, viewPager: ViewPager2, toolBar: MaterialToolbar) {
        viewPager.adapter = VPMainAdapter(childFragmentManager, lifecycle, seedAdapter)

        viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        toolBar.title = "Home"
                        bottomNav.selectedItemId = R.id.bottomNavMenuHome
                    }
                    1 -> {
                        toolBar.title = "Create new data"
                        bottomNav.selectedItemId = R.id.bottomNavCreateData
                    }
                    2 -> {
                        toolBar.title = "Favorites"
                        bottomNav.selectedItemId = R.id.bottomNavFavorites
                    }
                }
            }
        })

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottomNavMenuHome -> { viewPager.setCurrentItem(0, true);true }
                R.id.bottomNavCreateData -> { viewPager.setCurrentItem(1, true); true }
                R.id.bottomNavFavorites -> { viewPager.setCurrentItem(2, true); true }
                else -> false
            }
        }
    }

    private fun setFirebase() {
        firebase = FirebaseAuth.getInstance()
        currentUser = firebase.currentUser!!
    }

    private fun setNavigationDrawer(navView: NavigationView, toolbar: MaterialToolbar, drawer: DrawerLayout) {
        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val headerView: View = navView.getHeaderView(0)
        val avatar: CircleImageView = headerView.findViewById(R.id.ivNavHeaderAvatar)
        val username: TextView = headerView.findViewById(R.id.tvNavHeaderUsername)
        val email: TextView = headerView.findViewById(R.id.tvNavHeaderUserEmail)

        toolbar.setNavigationOnClickListener {
            drawer.open()
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            // Handle menu item selected
            when (menuItem.itemId) {
                R.id.menuAbout -> {
                    Log.i("DEV.LOG.INFO", "About is pressed")
                    drawer.close()
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
                    firebase.signOut()
                    GoogleSignIn.getClient(requireActivity(), gso).signOut()
                    true
                }
                else -> false
            }
        }

        if (currentUser.displayName == null) {
            // Get name from the email if user used sign in with email and password
            val createName = currentUser.email.toString().split("@")
            username.text = createName.first()
        } else {
            username.text = currentUser.displayName
        }

        email.text = currentUser.email

        if (currentUser.photoUrl == null) {
            // Set a default avatar if the user does not use google sign in
            avatar.foreground = ContextCompat.getDrawable(
                requireActivity(), R.drawable.ic_launcher_foreground
            )
        } else {
            Glide.with(this).load(currentUser.photoUrl).into(avatar)
        }
    }
}