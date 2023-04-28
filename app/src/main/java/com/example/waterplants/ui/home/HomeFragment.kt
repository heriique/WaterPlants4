package com.example.waterplants.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.waterplants.R
import com.example.waterplants.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {


    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val buttonConnect: Button = binding.buttonConnect
        buttonConnect.setOnClickListener { homeViewModel.connect() }

        // Image
        val imageView: ImageView = binding.imageView
        homeViewModel.isConnected.observe(viewLifecycleOwner) {
            if (it == false) {
                imageView.setImageResource(R.drawable.baseline_bluetooth_disabled_24)
                buttonConnect.setOnClickListener { homeViewModel.connect() }
                buttonConnect.text = getString(R.string.home_connect)
            }
            else {
                imageView.setImageResource(R.drawable.baseline_bluetooth_connected_24)
                buttonConnect.setOnClickListener {homeViewModel.disconnect()}
                buttonConnect.text = getString(R.string.home_disconnect)
            }
        }


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}