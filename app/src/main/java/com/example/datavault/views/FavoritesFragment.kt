package com.example.datavault.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datavault.MainActivity
import com.johndeweydev.datavault.R
import com.johndeweydev.datavault.databinding.FragmentFavoritesBinding

class FavoritesFragment : Fragment() {

    private lateinit var binding: FragmentFavoritesBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFavoritesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val scrollableView: RecyclerView = view.findViewById(R.id.rvFavoriteScrollableView)
        scrollableView.adapter = (activity as MainActivity).favoriteAdapter
        scrollableView.layoutManager = LinearLayoutManager(context)
    }
}