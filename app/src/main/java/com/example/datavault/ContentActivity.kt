package com.example.datavault

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.datavault.adapters.SeedAdapter
import com.example.datavault.databinding.ActivityContentBinding
import com.example.datavault.fragments.HomeFragment
import com.example.datavault.fragments.FavoritesFragment
import com.example.datavault.fragments.SearchFragment
import com.example.datavault.schema.DataModelConverter
import com.example.datavault.schema.SeedSchema
import com.example.datavault.viewModels.ContentViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView

class ContentActivity : AppCompatActivity() {

    private lateinit var viewModel: ContentViewModel
    private lateinit var homeFactory: HomeFragmentFactory
    private lateinit var homeFragment: HomeFragment

    private lateinit var binding: ActivityContentBinding
    private lateinit var seedAdapter: SeedAdapter

    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var firebaseInstance: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[ContentViewModel::class.java]
        seedAdapter = SeedAdapter(viewModel.listDataModel, viewModel.mapDataModel)
        homeFactory = HomeFragmentFactory(seedAdapter)

        supportFragmentManager.fragmentFactory = homeFactory

        super.onCreate(savedInstanceState)

        homeFragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader, HomeFragment::class.java.name) as HomeFragment

        setTheme(R.style.Theme_DataVault_Content)
        binding = ActivityContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // Set the firebase instances to get current user.
        setFirebaseInstance()
        // Check authentication, whenever the user clicks log out, it will sign out the user in firebase
        // instance and navigate to the login page which is the MainActivity.
        setFirebaseAuthListeners()
        // Set the Navigation drawer on the top right corner of the screen.
        setNavigationDrawer()
        // Set the bottom navigation menu
        setBottomNavigation()
        // Listen to realtime updates from a target collection in firestore firebase.
        subscribeToRealtimeUpdates(viewModel)
        // This shows the first default fragment which is DataFragment.
        setFirstFragment()

        binding.btnAddData.setOnClickListener {
            val intent = Intent(this, CreateDataActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (supportFragmentManager.backStackEntryCount > 1) {
                supportFragmentManager.popBackStack()
            } else if (supportFragmentManager.backStackEntryCount == 0) {
                moveTaskToBack(true)
            }
        }
    }

    private fun setFirebaseInstance() {
        firebaseInstance = FirebaseAuth.getInstance()
        currentUser = firebaseInstance.currentUser!!
    }

    private fun setFirebaseAuthListeners() {
        firebaseInstance.addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user == null) {
                // User is not signed in
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    private fun setNavigationDrawer() {
        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val nav: NavigationView = binding.navView
        val drawer: DrawerLayout = binding.drawerLayout
        val headerView: View = nav.getHeaderView(0)
        val avatar: CircleImageView = headerView.findViewById(R.id.ivNavHeaderAvatar)
        val username: TextView = headerView.findViewById(R.id.tvNavHeaderUsername)
        val email: TextView = headerView.findViewById(R.id.tvNavHeaderUserEmail)

        toggle = ActionBarDrawerToggle(this, drawer, R.string.open, R.string.close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        nav.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.menuAbout -> {
                    Toast.makeText(applicationContext, "Clicked About!", Toast.LENGTH_SHORT)
                        .show()
                }
                R.id.menuSeed -> {
                    // Add click listener
                }
                R.id.menuPrivacyPolicy -> {
                    // Add click listener
                }
                R.id.menuTermsOfUse -> {
                    // Add click listener
                }
                R.id.menuSettings -> {
                    // Add click listener
                }
                R.id.menuDarkMode -> {
                    // Add click listener
                }
                R.id.menuSignOut -> {
                    firebaseInstance.signOut()
                    GoogleSignIn.getClient(this, gso).signOut()
                }

            }
            true
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
            avatar.foreground = ContextCompat.getDrawable(this, R.drawable.ic_launcher_foreground)
        } else {
            Glide.with(this).load(currentUser.photoUrl).into(avatar)
        }
    }

    private fun setBottomNavigation() {
        binding.contentBottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottomNavMenuHome -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flContentFragment, HomeFragment(seedAdapter))
                        commit()
                    }
                    return@setOnItemSelectedListener true
                }
                R.id.bottomNavMenuFavorites -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flContentFragment, FavoritesFragment())
                        commit()
                    }
                    return@setOnItemSelectedListener true
                }
                R.id.bottomNavMenuSearch -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flContentFragment, SearchFragment())
                        commit()
                    }
                    return@setOnItemSelectedListener true
                } else -> {
                return@setOnItemSelectedListener false
            }
            }
        }
    }

    private fun subscribeToRealtimeUpdates(viewModel: ContentViewModel) {
        val dataRef: CollectionReference = Firebase.firestore.collection("generatedUserData")
            .document(currentUser.uid)
            .collection("data")

        dataRef.orderBy("createdAt").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                return@addSnapshotListener
            }
            for (changes in querySnapshot?.documentChanges!!) {
                when (changes.type) {
                    DocumentChange.Type.ADDED -> {
                        // Convert DocumentChange! to DataModel
                        val dataModel: SeedSchema = DataModelConverter().convertToModel(changes)
                        // Modify the data in the view model
                        val addedPos: Int = viewModel.addData(dataModel, dataModel.docId)
                        if (addedPos != -1) {
                            seedAdapter.notifyItemInserted(addedPos)
                            Log.i("DEV.LOG.INFO",
                                "Added new document [${dataModel.appName}] from firestore snapshot"
                            )
                        }
                    }
                    DocumentChange.Type.MODIFIED -> {
                        // Convert DocumentChange! to DataModel
                        val dataModel: SeedSchema = DataModelConverter().convertToModel(changes)
                        // Modify the data in the view model
                        val modifiedPos: Int = viewModel.modifyData(dataModel, dataModel.docId)
                        if (modifiedPos != -1) {
                            seedAdapter.notifyItemChanged(modifiedPos)
                            Log.i("DEV.LOG.INFO", "Document [${dataModel.appName}] has been modified")
                        }
                    }
                    DocumentChange.Type.REMOVED -> {
                        // Convert DocumentChange! to DataModel
                        val dataModel: SeedSchema = DataModelConverter().convertToModel(changes)
                        // Modify the data in the view model
                        val deletedPos: Int = viewModel.deleteData(dataModel.docId)
                        if (deletedPos != -1) {
                            seedAdapter.notifyItemRemoved(deletedPos)
                            Log.i("DEV.LOG.INFO", "Document [${dataModel.appName}] has been deleted")
                        }
                    }
                }
            }
        }
    }

    private fun setFirstFragment() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flContentFragment, HomeFragment(seedAdapter))
            commit()
        }
    }
}

