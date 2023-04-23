package com.example.waterplants.ui.system

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
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
            ViewModelProvider(this).get(SystemViewModel::class.java)

        _binding = FragmentSystemBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textViewWaterLevel: TextView = binding.textSystemWaterLevel
        systemViewModel.systemWaterLevel.observe(viewLifecycleOwner) {
            textViewWaterLevel.text = "${getString(R.string.system_water_level)} $it"
        }

        val buttonUpdate: Button = binding.buttonSystemUpdate
        buttonUpdate.setOnClickListener { systemViewModel.askForSystemStatus() }

        val buttonReceive: Button = binding.buttonSystemReceive
        buttonReceive.setOnClickListener { systemViewModel.processMessage("a4,4,1,111,11,5,5,1,122,12,6,6,1,133,13,50") }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}