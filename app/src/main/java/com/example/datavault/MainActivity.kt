package com.example.datavault

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.datavault.adapters.RvFavorite
import com.example.datavault.adapters.RvHome
import com.example.datavault.models.MainViewModel
import com.example.datavault.schema.SeedSchema
import com.example.datavault.views.MainFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    var currentUser: FirebaseUser? = null
    lateinit var viewModel: MainViewModel
    lateinit var seedAdapter: RvHome
    lateinit var favoriteAdapter: RvFavorite

    override fun onCreate(savedInstanceState: Bundle?) {
        currentUser = FirebaseAuth.getInstance().currentUser
        authListener()

        setContentView(R.layout.activity_main)
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        favoriteAdapter = RvFavorite(this)
        seedAdapter = RvHome(this)

        subscribeToRealtimeUpdates()

        // Set the MainFragment as the first fragment to appear on the activity
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frameLayoutActivityMain, MainFragment())
            commit()
        }
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        // If the user presses back button, pop the fragment stack or pause the activity
        // if there is only one fragment.
        override fun handleOnBackPressed() {
            if (supportFragmentManager.backStackEntryCount == 0) {
                moveTaskToBack(true)
            } else {
                supportFragmentManager.popBackStack()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        // This handles the configuration change of the screen to prevent restart of MainActivity.
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.i("devlog", "Screen orientation changed to landscape in MainActivity")
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.i("devlog", "Screen orientation changed to portrait in MainActivity")
        }
        super.onConfigurationChanged(newConfig)
    }

    private fun authListener() {
        FirebaseAuth.getInstance().addAuthStateListener { auth -> userMightBeNull(auth.currentUser) }
    }

    fun userMightBeNull(user: FirebaseUser?) {
        if (user == null) {
            Log.i("devlog", "User is null, navigating to AuthActivity")
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun subscribeToRealtimeUpdates() {
        userMightBeNull(currentUser)
        val generatedUserData = Firebase.firestore.collection("generatedUserData")
        val userUid = generatedUserData.document(currentUser!!.uid)
        val data = userUid.collection("data")
        val query = data.orderBy("createdAt")

        // Listen to any update from the database and determine if the update is about
        // appending new data, modifying existing data or deleting existing data.
        query.addSnapshotListener{ snapshot, exception ->
            if (exception?.message != null) {
                Log.i("devlog", exception.message!!)
            } else {
                for (changes in snapshot?.documentChanges!!) {
                    when (changes.type) {
                        DocumentChange.Type.ADDED -> handleNewData(changes)
                        DocumentChange.Type.MODIFIED -> handleModifiedData(changes)
                        DocumentChange.Type.REMOVED -> handleRemovedData(changes)
                    }
                }
            }
        }
    }

    private fun handleNewData(changes: DocumentChange) {
        val dataModel: SeedSchema = viewModel.convertToModel(changes)
        // Append data in the view model, returns a list, the first item is the index
        // position of added data, the last item is the index position of the added
        // data in favorites.
        val addedPos: List<Int> = viewModel.addData(dataModel)
        // addedPos might return -1, this means that there is no need to notify
        // the adapter about the change.

        seedAdapter.notifyItemInserted(addedPos.first())

        if (addedPos.last() != -1) {
            favoriteAdapter.notifyItemInserted(addedPos.last())
            Log.i("devlog", "Added [${dataModel.appName}] to favorites")
        }
    }

    private fun handleModifiedData(changes: DocumentChange) {
        val dataModel: SeedSchema = viewModel.convertToModel(changes)
        // Modify the data in the view model, returns a list, the first item is the index position
        // of modified data in home the last item is the index position of the modified
        // data in favorites.
        val modifiedPos: List<Int> = viewModel.modifyData(dataModel)
        // modifiedPos might return -1, this means that there is no need to notify
        // the adapter about the change.

        if (modifiedPos.first() != -1) {
            seedAdapter.notifyItemChanged(modifiedPos.first())
            Log.i("devlog", "Document [${dataModel.appName}] has been modified")
        }

        if (modifiedPos.last() != -1) {
            favoriteAdapter.notifyItemRemoved(modifiedPos.last())
            Log.i("devlog", "Document [${dataModel.appName}] has also been modified in favorites")
        }
    }

    private fun handleRemovedData(changes: DocumentChange) {
        val dataModel: SeedSchema = viewModel.convertToModel(changes)
        // Delete the data in the view model, returns a list, the first item is the index position
        // of deleted data in home, the last item is the index position of the deleted
        // data in favorites.
        val deletedPos: List<Int> = viewModel.deleteData(dataModel)
        // deletedPos might return -1, this means that there is no need to notify
        // the adapter about the change.

        if (deletedPos.first() != -1) {
            seedAdapter.notifyItemRemoved(deletedPos.first())
            Log.i("devlog", "Document [${dataModel.appName}] has been deleted")
        }

        if (deletedPos.last() != -1) {
            favoriteAdapter.notifyItemRemoved(deletedPos.last())
            Log.i("devlog", "Document [${dataModel.appName}] has also been deleted in favorites")
        }
    }
}