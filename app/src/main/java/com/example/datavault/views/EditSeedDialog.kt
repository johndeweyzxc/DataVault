package com.example.datavault.views

import android.content.Context
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.get
import androidx.fragment.app.DialogFragment
import com.example.datavault.MainActivity
import com.example.datavault.R
import com.example.datavault.databinding.FragmentDialogEditBinding
import com.example.datavault.schema.SeedSchema
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class EditSeedDialog(
    private val itemView: View,
    private val currentData: SeedSchema
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
        val seedData = (activity as MainActivity).EditSeed().getSeedViaFirestoreDocId(currentData.fireStoreDocId)

        etAppName.setText(seedData.appName)
        etUserName.setText(seedData.userName)
        etEmail.setText(seedData.email)
        etPassword.setText(seedData.password)
        etPhoneNumber.setText(seedData.phoneNumber)
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
                if (checkForBlankOrNull() == -1) {
                    return@setOnClickListener
                }
                (activity as MainActivity).EditSeed().updateData(currentData.fireStoreDocId, binding)
                closeActiveKeyboard()
                dismiss()
            }
        }

        if (currentData.favorite) {
            binding.editToolBar.menu[0].setIcon(R.drawable.ic_edit_seed_dialog_fav)
        } else {
            binding.editToolBar.menu[0].setIcon(R.drawable.ic_edit_seed_dialog_not_fav)
        }

        binding.editToolBar.setOnMenuItemClickListener { listener ->
            when (listener.title) {
                "Favorite" -> {
                    if (currentData.favorite) {
                        (activity as MainActivity).EditSeed().addToFavorites(
                            requireActivity(), currentData.fireStoreDocId, false
                        )
                        Log.i("devlog", "Removed from favorite")
                    } else {
                        (activity as MainActivity).EditSeed().addToFavorites(
                            requireActivity(), currentData.fireStoreDocId, true
                        )
                        Log.i("devlog", "Added to favorite")
                    }
                    dismiss()
                    true
                }
                "Delete" -> {
                    (activity as MainActivity).EditSeed().deleteData(
                        itemView.context, currentData.fireStoreDocId, currentData.appName
                    )
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

    private fun closeActiveKeyboard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
    }
}