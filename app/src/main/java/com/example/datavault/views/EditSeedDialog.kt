package com.example.datavault.views

import android.content.Context
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.datavault.MainActivity
import com.example.datavault.R
import com.example.datavault.databinding.FragmentDialogEditBinding
import com.example.datavault.models.SeedSchemaUpload
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class EditSeedDialog(private val firestoreDocId: String) : DialogFragment() {

    private lateinit var binding: FragmentDialogEditBinding
    private lateinit var userId: String

    private lateinit var ilAppName: TextInputLayout
    private lateinit var ilUserName: TextInputLayout
    private lateinit var ilEmail: TextInputLayout
    private lateinit var ilPassword: TextInputLayout
    private lateinit var ilPhoneNumber: TextInputLayout

    private lateinit var etAppName: TextInputEditText
    private lateinit var etUserName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etPhoneNumber: TextInputEditText

    private lateinit var createdAt: Timestamp
    private lateinit var updatedAt: Timestamp
    private lateinit var docId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        // When this fragment enter, slide to the left
        enterTransition = inflater.inflateTransition(R.transition.fragment_slide_right)
        // When this fragment exit, slide to the right
        exitTransition = inflater.inflateTransition(R.transition.fragment_slide_right)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDialogEditBinding.inflate(layoutInflater)
        val currentUser = FirebaseAuth.getInstance().currentUser

        (activity as MainActivity).userMightBeNull(currentUser)
        userId = currentUser!!.uid
        return binding.root
    }

    override fun onStart() {
        referenceEditTextsAndInputLayouts()
        setDefaultValues()
        setButtonListeners()
        super.onStart()
    }

    private fun referenceEditTextsAndInputLayouts() {
        ilAppName = binding.editIlAppname
        ilUserName = binding.editIlUsername
        ilEmail = binding.editIlEmail
        ilPassword = binding.editIlPassword
        ilPhoneNumber = binding.editIlPhonenumber

        etAppName = binding.editEtAppname
        etUserName = binding.editEtUsername
        etEmail = binding.editEtEmail
        etPhoneNumber = binding.editEtPhonenumber
        etPassword = binding.editEtPassword
    }

    private fun setDefaultValues() {
        val generatedUserData = Firebase.firestore.collection("generatedUserData")
        val userId = generatedUserData.document(userId)
        val data = userId.collection("data")
        val targetDocument = data.document(firestoreDocId)

        targetDocument.get().addOnSuccessListener {
            etAppName.setText(it.data?.get("appName").toString())
            etUserName.setText(it.data?.get("userName").toString())
            etEmail.setText(it.data?.get("email").toString())
            etPassword.setText(it.data?.get("password").toString())
            etPhoneNumber.setText(it.data?.get("phoneNumber").toString())
            docId = it.data?.get("docId").toString()
            createdAt = it.data?.get("createdAt") as Timestamp
            updatedAt = Timestamp(Date())
        }
    }

    private fun setButtonListeners() {
        ilAppName.setEndIconOnClickListener { etAppName.text?.clear() }
        ilUserName.setEndIconOnClickListener { etUserName.text?.clear() }
        ilEmail.setEndIconOnClickListener { etEmail.text?.clear() }
        ilPhoneNumber.setEndIconOnClickListener { etPhoneNumber.text?.clear() }

        binding.editToolBar.setOnClickListener { dismiss() }
        binding.editBackButton.setOnClickListener { dismiss() }

        binding.editSaveChangesButton.setOnClickListener {
            if (checkForBlankOrNull() == 0) {
                updateData()
                closeActiveKeyboard()
            }
        }

        binding.editToolBar.setOnMenuItemClickListener { listener ->
            when (listener.title) {
                "Save" -> {
                    if (checkForBlankOrNull() == 0) {
                        closeActiveKeyboard()
                        updateData()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun checkForBlankOrNull(): Int {
        when {
            etAppName.text.isNullOrBlank() -> {ilAppName.helperText = "*App name is required"; return -1 }
            etUserName.text.isNullOrBlank() -> {ilUserName.helperText = "*User name is required"; return -1}
            etEmail.text.isNullOrBlank() -> {ilEmail.helperText = "*Email is required"; return -1}
            etPhoneNumber.text.isNullOrBlank() -> {ilPhoneNumber.helperText = "*Phone number is required"; return -1 }
            etPassword.text.isNullOrBlank() -> {ilPassword.helperText = "Password is required"; return -1}
        }

        return 0
    }

    private fun updateData() {
        if (checkForBlankOrNull() == -1) {
            return
        }

        val dataModel = SeedSchemaUpload(
            etAppName.text.toString(),
            etUserName.text.toString(),
            etEmail.text.toString(),
            etPassword.text.toString(),
            etPhoneNumber.text.toString(),
            docId,
            createdAt,
            updatedAt
        )

        val generatedUserData = Firebase.firestore.collection("generatedUserData")
        val userId = generatedUserData.document(userId)
        val data = userId.collection("data")
        val targetDocument = data.document(firestoreDocId)

        targetDocument.set(dataModel).addOnSuccessListener {
            Toast.makeText(requireActivity(), "Successfully saved changes", Toast.LENGTH_SHORT).show()
            dismiss()
        }.addOnCanceledListener {
            Toast.makeText(requireActivity(), "Failed to save changes", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            if (exception.message != null) {
                Log.i("devlog", exception.message!!)
            }
            Toast.makeText(requireActivity(), "Failed to save changes", Toast.LENGTH_SHORT).show()
        }
    }

    private fun closeActiveKeyboard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
    }
}