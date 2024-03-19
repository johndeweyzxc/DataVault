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
import com.example.datavault.Database
import com.example.datavault.MainActivity
import com.johndeweydev.datavault.R
import com.johndeweydev.datavault.databinding.FragmentDialogEditBinding
import com.example.datavault.schema.SeedSchema
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EditSeedDialog : DialogFragment(), Database {

    private lateinit var binding: FragmentDialogEditBinding
    private lateinit var mainActivity: MainActivity
    private var currentData: SeedSchema? = null

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
        mainActivity = (activity as MainActivity)
        currentData = mainActivity.viewModel.getEditSeedCurrentData()

        // If the system needs to reclaim memory, the app process is terminated.
        // It may destroy the ViewModel along with other resources.
        if (currentData == null) {
            Log.w("devlog",
                "[onCreateView] EditSeedDialog is created but current data is null. Popping this fragment")
            dismiss()
        }
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
        val seedData = (activity as MainActivity).viewModel.getSeed(currentData!!.indexId)

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

        val scope = CoroutineScope(Dispatchers.IO)

        binding.editToolBar.setOnClickListener {
            closeActiveKeyboard()
            scope.launch {
                delay(200)
                dismiss()
            }
        }
        binding.editBackButton.setOnClickListener {
            closeActiveKeyboard()
            scope.launch {
                delay(200)
                dismiss()
            }
        }

        binding.editSaveChangesButton.setOnClickListener {
            if (checkForBlankOrNull() == 0) {
                if (checkForBlankOrNull() == -1) { return@setOnClickListener }

                if (mainActivity.currentUser == null) { mainActivity.navigateToAuthActivity() }
                updateData(requireActivity(), mainActivity.viewModel.getSeed(currentData!!.indexId), binding)
                closeActiveKeyboard()
                dismiss()
            }
        }

        if (currentData!!.favorite) {
            binding.editToolBar.menu[0].setIcon(R.drawable.ic_edit_seed_dialog_fav)
        } else {
            binding.editToolBar.menu[0].setIcon(R.drawable.ic_edit_seed_dialog_not_fav)
        }

        binding.editToolBar.setOnMenuItemClickListener { listener ->
            when (listener.title) {
                "Favorite" -> {
                    if (currentData!!.favorite) {
                        addToFavorites(
                            requireActivity(), currentData!!.fireStoreDocId, false)
                        binding.editToolBar.menu[0].setIcon(R.drawable.ic_edit_seed_dialog_not_fav)
                        Log.i("devlog", "Removed from favorite")
                    } else {
                        addToFavorites(
                            requireActivity(), currentData!!.fireStoreDocId, true)
                        binding.editToolBar.menu[0].setIcon(R.drawable.ic_edit_seed_dialog_fav)
                        Log.i("devlog", "Added to favorite")
                    }
                    true
                }
                "Delete" -> {
                    deleteData(requireActivity(), currentData!!, this)
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