package com.example.waterplants.ui.schedule

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.waterplants.model.Model

class ScheduleViewModel : ViewModel() {
    //var selectedHoseInt = 1
    val selectedHose = MutableLiveData<Int>().apply { value = 0 }

    fun select(position: Int) {
        Model.getInstance(null)?.select(selectedHose.value!!, position)
    }

    fun write() {
        var s = "b"
        val plants = Model.getInstance(null)?.appChosenPlants?.value
        for (i in 0..2) {
            val plant = plants?.get(i)
            s += (i + 4).toString() + ","
            s += plant?.intervalDays.toString() + ","
            s += plant?.amount.toString() + ","
            s += plant?.hourOfDay.toString() + ","
            s += "0" // Vannet
            if (i < 2)
                s += ","
        }
        Model.getInstance(null)?.bluetooth?.writeData(s)
    }
}