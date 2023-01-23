package com.example.datavault

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.datavault.adapters.RvHome
import com.example.datavault.models.Main
import com.example.datavault.models.SeedSchema
import com.example.datavault.models.SeedSchemaUpload
import com.example.datavault.views.MainFragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), MainInterface {

    private lateinit var viewModel: Main
    private lateinit var seedAdapter: RvHome
    private var currentUser: FirebaseUser? = null

    private val generatedUserData = Firebase.firestore.collection("generatedUserData")

    override fun onCreate(savedInstanceState: Bundle?) {
        currentUser = FirebaseAuth.getInstance().currentUser
        authListener()

        setContentView(R.layout.activity_main)
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[Main::class.java]
        // Clipboard manager to be use by the chips inside the data container layout
        val clipBoardManager: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        seedAdapter = RvHome(viewModel.getListData(), viewModel.getMapData(), supportFragmentManager, clipBoardManager)

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
        val addedPos: Int = viewModel.addData(dataModel, dataModel.docId)
        if (addedPos != -1) {
            // Notify the adapter about the new added data from the view model
            seedAdapter.notifyItemInserted(addedPos)
            Log.i("devlog", "Added new document [${dataModel.appName}] from firestore snapshot")
        }
    }

    private fun handleModifiedData(changes: DocumentChange) {
        // Convert DocumentChange to DataModel
        val dataModel: SeedSchema = convertToModel(changes)
        // Modify the data in the view model
        val modifiedPos: Int = viewModel.modifyData(dataModel, dataModel.docId)
        if (modifiedPos != -1) {
            // Notify the adapter about the modified data from the view model
            seedAdapter.notifyItemChanged(modifiedPos)
            Log.i("devlog", "Document [${dataModel.appName}] has been modified")
        }
    }

    private fun handleRemovedData(changes: DocumentChange) {
        // Convert DocumentChange to DataModel
        val dataModel: SeedSchema = convertToModel(changes)
        // Remove the data in the view model
        val deletedPos: Int = viewModel.deleteData(dataModel.docId)
        if (deletedPos != -1) {
            // Notify the adapter about the removed data from the view model
            seedAdapter.notifyItemRemoved(deletedPos)
            Log.i("devlog", "Document [${dataModel.appName}] has been deleted")
        }
    }

    private fun convertToModel(changes: DocumentChange): SeedSchema {
        return SeedSchema(
            changes.document.get("appName") as String,
            changes.document.get("userName") as String,
            changes.document.get("email") as String,
            changes.document.get("password") as String,
            changes.document.get("phoneNumber") as String,
            changes.document.get("docId") as String,
            changes.document.id,
            changes.document.get("createdAt") as Timestamp?,
            changes.document.get("updatedAt") as Timestamp?,
        )
    }

    // MainInterface
    override fun updateData(userId: String, firestoreDocId: String, dataModel: SeedSchemaUpload) {
        val userIdDocRef = generatedUserData.document(userId)
        val dataColRef = userIdDocRef.collection("data")
        val targetDocument = dataColRef.document(firestoreDocId)

        targetDocument.set(dataModel).addOnSuccessListener {
            Toast.makeText(applicationContext, "Successfully saved changes", Toast.LENGTH_SHORT).show()
        }.addOnCanceledListener {
            Toast.makeText(applicationContext, "Canceled to save changes", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            if (exception.message != null) {
                Log.i("devlog", exception.message!!)
            }
            Toast.makeText(applicationContext, "Failed to save changes", Toast.LENGTH_SHORT).show()
        }
    }

    override fun uploadData(userId: String, dataModel: SeedSchemaUpload) {
        val userIdDocRef = generatedUserData.document(currentUser!!.uid)
        val dataColRef = userIdDocRef.collection("data")

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

    override fun userMightBeNull(user: FirebaseUser?) {
        if (user == null) {
            Log.i("devlog", "User is null, navigating to AuthActivity")
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun currentUserEmail(): String {
        userMightBeNull(currentUser)
        return viewModel.getUserEmail(currentUser!!)
    }

    override fun currentUserName(): String {
        userMightBeNull(currentUser)
        return viewModel.getUserName(currentUser!!)
    }

    override fun currentUserUrlPhoto(): String {
        userMightBeNull(currentUser)
        return viewModel.getUserphotoUrl(currentUser!!, getString(R.string.default_user_photo))
    }

    override fun homeAdapter(): RvHome {
        return seedAdapter
    }

    override fun adapterItemCount(): Int {
        return viewModel.countItem()
    }
}