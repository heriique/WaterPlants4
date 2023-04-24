package com.example.waterplants.model

import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import java.time.Instant

class Model private constructor(owner: AppCompatActivity) {
    var bluetooth: MyBluetooth

    var instant: Instant = Instant.now()
    private val defaultPlant1= Plant(4,1, 4,12 ,false, "Daffodil", null)
    private val defaultPlant2= Plant(5,2, 4,12 ,true, "Lily", null)
    private val defaultPlant3= Plant(6,3, 4,12 ,false, "Rose", null)

    // Copy of plants stored on the Arduino
    private var systemPlantList = mutableListOf<Plant>()
    var systemPlants = MutableLiveData<List<Plant>>()

    // Plants stored in the app
    private var appPlantList = mutableListOf<Plant>()
    var appPlants = MutableLiveData<List<Plant>>()

    var systemWaterLevel = MutableLiveData<Int?>()

    // Singleton instance
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
        appPlantList.add(0, defaultPlant1)
        appPlantList.add(1, defaultPlant2)
        appPlantList.add(2, defaultPlant3)
        appPlants.value = appPlantList
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
        if (msg.isNotEmpty()) {
            val strings = msg.substring(1).split(',')

            if (msg[0] == 'a') { // System is sending us its data
                var ints: Array<Int>
                try {
                    ints = strings.map { it.toInt() }.toTypedArray()
                }catch (e: java.lang.NumberFormatException) {
                    return false
                }
                if (ints.size != 16)
                    return false
                for (i in 0..2) {
                    if (systemPlantList[i].pin != ints[i * 5])
                        return false
                    systemPlantList[i].intervalDays = ints[i * 5 + 1]
                    systemPlantList[i].amount = ints[i * 5 + 2]
                    systemPlantList[i].hourOfDay = ints[i * 5 + 3]
                    systemPlantList[i].watered = ints[i * 5 + 4] != 0
                }
                systemPlants.value = systemPlantList
                systemWaterLevel.value = ints[15]
            }
        }
        return true
    }
}