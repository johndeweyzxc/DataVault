package com.example.datavault

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModelProvider
import com.example.datavault.adapters.SeedAdapter
import com.example.datavault.fragments.MainFragment
import com.example.datavault.schema.DataModelConverter
import com.example.datavault.schema.SeedSchema
import com.example.datavault.models.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var firebase: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    private lateinit var viewModel: HomeViewModel

    private lateinit var seedAdapter: SeedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        val clipBoardManager: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        seedAdapter = SeedAdapter(
            viewModel.listDataModel, viewModel.mapDataModel,
            supportFragmentManager, R.id.frameLayoutActivityMain,
            clipBoardManager
        )

        setContentView(R.layout.activity_main)

        supportFragmentManager.fragmentFactory = HomeFragmentFactory(seedAdapter)

        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        setFirebaseAuth()
        setFirebaseAuthListeners()
        subscribeToRealtimeUpdates(viewModel)
        setFirstFragment()
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

    private fun setFirebaseAuth() {
        firebase = FirebaseAuth.getInstance()
        currentUser = firebase.currentUser!!
    }

    private fun setFirebaseAuthListeners() {
        firebase.addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user == null) {
                val intent = Intent(this, AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    private fun setFirstFragment() {
        supportFragmentManager.beginTransaction().apply {
            // Replace the frame layout with main fragment in the main activity.
            replace(R.id.frameLayoutActivityMain, MainFragment(seedAdapter))
            commit()
        }
    }

    private fun subscribeToRealtimeUpdates(viewModel: HomeViewModel) {
        val dataRef: CollectionReference = Firebase.firestore.collection("generatedUserData")
            .document(currentUser!!.uid)
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
}