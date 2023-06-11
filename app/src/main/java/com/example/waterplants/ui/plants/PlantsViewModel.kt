package com.example.waterplants.ui.plants

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.waterplants.model.Model
import com.example.waterplants.model.Plant

class PlantsViewModel : ViewModel() {
    val isConnected: LiveData<Boolean> get() {return Model.getInstance(null)?.bluetooth?.isConnected ?: MutableLiveData(false) }
    val selectedPlant = MutableLiveData<Int>().apply { value = 0 }
    val pickedImageUri: LiveData<Uri?> get() {
        return Model.getInstance(null)!!.pickedImageUri
    }
    var fieldsModified = MutableLiveData(false)

    fun savePlant(name: String?, intervalDays: Int?, amount: Int?, hourOfDay: Int?) {
        val plants = Model.getInstance(null)?.appPlants?.value!!
        if (selectedPlant.value == plants.size) {
            val p = Plant(intervalDays, amount, hourOfDay, null, name, pickedImageUri.value)
            Model.getInstance(null)?.addPlant(p)
        } else {
            plants[selectedPlant.value!!].name = name
            plants[selectedPlant.value!!].intervalDays = intervalDays
            plants[selectedPlant.value!!].amount = amount
            plants[selectedPlant.value!!].hourOfDay = hourOfDay
            plants[selectedPlant.value!!].imageUri = pickedImageUri.value
        }
        fieldsModified.value = false
    }

    fun choosePhoto() {
        Model.getInstance(null)?.pickImage()
    }
}