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
import com.example.datavault.databinding.FragmentDialogCreateBinding
import com.example.datavault.databinding.FragmentDialogEditBinding
import com.example.datavault.models.MainViewModel
import com.example.datavault.schema.SeedSchema
import com.example.datavault.schema.SeedSchemaUpload
import com.example.datavault.views.MainFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        favoriteAdapter = RvFavorite(viewModel.getListFavorites(), this)
        seedAdapter = RvHome(viewModel.getListSeed(), this)

        subscribeToRealtimeUpdates()

        // Set the MainFragment as the first fragment to appear on the activity
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frameLayoutActivityMain, MainFragment())
            commit()
        }
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        // If the user presses back button, pop the fragment stack or pause the activity if there is only one fragment.
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

    private fun userMightBeNull(user: FirebaseUser?) {
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

        // Listen to any update from the database and determine if the update is about appending new data, modifying existing
        // data or deleting existing data.
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
        val dataModel: SeedSchema = viewModel.convertToModel(changes)
        // Append data in the view model, returns a list, the first item is the index position of added data,
        // the last item is the index position of the added data in favorites.
        val addedPos: List<Int> = viewModel.addData(dataModel)

        // Notify the home adapter about the new added data
        seedAdapter.notifyItemInserted(addedPos.first())
        Log.i("devlog", "Added new document [${dataModel.appName}] from firestore snapshot")

        // If the data is also marked as favorite by the user then notify the favorite adapter about the new data to update
        // the recycler view.
        if (addedPos.last() != -1) {
            favoriteAdapter.notifyItemInserted(addedPos.last())
            Log.i("devlog", "Added [${dataModel.appName}] to favorites")
        }
    }

    private fun handleModifiedData(changes: DocumentChange) {
        // Convert DocumentChange to DataModel
        val dataModel: SeedSchema = viewModel.convertToModel(changes)
        // Modify the data in the view model, returns a list, the first item is the index position of modified data in home
        // the last item is the index position of the modified data in favorites.
        val modifiedPos: List<Int> = viewModel.modifyData(dataModel)

        // modifiedPos will return -1 if the data is not in the view model
        if (modifiedPos.first() != -1) {
            seedAdapter.notifyItemChanged(modifiedPos.first())
            Log.i("devlog", "Document [${dataModel.appName}] has been modified")
        }

        // If the data is also marked as favorite by the user then notify the favorite adapter about the new data to
        // upate the recycler view.
        if (modifiedPos.last() != -1) {
            favoriteAdapter.notifyItemChanged(modifiedPos.last())
            Log.i("devlog", "Document [${dataModel.appName}] has also been modified in favorites")
        }
    }

    private fun handleRemovedData(changes: DocumentChange) {
        // Convert DocumentChange to DataModel
        val dataModel: SeedSchema = viewModel.convertToModel(changes)
        // Delete the data in the view model, returns a list, the first item is the index position of deleted data in home,
        // the last item is the index position of the deleted data in favorites.
        val deletedPos: List<Int> = viewModel.deleteData(dataModel)

        // deletedPos will return -1 if the data is not in the view model
        if (deletedPos.first() != -1) {
            seedAdapter.notifyItemRemoved(deletedPos.first())
            Log.i("devlog", "Document [${dataModel.appName}] has been deleted")
        }

        // If the data is also marked as favorite by the user then notify the favorite adapter about the deleted data to
        // update the recycler view.
        if (deletedPos.last() != -1) {
            favoriteAdapter.notifyItemRemoved(deletedPos.last())
            Log.i("devlog", "Document [${dataModel.appName}] has also been deleted in favorites")
        }
    }

    inner class FavoriteFrag {
        fun favoriteAdapter(): RvFavorite {
            return favoriteAdapter
        }
    }

    inner class HomeFrag {
        fun homeAdapter(): RvHome {
            return seedAdapter
        }

        fun seedCount(): Int {
            return viewModel.countSeed()
        }
    }

    inner class MainFrag {
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
    }

    inner class CreateSeed {
        fun uploadData(binding: FragmentDialogCreateBinding) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            userMightBeNull(currentUser)
            val userIdDocRef = generatedUserData.document(currentUser!!.uid)
            val dataColRef = userIdDocRef.collection("data")
            val dataModel = SeedSchemaUpload(
                binding.createEtAppname.text.toString(), binding.createEtUsername.text.toString(),
                binding.createEtEmail.text.toString(), binding.createEtPassword.text.toString(),
                binding.createEtPhonenumber.text.toString(), false,
                Timestamp(Date()), Timestamp(Date())
            )

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
    }

    inner class EditSeed {
        fun getSeedViaFirestoreDocId(fireStoreDocId: String): SeedSchema {
            return viewModel.getSeed(fireStoreDocId)
        }

        fun updateData(fireStoreDocId: String, binding: FragmentDialogEditBinding) {
            val seedData = getSeedViaFirestoreDocId(fireStoreDocId)
            val currentUser = FirebaseAuth.getInstance().currentUser
            userMightBeNull(currentUser)
            val userIdDocRef = generatedUserData.document(currentUser!!.uid)
            val dataColRef = userIdDocRef.collection("data")
            val targetDocument = dataColRef.document(fireStoreDocId)
            val dataModel = SeedSchemaUpload(
                binding.editEtAppname.text.toString(), binding.editEtUsername.text.toString(),
                binding.editEtEmail.text.toString(), binding.editEtPassword.text.toString(),
                binding.editEtPhonenumber.text.toString(), seedData.favorite,
                seedData.createdAt, Timestamp(Date())
            )

            val task = targetDocument.set(dataModel)
            task.addOnSuccessListener {
                Toast.makeText(applicationContext, "Successfully updated the data", Toast.LENGTH_SHORT).show()
            }.addOnCanceledListener {
                Toast.makeText(applicationContext, "Canceled updating the data", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { exception ->
                if (exception.message != null) {
                    Log.i("devlog", exception.message!!)
                }
                Toast.makeText(applicationContext, "Failed to update the data", Toast.LENGTH_SHORT).show()
            }
        }

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

        fun addToFavorites(context: Context, fireStoreDocId: String, value: Boolean) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            userMightBeNull(currentUser)
            val userId = currentUser?.uid

            val userIdDocRef = generatedUserData.document(userId!!)
            val dataColRef = userIdDocRef.collection("data")
            val targetDocument = dataColRef.document(fireStoreDocId)

            targetDocument.update("favorite", value).addOnSuccessListener {
                Toast.makeText(context, "Added to favorites", Toast.LENGTH_LONG).show()
            }.addOnCanceledListener {
                Toast.makeText(context, "Canceled saving to favorite", Toast.LENGTH_LONG).show()
            }.addOnFailureListener { exception ->
                if (exception.message != null) {
                    Log.i("devlog", exception.message!!)
                }
                Toast.makeText(context, "Failed to add to favorites", Toast.LENGTH_LONG).show()
            }
        }
    }
}