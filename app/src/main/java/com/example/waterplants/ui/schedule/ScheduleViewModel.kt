package com.example.waterplants.ui.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.waterplants.model.MessageThread
import com.example.waterplants.model.MessageType
import com.example.waterplants.model.Model

class ScheduleViewModel : ViewModel() {
    //var selectedHoseInt = 1
    val selectedHose = MutableLiveData<Int>().apply { value = 0 }
    val isConnected: LiveData<Boolean> get() {return Model.getInstance(null)?.bluetooth?.isConnected ?: MutableLiveData(false) }

    fun select(position: Int) {
        Model.getInstance(null)?.select(selectedHose.value!!, position)
    }

    fun write() {
        var s = "b"
        val plants = Model.getInstance(null)?.appChosenPlants?.value
        for (i in 0..2) {
            val plant = plants?.get(i)
            val intervalDays = if (plant?.intervalDays?.toString().isNullOrEmpty()) "0" else plant?.intervalDays?.toString()
            val amount = if (plant?.amount?.toString().isNullOrEmpty()) "0" else plant?.amount?.toString()
            val hourOfDay = if (plant?.hourOfDay?.toString().isNullOrEmpty()) "0" else plant?.hourOfDay?.toString()

            s += (i + 4).toString() + ","
            s += "$intervalDays,"
            s += "$amount,"
            s += "$hourOfDay,"
            s += "0" // Vannet
            s += if (i < 2)
                ","
            else
                "\n"
        }
        //Model.getInstance(null)?.bluetooth?.writeData(s)
        MessageThread.postMessage(s, MessageType.WRITE, Model.getInstance(null)?.getMessageHandler()!!)
    }
}