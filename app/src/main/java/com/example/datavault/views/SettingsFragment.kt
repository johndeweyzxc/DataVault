package com.example.datavault.views

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.johndeweydev.datavault.R
import com.johndeweydev.datavault.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        // When this fragment enter, slide to the left
        enterTransition = inflater.inflateTransition(R.transition.fragment_slide_right)
        // When this fragment exit, slide to the right
        exitTransition = inflater.inflateTransition(R.transition.fragment_slide_right)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settingsToolBar.setOnClickListener { parentFragmentManager.popBackStack() }
    }
}