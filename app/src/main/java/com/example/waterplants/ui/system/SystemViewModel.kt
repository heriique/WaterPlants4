package com.example.waterplants.ui.system

import android.app.Application
import androidx.lifecycle.*
import com.example.waterplants.R
import com.example.waterplants.model.Model
import com.example.waterplants.model.Plant
import java.time.Duration
import java.time.Instant

class SystemViewModel(application: Application) : AndroidViewModel(application) {
    val systemWaterLevel: LiveData<Int?> = Model.getInstance(null)?.systemWaterLevel!!
    private val systemPlants: LiveData<List<Plant>> = Model.getInstance(null)?.systemPlants!!
    val isConnected: LiveData<Boolean> get() {return Model.getInstance(null)?.bluetooth?.isConnected ?: MutableLiveData(false) }

    //val textSystemHose: LiveData<List<String>> = systemPlants.map { hoseToString(systemPlants.value!!) }
    val textSystemNext: LiveData<List<String>> = systemPlants.map { nextToString(systemPlants.value!!) }
    val textSystemAmount: LiveData<List<String>> = systemPlants.map { amountToString(systemPlants.value!!) }
    val textSystemInterval: LiveData<List<String>> = systemPlants.map {intervalToString(systemPlants.value!!)}
    val textSystemWatered: LiveData<List<String>> = systemPlants.map { wateredToString(systemPlants.value!!) }

    private fun getString(r: Int): String {
        return getApplication<Application>().resources.getString(r)
    }
    /*private fun hoseToString(plants: List<Plant>): List<String> {
        val r = mutableListOf<String>()
            for (i in 0..2)
                r.add("${plants[i].pin?.minus(3)}")
        return r
    }*/

    private fun nextToString(plants: List<Plant>): List<String> {
        val r = mutableListOf<String>()
        for (i in 0..2)
            if (plants[i].hourOfDay == null || plants[i].hourOfDay!! < 0
                || plants[i].hourOfDay!! > 23 || plants[i].intervalDays == null) {
                r.add(getString(R.string.system_empty))
            } else {
                val h : Int = plants[i].hourOfDay!!
                val d : Int = plants[i].intervalDays!!
                val now = Instant.now()
                val hoursElapsed: Long = now.epochSecond / (60*60)
                val daysElapsed: Long = hoursElapsed / 24
                val cyclesElapsed: Long = daysElapsed / d
                var daysToCycleStart: Long = cyclesElapsed * d
                val daysIntoCycle: Long = daysElapsed % d
                val hoursIntoDay: Long = hoursElapsed % 24
                if (!(daysIntoCycle == 0L && hoursIntoDay < h)) {
                    daysToCycleStart += d
                }
                val next = Instant.ofEpochSecond((daysToCycleStart * 24 + h) * 60 * 60)
                val difference = Duration.between(now, next).toHours()
                val days = difference / 24
                val hours = difference % 24
                r.add("$days ${getString(R.string.system_days)}, $hours ${getString(R.string.system_hours)}")
            }
        return r
    }

    private fun amountToString(plants: List<Plant>): List<String> {
        val r = mutableListOf<String>()
        for (i in 0..2)
            r.add("${plants[i].amount ?: getString(R.string.system_empty)}")
        return r
    }

    private fun intervalToString(plants: List<Plant>): List<String> {
        val r = mutableListOf<String>()
        for (i in 0..2)
            if (plants[i].intervalDays == null) {
                r.add(getString(R.string.system_empty))
            } else {
                r.add("${plants[i].intervalDays} ${getString(R.string.system_days)}")
            }
        return r
    }

    private fun wateredToString(plants: List<Plant>): List<String> {
        val r = mutableListOf<String>()
        for (i in 0..2)
            if (plants[i].watered == null) {
                r.add(getString(R.string.system_empty))
            } else {
                if (plants[i].watered!!) {
                    r.add(getString(R.string.yes))
                } else {
                    r.add(getString(R.string.no))
                }
            }
        return r
    }

    fun askForSystemStatus() {
        Model.getInstance(null)?.askForSystemStatus()
    }
}