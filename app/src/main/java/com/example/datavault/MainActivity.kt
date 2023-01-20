package com.example.datavault

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.datavault.adapters.SeedAdapter
import com.example.datavault.models.MainViewModel
import com.example.datavault.schema.DataModelConverter
import com.example.datavault.schema.SeedSchema
import com.example.datavault.views.MainFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var seedAdapter: SeedAdapter
    private var currentUser: FirebaseUser? = null

    inner class MainFragmentInterface() {
        fun getUserName(): String {
            this@MainActivity.nullUserUpdateUi(currentUser)
            return viewModel.getUserName(currentUser!!)
        }

        fun getUserEmail(): String {
            this@MainActivity.nullUserUpdateUi(currentUser)
            return viewModel.getUserEmail(currentUser!!)
        }

        fun getUserPhoto(): String {
            this@MainActivity.nullUserUpdateUi(currentUser)
            return viewModel.getUserphotoUrl(currentUser!!)
        }

        fun getAdapter(): SeedAdapter {
            return seedAdapter
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        val clipBoardManager: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        seedAdapter = SeedAdapter(viewModel.getListData(), viewModel.getMapData(), supportFragmentManager, clipBoardManager)

        setContentView(R.layout.activity_main)
        super.onCreate(savedInstanceState)

        currentUser = FirebaseAuth.getInstance().currentUser
        nullUserUpdateUi(currentUser)
        authListener()
        subscribeToRealtimeUpdates()

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        supportFragmentManager.beginTransaction().apply {
            // Replace the frame layout with main fragment in the main activity.
            replace(R.id.frameLayoutActivityMain, MainFragment())
            commit()
        }
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (supportFragmentManager.backStackEntryCount == 0) {
                moveTaskToBack(true)
            } else {
                supportFragmentManager.popBackStack()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.i("DEV.LOG.INFO", "Screen orientation changed to landscape in MainActivity")
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.i("DEV.LOG.INFO", "Screen orientation changed to portrait in MainActivity")
        }
        super.onConfigurationChanged(newConfig)
    }

    private fun nullUserUpdateUi(user: FirebaseUser?) {
        if (user == null) {
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun authListener() {
        FirebaseAuth.getInstance().addAuthStateListener { auth -> nullUserUpdateUi(auth.currentUser) }
    }

    private fun subscribeToRealtimeUpdates() {
        nullUserUpdateUi(currentUser)
        val generatedUserData = Firebase.firestore.collection("generatedUserData")
        val userUid = generatedUserData.document(currentUser!!.uid)
        val data = userUid.collection("data")

        val query = data.orderBy("createdAt")

        query.addSnapshotListener{ snapshot, exception ->
            exception?.let {
                return@addSnapshotListener
            }
            for (changes in snapshot?.documentChanges!!) {
                when (changes.type) {
                    DocumentChange.Type.ADDED -> handleNewData(changes)
                    DocumentChange.Type.MODIFIED -> handleModifiedData(changes)
                    DocumentChange.Type.REMOVED -> handleRemovedData(changes)
                }
            }
        }
    }

    private fun handleNewData(changes: DocumentChange) {
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

    private fun handleModifiedData(changes: DocumentChange) {
        // Convert DocumentChange! to DataModel
        val dataModel: SeedSchema = DataModelConverter().convertToModel(changes)
        // Modify the data in the view model
        val modifiedPos: Int = viewModel.modifyData(dataModel, dataModel.docId)
        if (modifiedPos != -1) {
            seedAdapter.notifyItemChanged(modifiedPos)
            Log.i("DEV.LOG.INFO", "Document [${dataModel.appName}] has been modified")
        }
    }

    private fun handleRemovedData(changes: DocumentChange) {
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