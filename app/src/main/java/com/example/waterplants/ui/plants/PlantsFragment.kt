package com.example.waterplants.ui.plants

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.waterplants.R
import com.example.waterplants.databinding.FragmentPlantsBinding
import com.example.waterplants.model.Model
import com.example.waterplants.model.getUriToDrawable

class PlantsFragment : Fragment() {

    private var _binding: FragmentPlantsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val plantsViewModel =
            ViewModelProvider(this).get(PlantsViewModel::class.java)

        _binding = FragmentPlantsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val gridView = binding.gridViewPlants
        val tileAdapter = TileAdapter2(this.requireContext(), Model.getInstance(null)!!.appPlants,
            plantsViewModel.selectedPlant)
        gridView.adapter = tileAdapter
        gridView.setOnItemClickListener{adapterView, parent, position, l->
            run {
                Toast.makeText(this.requireContext(), "Click on $position", Toast.LENGTH_SHORT)
                    .show()
                plantsViewModel.selectedPlant.value = position
                select(position)
                tileAdapter.notifyDataSetChanged()
            }
        }
        select(0)

        val imageViewConnected = binding.imagePlantsConnection

        plantsViewModel.isConnected.observe(viewLifecycleOwner) {
            if (it == false) {
                imageViewConnected.setImageResource(R.drawable.baseline_bluetooth_disabled_24)
            }
            else {
                imageViewConnected.setImageResource(R.drawable.baseline_bluetooth_connected_24)
            }
        }

        fun checkFieldsModified() {
            val plants = Model.getInstance(null)!!.appPlants.value!!
            val s = plantsViewModel.selectedPlant.value!!
            if (s < plants.size) {
                plantsViewModel.fieldsModified.value =
                    binding.editPlantsTime.text.toString() != plants[s].hourOfDay.toString() ||
                        binding.editPlantsName.text.toString() != plants[s].name.toString() ||
                        binding.editPlantsAmount.text.toString() != plants[s].amount.toString() ||
                        binding.editPlantsInterval.text.toString() != plants[s].intervalDays.toString() ||
                        plantsViewModel.pickedImageUri.value != plants[s].imageUri
            } else {
                plantsViewModel.fieldsModified.value =
                    binding.editPlantsTime.text.isNotEmpty() ||
                        binding.editPlantsName.text.toString().isNotEmpty() ||
                        binding.editPlantsAmount.text.toString().isNotEmpty() ||
                        binding.editPlantsInterval.text.toString().isNotEmpty() ||
                        plantsViewModel.pickedImageUri.value != null
            }
        }

        plantsViewModel.pickedImageUri.observe(viewLifecycleOwner) {
            if (it == null)
                binding.imagePlantsPlant.setImageURI(getUriToDrawable(requireContext(), R.drawable.baseline_add_a_photo_24))
            else
                binding.imagePlantsPlant.setImageURI(it)
            checkFieldsModified()
        }

        binding.buttonSave.setOnClickListener {
            var name: String? = null
            var amount: Int? = null
            var intervalDays: Int? = null
            var hourOfDay: Int? = null
            try {
                name = binding.editPlantsName.text.toString()
            } catch (e: java.lang.Exception) {}
            try {
                amount =  Integer.parseInt(binding.editPlantsAmount.text.toString())
            } catch (e: java.lang.Exception) {}
            try {
                intervalDays =  Integer.parseInt(binding.editPlantsInterval.text.toString())
            } catch (e: java.lang.Exception) {}
            try {
                hourOfDay =  Integer.parseInt(binding.editPlantsTime.text.toString())
            } catch (e: java.lang.Exception) {}

            plantsViewModel.savePlant(name, intervalDays, amount, hourOfDay)
            tileAdapter.notifyDataSetChanged()
        }

        binding.imagePlantsPlant.setOnClickListener {
            plantsViewModel.choosePhoto()
            //checkFieldsModified()
        }

        class MyTextWatcher:TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                checkFieldsModified()
            }
            override fun afterTextChanged(p0: Editable?) {
                checkFieldsModified()
            }
        }

        binding.editPlantsTime.addTextChangedListener(MyTextWatcher())
        binding.editPlantsAmount.addTextChangedListener(MyTextWatcher())
        binding.editPlantsInterval.addTextChangedListener(MyTextWatcher())
        binding.editPlantsName.addTextChangedListener(MyTextWatcher())

        plantsViewModel.fieldsModified.observe(viewLifecycleOwner) {
            binding.buttonSave.isEnabled = it
        }
        //binding.buttonSave.isEnabled = false

        return root
    }


    private fun select(position: Int) {
        val plants = Model.getInstance(null)!!.appPlants.value!!
        val newPlant = plants.size == position
        if (newPlant) {
            binding.editPlantsName.setText("")
            //binding.imagePlantsPlant.setImageResource(R.drawable.baseline_add_a_photo_24)
            Model.getInstance(null)?.setPickedImage(null)
            binding.editPlantsInterval.setText("")
            binding.editPlantsTime.setText("")
            binding.editPlantsAmount.setText("")
        }
        else {
            binding.editPlantsName.setText(plants[position].name)
            if (plants[position].imageUri != null) {
                //binding.imagePlantsPlant.setImageURI(plants[position].imageUri)
                Model.getInstance(null)?.setPickedImage(plants[position].imageUri!!)
            }
            else
                Model.getInstance(null)!!.setPickedImage(null)
            if (plants[position].intervalDays == null)
                binding.editPlantsInterval.setText("")
            else
                binding.editPlantsInterval.setText(plants[position].intervalDays!!.toString())
            if (plants[position].hourOfDay == null)
                binding.editPlantsTime.setText("")
            else
                binding.editPlantsTime.setText(plants[position].hourOfDay!!.toString())
            if (plants[position].amount == null)
                binding.editPlantsAmount.setText("")
            else
                binding.editPlantsAmount.setText(plants[position].amount!!.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}