package com.example.datavault

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.datavault.adapters.RvFavorite
import com.example.datavault.adapters.RvHome
import com.example.datavault.models.MainViewModel
import com.example.datavault.schema.SeedSchema
import com.example.datavault.schema.SeedSchemaUpload
import com.example.datavault.views.MainFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var seedAdapter: RvHome
    private lateinit var favoriteAdapter: RvFavorite
    private var currentUser: FirebaseUser? = null

    private val generatedUserData = Firebase.firestore.collection("generatedUserData")

    override fun onCreate(savedInstanceState: Bundle?) {
        currentUser = FirebaseAuth.getInstance().currentUser
        authListener()

        setContentView(R.layout.activity_main)
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        // Clipboard manager to be use by the chips inside the data container layout
        seedAdapter = RvHome(viewModel.getListSeed(), this)
        favoriteAdapter = RvFavorite(viewModel.getListFavorites())
        subscribeToRealtimeUpdates()
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        // Set the MainFragment as the first fragment to appear on the activity
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frameLayoutActivityMain, MainFragment())
            commit()
        }
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        // If the user presses back button, pop the fragment stack or pause the activity if there is only one fragment
        override fun handleOnBackPressed() {
            if (supportFragmentManager.backStackEntryCount == 0) {
                moveTaskToBack(true)
            } else {
                supportFragmentManager.popBackStack()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        // This handles the configuration change of the screen to prevent restart of MainActivity
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

    private fun subscribeToRealtimeUpdates() {
        // Subscribe to realtime updates from the database
        userMightBeNull(currentUser)
        val generatedUserData = Firebase.firestore.collection("generatedUserData")
        val userUid = generatedUserData.document(currentUser!!.uid)
        val data = userUid.collection("data")
        val query = data.orderBy("createdAt")

        // Listen to any update from the database and determine if the update is about appending new data, modifying existing
        // data or deleting the data.
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
        // Convert DocumentChange to DataModel
        val dataModel: SeedSchema = convertToModel(changes)
        // Append the data in the view model
        val addedPos: List<Int> = viewModel.addData(dataModel)
        if (addedPos.first() != -1) {
            // Notify the adapter about the new added data from the view model
            seedAdapter.notifyItemInserted(addedPos.first())
            Log.i("devlog", "Added new document [${dataModel.appName}] from firestore snapshot")
        }
        if (addedPos.last() != -1) {
            // Notify the adapter about the new added data from the view model
            favoriteAdapter.notifyItemInserted(addedPos.last())
            Log.i("devlog", "Added [${dataModel.appName}] to favorites")
        }
    }

    private fun handleModifiedData(changes: DocumentChange) {
        // Convert DocumentChange to DataModel
        val dataModel: SeedSchema = convertToModel(changes)
        // Modify the data in the view model
        val modifiedPos: List<Int> = viewModel.modifyData(dataModel)

        Log.i("devlog" ,
            "seedAdapter modified pos: ${modifiedPos.first()}, favoriteAdapter modified pos: ${modifiedPos.last()}")

        if (modifiedPos.first() != -1) {
            // Notify the adapter about the modified data from the view model
            seedAdapter.notifyItemChanged(modifiedPos.first())
            Log.i("devlog", "Document [${dataModel.appName}] has been modified")
        }
        if (modifiedPos.last() != -1) {
            // Notify the adapter about the modified data from the view model
            favoriteAdapter.notifyItemChanged(modifiedPos.last())
            Log.i("devlog", "Document [${dataModel.appName}] has also been modified in favorites")
        }
    }

    private fun handleRemovedData(changes: DocumentChange) {
        // Convert DocumentChange to DataModel
        val dataModel: SeedSchema = convertToModel(changes)
        // Remove the data in the view model
        val deletedPos: List<Int> = viewModel.deleteData(dataModel)

        Log.i("devlog" ,
            "seedAdapter modified pos: ${deletedPos.first()}, favoriteAdapter modified pos: ${deletedPos.last()}")

        if (deletedPos.first() != -1) {
            // Notify the adapter about the removed data from the view model
            seedAdapter.notifyItemRemoved(deletedPos.first())
            Log.i("devlog", "Total seed count: ${seedCount()}")
            Log.i("devlog", "Document [${dataModel.appName}] has been deleted")
        }
        if (deletedPos.last() != -1) {
            // Notify the adapter about the removed data from the view model
            favoriteAdapter.notifyItemRemoved(deletedPos.first())
            Log.i("devlog", "Document [${dataModel.appName}] has also been deleted in favorites")
        }
    }

    private fun convertToModel(changes: DocumentChange): SeedSchema {
        return SeedSchema(
            changes.document.get("appName") as String,
            changes.document.get("userName") as String,
            changes.document.get("email") as String,
            changes.document.get("password") as String,
            changes.document.get("phoneNumber") as String,
            changes.document.get("favorite") as Boolean,
            changes.document.id,
            changes.document.get("createdAt") as Timestamp?,
            changes.document.get("updatedAt") as Timestamp?,
        )
    }

    // Use by EditSeedDialog
    suspend fun fetchData(
        firestoreDocId: String, appName: TextInputEditText, userName: TextInputEditText,
        email: TextInputEditText, password: TextInputEditText, pNumber: TextInputEditText,
    ): MutableList<Any> {

        val currentUser = FirebaseAuth.getInstance().currentUser
        userMightBeNull(currentUser)
        val userIdDocRef = generatedUserData.document(currentUser!!.uid)
        val dataColRef = userIdDocRef.collection("data")
        val targetDocument = dataColRef.document(firestoreDocId)

        val list = mutableListOf<Any>()
        var result: Map<String, Any>? = null

        withContext(Dispatchers.IO) {
            try {
                result = targetDocument.get().await().data
            } catch (error: Exception) {
                if (error.message != null) {
                    Log.i("devlog", error.message!!)
                }
                runOnUiThread {
                    Toast.makeText(applicationContext, "There is a problem fetching data from the database",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        appName.setText(result?.get("appName").toString())
        userName.setText(result?.get("userName").toString())
        email.setText(result?.get("email").toString())
        password.setText(result?.get("password").toString())
        pNumber.setText(result?.get("phoneNumber").toString())

        list.add(result?.get("favorite") as Boolean)
        list.add(result?.get("createdAt")!!)
        list.add(Timestamp(Date()))

        return list
    }

    // Use by EditSeedDialog
    fun updateData(
        firestoreDocId: String, appName: String, userName: String, email: String,
        password: String, phoneNumber: String, favorite: Boolean,
        createdAt: Timestamp, updatedAt: Timestamp
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        userMightBeNull(currentUser)
        val userIdDocRef = generatedUserData.document(currentUser!!.uid)
        val dataColRef = userIdDocRef.collection("data")
        val targetDocument = dataColRef.document(firestoreDocId)
        val dataModel = SeedSchemaUpload(
            appName, userName, email, password, phoneNumber, favorite, createdAt, updatedAt
        )

        val task = targetDocument.set(dataModel)
        // Display status of the task, like is it successful or failed
        task.addOnSuccessListener {
            Toast.makeText(applicationContext, "Successfully deleted the data", Toast.LENGTH_SHORT).show()
        }.addOnCanceledListener {
            Toast.makeText(applicationContext, "Canceled deleting data", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            if (exception.message != null) {
                Log.i("devlog", exception.message!!)
            }
            Toast.makeText(applicationContext, "Failed to delete data", Toast.LENGTH_SHORT).show()
        }
    }

    // Use by EditSeedDialog
    fun deleteData(context: Context, fireStoreDocId: String, appName: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        userMightBeNull(currentUser)
        val userId = currentUser?.uid

        val userIdDocRef = generatedUserData.document(userId!!)
        val dataColRef = userIdDocRef.collection("data")
        val targetDocument = dataColRef.document(fireStoreDocId)

        MaterialAlertDialogBuilder(context)
            .setTitle("Deleting $appName")
            .setMessage("Do you really want to delete $appName?")
            .setNeutralButton("CANCEL") { dialog, _ ->
                dialog.cancel()
                return@setNeutralButton
            }
            .setPositiveButton("DELETE") {_, _ ->
                val task = targetDocument.delete()
                // Display status of the task, like is it successful or failed
                task.addOnSuccessListener {
                    Toast.makeText(context, "Successfully deleted the data", Toast.LENGTH_SHORT).show()
                }.addOnCanceledListener {
                    Toast.makeText(context, "Canceled deleting data", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { exception ->
                    if (exception.message != null) {
                        Log.i("devlog", exception.message!!)
                    }
                    Toast.makeText(context, "Failed to delete data", Toast.LENGTH_SHORT).show()
                }
                supportFragmentManager.popBackStack()
                return@setPositiveButton
            }.show()
    }

    // Use by EditSeedDialog
    fun addToFavorites(fireStoreDocId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        userMightBeNull(currentUser)
        val userId = currentUser?.uid

        val userIdDocRef = generatedUserData.document(userId!!)
        val dataColRef = userIdDocRef.collection("data")
        val targetDocument = dataColRef.document(fireStoreDocId)

        targetDocument.update("favorite", true).addOnSuccessListener {
            Toast.makeText(applicationContext, "Added to favorites", Toast.LENGTH_LONG).show()
        }.addOnCanceledListener {
            Toast.makeText(applicationContext, "Canceled saving to favorite", Toast.LENGTH_LONG).show()
        }.addOnFailureListener { exception ->
            if (exception.message != null) {
                Log.i("devlog", exception.message!!)
            }
            Toast.makeText(applicationContext, "Failed to add to favorites", Toast.LENGTH_LONG).show()
        }
    }

    // Use by CreateSeedDialog
    fun uploadData(
        appName: String, userName: String, email: String, password: String,
        phoneNumber: String, createdAt: Timestamp, updatedAt: Timestamp
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        userMightBeNull(currentUser)
        val userIdDocRef = generatedUserData.document(currentUser!!.uid)
        val dataColRef = userIdDocRef.collection("data")
        val dataModel = SeedSchemaUpload(
            appName, userName, email, password, phoneNumber, false, createdAt, updatedAt
        )

        // Display status of the task, like is it successful or failed
        dataColRef.add(dataModel).addOnSuccessListener {
            Toast.makeText(applicationContext, "Successfully saved the data.", Toast.LENGTH_LONG).show()
        }.addOnCanceledListener {
            Toast.makeText(applicationContext, "Canceled saving data", Toast.LENGTH_LONG).show()
        }.addOnFailureListener { exception ->
            if (exception.message != null) {
                Log.i("devlog", exception.message!!)
            }
            Toast.makeText(applicationContext, "Failed to save data", Toast.LENGTH_LONG).show()
        }
    }

    private fun userMightBeNull(user: FirebaseUser?) {
        if (user == null) {
            Log.i("devlog", "User is null, navigating to AuthActivity")
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    // Functions below this comment is use by MainFragment
    fun currentUserEmail(): String {
        userMightBeNull(currentUser)
        return currentUser?.email!!
    }

    fun currentUserName(): String {
        userMightBeNull(currentUser)
        if (currentUser?.displayName.isNullOrBlank()) {
            val createName = currentUser?.email.toString().split("@")
            return createName.first()
        }
        return currentUser?.displayName!!
    }

    fun currentUserUrlPhoto(): String {
        userMightBeNull(currentUser)
        if (currentUser?.photoUrl == null) {
            return getString(R.string.default_user_photo)
        }
        return currentUser?.photoUrl.toString()
    }

    // Functions below this comment is use by HomeFragment
    fun homeAdapter(): RvHome {
        return seedAdapter
    }

    fun seedCount(): Int {
        return viewModel.countSeed()
    }

    // Functions below this comment is use by FavoritesFragment
    fun favoriteAdapter(): RvFavorite {
        return favoriteAdapter
    }
}