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
import com.johndeweydev.datavault.R

class MainActivity : AppCompatActivity(), Database {

    var currentUser: FirebaseUser? = null
    lateinit var viewModel: MainViewModel
    lateinit var seedAdapter: RvHome
    lateinit var favoriteAdapter: RvFavorite

    override fun onCreate(savedInstanceState: Bundle?) {
        currentUser = FirebaseAuth.getInstance().currentUser
        authListener()

        setContentView(R.layout.activity_main)
        super.onCreate(savedInstanceState)
        Log.i("devlog", "[onCreate()] MainActivity launched")
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        favoriteAdapter = RvFavorite(this)
        seedAdapter = RvHome(this)

        // Listen to any changes in the database if user is logged in
        if (currentUser != null) {
            subscribeToRealtimeUpdates()
            subscribeUserProfileUpdates()

            // Set the MainFragment as the first fragment to appear on the activity
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.frameLayoutActivityMain, MainFragment())
                commit()
            }
        }
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        // If the user presses back button, pop the fragment stack or pause the activity
        // if there is only one fragment.
        override fun handleOnBackPressed() {
            val countStack = supportFragmentManager.backStackEntryCount
            val latestFragment = supportFragmentManager.getBackStackEntryAt(countStack - 1)

            Log.i("devlog", "[handleOnBackPressed()] Back button is pressed")
            if (countStack == 0) {
                moveTaskToBack(true)
            } else {
                supportFragmentManager.popBackStack()
            }
            Log.i("devlog",
                "[handleOnBackPressed()] Current fragment back stack count in MainActivity -> $countStack"
            )
            Log.i("devlog",
                "[handleOnBackPressed()] Current top of fragment stack in MainActivity " +
                        "-> ${latestFragment.name}"
            )
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        // This handles the configuration change of the screen to prevent restart of MainActivity.
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.w("devlog",
                "[onConfigurationChanged()] Screen orientation changed to landscape in MainActivity")
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.w("devlog",
                "[onConfigurationChanged()] Screen orientation changed to portrait in MainActivity")
        }
        super.onConfigurationChanged(newConfig)
    }

    fun navigateToAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun authListener() {
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            if (auth.currentUser == null) {
                Log.w("devlog", "[authListener()] User is null, navigate to AuthActivity")
                navigateToAuthActivity()
            }
        }
    }

    private fun subscribeUserProfileUpdates() {
        val generatedUserData = Firebase.firestore.collection("generatedUserData")
        val userIdDocRef = generatedUserData.document(currentUser!!.uid)
        val dataColRef = userIdDocRef.collection("profile")
        val targetDocument = dataColRef.document("user")

        // Set default user profile data in the view model
        setDefaultUserProfileValue()

        // Listens to any change on the document, overrides value set by setDefaultUserProfileValue().
        targetDocument.addSnapshotListener{ snapshot, exception ->
            if (exception != null) {
                Log.w("devlog", "[subscribeUserProfileUpdates()] SNAPSHOT ERROR ${exception.message.toString()}")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                // A user profile exists in firestore, update the view model and also set userProfileExists to true so that
                // MainFragment does not have to upload an initial data.
                viewModel.userName.value = snapshot.get("name").toString()
                viewModel.userProfileExists.value = true
            } else {
                // A user profile does not exists in firestore, set userProfileExists to false so that MainFragment upload
                // initial data.
                Log.w("devlog", "[subscribeUserProfileUpdates()] USER PROFILE DOES NOT EXISTS IN FIRESTORE" )
                viewModel.userProfileExists.value = false
            }
        }
    }

    private fun setDefaultUserProfileValue() {
        // This set the initial value for user profile
        viewModel.userEmail.value = currentUser?.email

        if (currentUser?.displayName.isNullOrBlank()) {
            val createName = currentUser?.email.toString().split("@")
            viewModel.userName.value = createName.first()
        } else {
            viewModel.userName.value = currentUser?.displayName
        }
    }

    private fun subscribeToRealtimeUpdates() {
        val generatedUserData = Firebase.firestore.collection("generatedUserData")
        val userUid = generatedUserData.document(currentUser!!.uid)
        val data = userUid.collection("data")
        val query = data.orderBy("createdAt")

        // Listen to any update from the database and determine if the update is about
        // appending new data, modifying existing data or deleting existing data.
        query.addSnapshotListener{ snapshot, exception ->
            if (exception?.message != null) {
                Log.w("devlog", "[subscribeToRealtimeUpdates()] SNAPSHOT ERROR ${exception.message.toString()}")
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
        Log.i("devlog", "--------------- NEW DATABASE UPDATE ---------------")
        val dataModel: SeedSchema = viewModel.convertToModel(changes)
        val appName: String = dataModel.appName
        Log.i("devlog", "[handleNewData()] New data from database -> $appName")

        // Append data in the view model, returns a list, the first item is the index
        // position of added data, the last item is the index position of the added
        // data in favorites.
        val addedPos: List<Int> = viewModel.addData(dataModel)
        val seedIndex: Int = addedPos.first()
        val favoriteIndex: Int = addedPos.last()

        // addedPos might return -1, this means that there is no need to notify
        // the adapter about the change.

        Log.i("devlog",
            "[handleNewData()] Notifying seed adapter about $appName inserted at index position $seedIndex"
        )
        seedAdapter.notifyItemInserted(addedPos.first())

        if (addedPos.last() != -1) {
            Log.i("devlog",
                "[handleNewData()] Notifying favorite adapter about $appName inserted at index position $favoriteIndex"
            )
            favoriteAdapter.notifyItemInserted(addedPos.last())
        }
        Log.i("devlog", "--------------- END OF DATABASE UPDATE ---------------")
    }

    private fun handleModifiedData(changes: DocumentChange) {
        Log.i("devlog", "--------------- NEW DATABASE UPDATE ---------------")
        val dataModel: SeedSchema = viewModel.convertToModel(changes)
        val appName: String = dataModel.appName
        Log.i("devlog", "[handleModifiedData()] New modified data from database -> $appName")

        // Modify the data in the view model, returns a list, the first item is the index position
        // of modified data in home the last item is the index position of the modified
        // data in favorites.
        val modifiedPos: List<Int> = viewModel.modifyData(dataModel)
        val seedIndex: Int = modifiedPos.first()
        val favoriteIndex: Int = modifiedPos.last()

        // modifiedPos might return -1, this means that there is no need to notify
        // the adapter about the change.

        if (modifiedPos.first() != -1) {
            Log.i("devlog",
                "[handleModifiedData()] Notifying seedAdapter about $appName inserted at $seedIndex"
            )
            seedAdapter.notifyItemChanged(modifiedPos.first())
        }

        if (modifiedPos.last() != -1) {
            Log.i("devlog",
                "[handleModifiedData()] Notifying favoriteAdapter about $appName inserted at $favoriteIndex"
            )
            favoriteAdapter.notifyItemRemoved(modifiedPos.last())
        }
        Log.i("devlog", "--------------- END OF DATABASE UPDATE ---------------")
    }

    private fun handleRemovedData(changes: DocumentChange) {
        Log.i("devlog", "--------------- NEW DATABASE UPDATE ---------------")
        val dataModel: SeedSchema = viewModel.convertToModel(changes)
        val appName: String = dataModel.appName

        // Delete the data in the view model, returns a list, the first item is the index position
        // of deleted data in home, the last item is the index position of the deleted
        // data in favorites.
        val deletedPos: List<Int> = viewModel.deleteData(dataModel)
        val seedIndex: Int = deletedPos.first()
        val favoriteIndex: Int = deletedPos.last()

        // deletedPos might return -1, this means that there is no need to notify
        // the adapter about the change.

        if (deletedPos.first() != -1) {
            Log.i("devlog",
                "[handleRemovedData()] Notifying seedAdapter about $appName deleted at $seedIndex"
            )
            seedAdapter.notifyItemRemoved(deletedPos.first())
        }

        if (deletedPos.last() != -1) {
            Log.i("devlog",
                "[handleRemovedData()] Notifying favoriteAdapter about $appName deleted at $favoriteIndex"
            )
            favoriteAdapter.notifyItemRemoved(deletedPos.last())
        }
        Log.i("devlog", "--------------- END OF DATABASE UPDATE ---------------")
    }
}