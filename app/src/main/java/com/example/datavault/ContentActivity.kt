package com.example.datavault

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ContentActivity : AppCompatActivity() {

    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var dataAdapter: DataAdapter
    private lateinit var firebaseInstance: FirebaseAuth
    private lateinit var gso: GoogleSignInOptions
    private val dataRef = Firebase.firestore.collection("dataVault")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content)
        // Get the instance of firebase auth to check authentication
        firebaseInstance = FirebaseAuth.getInstance()

        // Create an empty mutable list of data
        dataAdapter = DataAdapter(mutableListOf(), HashMap())

        // Configure Google Sign In
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Check if user logs out then return to login activity
        setAuthListener()

        // Set the navigation drawer and set click listeners on menu items
        setListenersOnNavMenu()
        setDrawerLayout()

        val name = intent.getStringExtra("name")
        val email = intent.getStringExtra("email")

        val rvMainScrollableView = findViewById<RecyclerView>(R.id.rvMainScrollableView)
        rvMainScrollableView.adapter = dataAdapter
        rvMainScrollableView.layoutManager = LinearLayoutManager(this)

        // Subscribe to realtime updates
        subscribeToRealtimeUpdates()

        val btnAddData = findViewById<FloatingActionButton>(R.id.btnAddData)
        btnAddData.setOnClickListener {
            val intent = Intent(this, CreateDataActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setDrawerLayout() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        // Toggle is now ready to be use
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setListenersOnNavMenu() {
        val navView = findViewById<NavigationView>(R.id.navView)
        navView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.menuAbout -> {
                    Toast.makeText(applicationContext, "Clicked About!", Toast.LENGTH_SHORT)
                        .show()
                }
                R.id.menuSignOut -> {
                    firebaseInstance.signOut()
                    GoogleSignIn.getClient(this, gso).signOut()
                }
            }
            true
        }
    }

    private fun setAuthListener() {
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

    private fun subscribeToRealtimeUpdates() {
        dataRef.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Toast.makeText(applicationContext, it.message, Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            for (changes in querySnapshot?.documentChanges!!) {
                when (changes.type) {
                    DocumentChange.Type.ADDED -> {
                        val dataModel: DataModel = dataAdapter.queryToModel(changes)
                        dataAdapter.onAddData(dataModel, dataModel.docId)
                    }
                    DocumentChange.Type.MODIFIED -> {
                        val dataModel: DataModel = dataAdapter.queryToModel(changes)
                        dataAdapter.onModifyData(dataModel, dataModel.docId)
                    }
                    DocumentChange.Type.REMOVED -> {
                        val dataModel: DataModel = dataAdapter.queryToModel(changes)
                        dataAdapter.onDeleteData(dataModel.docId)
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

