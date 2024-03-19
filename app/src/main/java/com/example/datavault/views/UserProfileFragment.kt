package com.example.datavault.views

import android.app.Activity.RESULT_OK
import android.content.Context
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.datavault.Database
import com.example.datavault.MainActivity
import com.johndeweydev.datavault.R
import com.johndeweydev.datavault.databinding.FragmentUserProfileBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UserProfileFragment : Fragment(), Database {

    private lateinit var binding: FragmentUserProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        // When this fragment enter, slide to the left
        enterTransition = inflater.inflateTransition(R.transition.fragment_slide_right)
        // When this fragment exit, slide to the right
        exitTransition = inflater.inflateTransition(R.transition.fragment_slide_right)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentUserProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainActivity = (activity as MainActivity)

        binding.btnUserProfileBack.setOnClickListener { finishFragment() }
        binding.userProfileEditToolBar.setOnClickListener { finishFragment() }

        // Set the current user profile image
        Glide.with(this).load(mainActivity.viewModel.currentUserProfileImage).into(binding.ivUserProfileImage)

        // If the user clicks on the edit icon, launch activity for getting photo.
        binding.ivUserProfileEdit.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .createIntent { intent -> startForProfileImageResult.launch(intent) }
        }

        // Set the current user name value in edit text
        binding.etUserProfileName.setText(mainActivity.viewModel.userName.value)

        // Save the data to firebase storage and update the firestore document.
        binding.btnUserProfileSave.setOnClickListener {
            if (binding.etUserProfileName.text.isNullOrBlank()) {
                Toast.makeText(requireActivity(), "Name cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (mainActivity.currentUser == null) { mainActivity.navigateToAuthActivity() }
            updateUserProfileData(requireActivity(), binding, mainActivity.viewModel ,mainActivity.currentUser!!.uid)
            finishFragment()
        }
    }

    // Launch the image picker and get data from the result.
    private val startForProfileImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->

        val resultCode = result.resultCode
        val data = result.data

        when (resultCode) {
            RESULT_OK -> binding.ivUserProfileImage.setImageURI(data?.data!!)
            ImagePicker.RESULT_ERROR -> Log.i("devlog", ImagePicker.getError(data))
            else -> Toast.makeText(requireActivity(), "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun finishFragment() {
        val scope = CoroutineScope(Dispatchers.IO)

        closeActiveKeyboard()
        scope.launch {
            delay(200)
            parentFragmentManager.popBackStack()
        }
    }

    private fun closeActiveKeyboard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
    }
}