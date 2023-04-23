package com.example.waterplants.model

import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import java.time.Instant

class Model private constructor(owner: AppCompatActivity) {
    var bluetooth: MyBluetooth

    var instant: Instant = Instant.now()
    val defaultPlant1= Plant(4,1, 4,12 ,0, "Daffodil", null)
    val defaultPlant2= Plant(5,2, 4,12 ,0, "Lily", null)
    val defaultPlant3= Plant(6,3, 4,12 ,0, "Rose", null)
    var systemPlantList = mutableListOf<Plant>()
    var systemPlants = MutableLiveData<List<Plant>>()
    //var systemPlant1 = Plant(4, null, null, null, null, null, null)
    //var systemPlant2 = Plant(5, null, null, null, null, null, null)
    //var systemPlant3 = Plant(6, null, null, null, null, null, null)
    //var systemPlants2 = arrayOf(systemPlant1, systemPlant2, systemPlant3)
    var systemWaterLevel = MutableLiveData<String>()
    var systemWaterLevel2 = MutableLiveData<Int?>()

    // Instance
    companion object {
        @Volatile private var instance: Model? = null

        // Owner should specify itself as owner, other users should pass owner = null.
        // If owner = null, the method will return null if the owner has yet to initialize it.
        fun getInstance(owner: AppCompatActivity?) =
            instance ?: synchronized(this) { instance ?:
                if (owner == null) null else
                    Model(owner).also { instance = it }
        }
    }
    init {
        bluetooth = MyBluetooth(owner)
        for (i in 0..2)
            systemPlantList.add(i, Plant(i + 4, null, null, null, null, null, null))
        systemPlants.value = systemPlantList
    }

    fun askForSystemStatus(): Boolean {
        val r = getInstance(null)?.bluetooth?.writeData("a")
        if (r == false || r == null)
            return false
        return true
    }

    fun checkForMessage(): String {
        return getInstance(null)?.bluetooth?.readData() ?: ""
    }

    fun processMessage(msg: String): Boolean {
        Log.d("jhgjgjhghjgjhgjh", "||||||||||||||||||||||||||||||||||||||||||")
        if (msg.isNotEmpty()) {
            val strings = msg.substring(1).split(',')

            if (msg[0] == 'a') { // System is sending us its data
                Log.d("jhgjgjhghjgjhgjh", "||||||||||||||||||||||||||||||||||||||||||")
                var ints: Array<Int>
                try {
                    ints = strings.map { it.toInt() }.toTypedArray()
                }catch (e: java.lang.NumberFormatException) {
                    return false
                }
                Log.d("jhgjgjhghjgjhgjh", "||||||||||||||||||||||||||||||||||||||||||")
                if (ints.size != 16)
                    return false
                Log.d("jhgjgjhghjgjhgjh", "||||||||||||||||||||||||||||||||||||||||||")
                for (i in 0..2) {
                    if (systemPlantList[i].pin != ints[i * 5])
                        return false
                    Log.d("jhgjgjhghjgjhgjh", "||||||||||||||||||||||||||||||||||||||||||")
                    systemPlantList[i].intervalDays = ints[i * 5 + 1]
                    systemPlantList[i].amount = ints[i * 5 + 2]
                    systemPlantList[i].hourOfDay = ints[i * 5 + 3]
                    systemPlantList[i].watered = ints[i * 5 + 4]
                }
                systemPlants.value = systemPlantList
                systemWaterLevel.value = ints[15].toString()
                systemWaterLevel2.value = ints[15]
                Log.d("jhgjgjhghjgjhgjh", "water lvl: " + ints[15].toString())
            }
        }
        return true
    }
}