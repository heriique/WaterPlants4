package com.example.waterplants.ui.system

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import com.example.waterplants.R
import com.example.waterplants.databinding.FragmentSystemBinding

class SystemFragment : Fragment() {

    private var _binding: FragmentSystemBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val systemViewModel =
            ViewModelProvider(this)[SystemViewModel::class.java]

        _binding = FragmentSystemBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //Bind data fields to textViews
        systemViewModel.systemWaterLevel.observe(viewLifecycleOwner) {
            @SuppressLint("SetTextI18n")
            binding.textSystemWaterLevel.text = "${getString(R.string.system_water_level)} $it %"
        }

        systemViewModel.textSystemNext.observe(viewLifecycleOwner) {
            binding.textSystemNext1.text = it[0]
            binding.textSystemNext2.text = it[1]
            binding.textSystemNext3.text = it[2]
        }

        systemViewModel.textSystemAmount.observe(viewLifecycleOwner) {
            binding.textSystemAmount1.text = it[0]
            binding.textSystemAmount2.text = it[1]
            binding.textSystemAmount3.text = it[2]
        }

        systemViewModel.textSystemInterval.observe(viewLifecycleOwner) {
            binding.textSystemInterval1.text = it[0]
            binding.textSystemInterval2.text = it[1]
            binding.textSystemInterval3.text = it[2]
        }

        systemViewModel.textSystemWatered.observe(viewLifecycleOwner) {
            binding.textSystemWatered1.text = it[0]
            binding.textSystemWatered2.text = it[1]
            binding.textSystemWatered3.text = it[2]
        }

        // Button
        val buttonUpdate: Button = binding.buttonSystemUpdate
        buttonUpdate.setOnClickListener {
            systemViewModel.askForSystemStatus()
        }

        // Image
        val imageView: ImageView = binding.imageGrid

        systemViewModel.isConnected.observe(viewLifecycleOwner) {
            buttonUpdate.isEnabled = it
            if (it == false) {
                imageView.setImageResource(R.drawable.baseline_bluetooth_disabled_24)
            }
            else {
                imageView.setImageResource(R.drawable.baseline_bluetooth_connected_24)
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}