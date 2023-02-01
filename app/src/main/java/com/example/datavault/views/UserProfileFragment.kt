package com.example.datavault.views

import android.content.Context
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.datavault.Database
import com.example.datavault.MainActivity
import com.example.datavault.R
import com.example.datavault.databinding.FragmentUserProfileBinding
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

        binding.btnUserProfileBack.setOnClickListener { finishFragment() }
        binding.userProfileEditToolBar.setOnClickListener { finishFragment() }

        // Save the data to firebase storage and update the firestore document.
        binding.btnUserProfileSave.setOnClickListener {
            if (binding.etUserProfileName.text.isNullOrBlank()) {
                Toast.makeText(requireActivity(), "Name cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val mainActivity = (activity as MainActivity)
            mainActivity.userMightBeNull(mainActivity.currentUser)

            updateUserProfileData(requireActivity(), binding)
            finishFragment()
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