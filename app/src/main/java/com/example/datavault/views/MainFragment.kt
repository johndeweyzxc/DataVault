package com.example.datavault.views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.datavault.Database
import com.example.datavault.MainActivity
import com.johndeweydev.datavault.R
import com.example.datavault.adapters.VpMain
import com.johndeweydev.datavault.databinding.FragmentMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView

class MainFragment : Fragment(), Database {

    private lateinit var binding: FragmentMainBinding
    private lateinit var mainActivity: MainActivity
    lateinit var fab: ExtendedFloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMainBinding.inflate(layoutInflater)
        mainActivity = (activity as MainActivity)
        fab = binding.floatinActionButton
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setNavigationDrawer()
        setViewPager()

        binding.floatinActionButton.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                add(R.id.frameLayoutActivityMain, CreateSeedDialog())
                addToBackStack("CreateFragment")
                commit()
            }
        }
    }

    private fun setViewPager() {
        binding.viewPager.adapter = VpMain(childFragmentManager, lifecycle)

        // Update bottom navigation indicator when user switches to different page in view pager.
        val viewPagerPageChange = object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        binding.mainToolBar.title = "Home"
                        binding.bottomNavigation.selectedItemId = R.id.bottomNavMenuHome
                    }
                    1 -> {
                        binding.mainToolBar.title = "Favorites"
                        binding.bottomNavigation.selectedItemId = R.id.bottomNavFavorites
                    }
                }
            }
        }

        binding.viewPager.registerOnPageChangeCallback(viewPagerPageChange)

        // When bottom navigation menu item is clicked navigate to a specific page in view pager.
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottomNavMenuHome -> {
                    binding.viewPager.setCurrentItem(0, true)
                    true
                }
                R.id.bottomNavFavorites -> {
                    binding.viewPager.setCurrentItem(1, true)
                    true
                }
                else -> false
            }
        }
    }

    private fun setDefaultUserProfileImage(imageView: CircleImageView) {
        if (mainActivity.currentUser?.photoUrl == null) {
            Glide.with(this).load(getString(R.string.default_user_photo)).into(imageView)
        } else {
            Glide.with(this).load(mainActivity.currentUser?.photoUrl).into(imageView)
        }
    }

    private fun setNavigationDrawer() {
        if (mainActivity.currentUser == null) { mainActivity.navigateToAuthActivity() }

        val headerView: View = binding.navView.getHeaderView(0)
        val avatar: CircleImageView = headerView.findViewById(R.id.ivNavHeaderAvatar)
        val username: TextView = headerView.findViewById(R.id.tvNavHeaderUsername)
        val email: TextView = headerView.findViewById(R.id.tvNavHeaderUserEmail)

        mainActivity.viewModel.userProfileExists.observe(viewLifecycleOwner) { exists ->
            if (!exists) {

                // Set a default name if the user does not sign up using gmail. Signing up using email
                // does not provide a default name for the user
                val name = if (mainActivity.currentUser?.displayName == null) {
                    val newName = mainActivity.currentUser?.email?.split("@")
                    newName!!.first()
                } else {
                    mainActivity.currentUser?.displayName!!
                }

                // Upload user profile document in firestore if it does not exists.
                uploadInitialUserProfileData(requireActivity(), name)
            }
        }

        // Set the default profile image of user
        setDefaultUserProfileImage(avatar)

        // Get the profile image of user if it exists in the storage and update the UI accordingly.
        // This overrides value set by setDefaultUserProfile().
        overrideDefaultUserProfileImage(this, avatar, mainActivity.currentUser!!.uid)

        // Observe any change in the view model and update UI accordingly.
        mainActivity.viewModel.userEmail.observe(viewLifecycleOwner) { email.text = it }
        mainActivity.viewModel.userName.observe(viewLifecycleOwner) { username.text = it }

        // Set a click listener on the circle image view in header
        avatar.setOnClickListener {
            mainActivity.viewModel.currentUserProfileImage = avatar.drawable.toBitmap()
            mainActivity.supportFragmentManager.beginTransaction().apply {
                add(R.id.frameLayoutActivityMain, UserProfileFragment())
                addToBackStack("UserProfileFragment")
                commit()
            }
            binding.drawerLayout.close()
        }

        // Set click listeners on navigation drawer items
        binding.mainToolBar.setNavigationOnClickListener { binding.drawerLayout.open() }

        binding.navView.setNavigationItemSelectedListener { menuItem ->

            when (menuItem.itemId) {
                R.id.menuHome -> {
                    binding.viewPager.setCurrentItem(0, true)
                    binding.drawerLayout.close()
                    true
                }
                R.id.menuFavorites -> {
                    binding.viewPager.setCurrentItem(1, true)
                    binding.drawerLayout.close()
                    true
                }
                R.id.menuSettings -> {
                    navigateToSettings()
                    binding.drawerLayout.close()
                    true
                }

                R.id.menuAccountSettings -> {
                    navigateToUserProfileFragment(avatar)
                    binding.drawerLayout.close()
                    true
                }
                R.id.menuSignOut -> {
                    signOutUser()
                    true
                }

                R.id.menuAbout -> {
                    navigateToAboutAppFragment()
                    binding.drawerLayout.close()
                    true
                }
                R.id.menuPrivacyPolicy -> {
                    navigateToAboutAppFragment()
                    binding.drawerLayout.close()
                    true
                }
                R.id.menuTermsOfUse -> {
                    navigateToAboutAppFragment()
                    binding.drawerLayout.close()
                    true
                }
                else -> false
            }
        }
    }

    private fun signOutUser() {
        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        try {
            FirebaseAuth.getInstance().signOut()
            GoogleSignIn.getClient(requireActivity(), gso).signOut()
        } catch (e: Exception) {
            if (e.message != null) {
                Log.i("devlog", e.message!!)
            }
            Toast.makeText(requireActivity(), "There is a problem signing out", Toast.LENGTH_LONG).show()
        }
    }

    private fun navigateToAboutAppFragment() {
        mainActivity.supportFragmentManager.beginTransaction().apply {
            add(R.id.frameLayoutActivityMain, AboutAppFragment())
            addToBackStack("AboutAppFragment")
            commit()
        }
    }

    private fun navigateToSettings() {
        mainActivity.supportFragmentManager.beginTransaction().apply {
            add(R.id.frameLayoutActivityMain, SettingsFragment())
            addToBackStack("SettingsFragment")
            commit()
        }
    }

    private fun navigateToUserProfileFragment(avatar: CircleImageView) {
        mainActivity.supportFragmentManager.beginTransaction().apply {
            mainActivity.viewModel.currentUserProfileImage = avatar.drawable.toBitmap()
            add(R.id.frameLayoutActivityMain, UserProfileFragment())
            addToBackStack("UserProfileFragment")
            commit()
        }
    }
}