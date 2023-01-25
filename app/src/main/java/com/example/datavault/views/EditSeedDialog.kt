package com.example.datavault.views

import android.content.Context
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import com.example.datavault.MainActivity
import com.example.datavault.R
import com.example.datavault.databinding.FragmentDialogEditBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditSeedDialog(
    private val itemView: View, private val fireStoreDocId: String, private val appName: String
    ) : DialogFragment() {

    private lateinit var binding: FragmentDialogEditBinding

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

    private lateinit var favorite: String
    private lateinit var createdAt: Timestamp
    private lateinit var updatedAt: Timestamp

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
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            val result: List<Any> = (activity as MainActivity).fetchData(
                fireStoreDocId, etAppName, etUserName,
                etEmail, etPassword, etPhoneNumber
            )

            favorite = if (result[0] as Boolean) {
                "Added"
            } else {
                "notAdded"
            }
            createdAt = result[1] as Timestamp
            updatedAt = result[2] as Timestamp
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
                "Favorite" -> {
                    (activity as MainActivity).addToFavorites(fireStoreDocId)
                    true
                }
                "Delete" -> {
                    (activity as MainActivity).deleteData(itemView.context, fireStoreDocId, appName)
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

        val isAdded: Boolean = favorite == "Added"
        if (isAdded) {
            Log.i("devlog", "This seed is marked as favorite in edit seed dialog")
        } else {
            Log.i("devlog", "This seed is not marked as favorite in edit seed dialog")
        }

        (activity as MainActivity).updateData(
            fireStoreDocId, etAppName.text.toString(), etUserName.text.toString(), etEmail.text.toString(),
            etPassword.text.toString(), etPhoneNumber.text.toString(), isAdded, createdAt, updatedAt
        )
    }

    private fun closeActiveKeyboard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
    }
}