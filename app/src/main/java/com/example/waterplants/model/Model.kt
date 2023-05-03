package com.example.waterplants.model

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.AnyRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.waterplants.MainActivity
import com.example.waterplants.R


class Model private constructor(owner: AppCompatActivity) {
    private var _tag = Model::class.qualifiedName
    private lateinit var _owner: AppCompatActivity

    private val numPlantProperties = 5
    private val numSystemPlants = 3
    private val additionalProperties = 1 //water level
    private val numSystemProperties = numPlantProperties * numSystemPlants + additionalProperties
    private val systemHoseOffset = 4 // System hoses are numbered 4, 5, 6

    private val defaultPlant1= Plant(4,7, 80,20,false,
        "Dracaenas", getUriToDrawable(owner.applicationContext, R.drawable.dracaenas))
    private val defaultPlant2= Plant(5,3, 30,20,false,
        "Fredslilje", getUriToDrawable(owner.applicationContext, R.drawable.fredslilje))
    private val defaultPlant3= Plant(6,9, 100,20,false,
        "Sansevieria", getUriToDrawable(owner.applicationContext, R.drawable.sansevieria))

    // Copy of plants stored on the Arduino
    private var systemPlantList = mutableListOf<Plant>()
    var systemPlants = MutableLiveData<List<Plant>>()

    // Plants stored in the app
    private var appPlantList = mutableListOf<Plant>()
    var appPlants = MutableLiveData<List<Plant>>()

    private var appChosenPlantList = mutableListOf<Plant>()
    var appChosenPlants = MutableLiveData<List<Plant>>()

    var systemWaterLevel = MutableLiveData<Int?>()

    var bluetooth: MyBluetooth

    private var _pickedImageUri = MutableLiveData<Uri?>(null)
    val pickedImageUri: LiveData<Uri?> get() = _pickedImageUri

    private val messageThread = MessageThread().apply { start() }

    // Singleton instance
    companion object {
        @Volatile private var instance: Model? = null

        // Owner should specify itself as owner, other users should pass owner = null.
        // If owner = null, the method will return null if the owner has yet to initialize it.
        fun getInstance(owner: AppCompatActivity?) =
            instance ?: synchronized(this) { instance ?:
                if (owner == null) null else
                    Model(owner).also {
                        instance = it
                        instance!!._owner = owner
                    }
        }
    }
    init {
        @Suppress("ControlFlowWithEmptyBody")
        while (!messageThread.ready) {}
        bluetooth = MyBluetooth(owner, messageThread.handler)

        for (i in 0..2)
            systemPlantList.add(i, Plant(i + systemHoseOffset, null, null, null, null, null, null))
        systemPlants.value = systemPlantList
        appPlantList.add(0, defaultPlant1)
        appPlantList.add(1, defaultPlant2)
        appPlantList.add(2, defaultPlant3)
        appPlants.value = appPlantList

        for (i in 0..2)
            appChosenPlantList.add(appPlantList[(i+1)%3])
        appChosenPlants.value = appChosenPlantList


    }

    /*fun pickImageSetup() {
        _pickedImageUriDefault =
            getUriToDrawable(_owner.applicationContext, R.drawable.baseline_add_a_photo_24)
        _pickedImageUri = MutableLiveData(_pickedImageUriDefault)
    }*/

    fun getHandler(): android.os.Handler {
        return messageThread.handler
    }

    fun pickImage() {
        (_owner as MainActivity).pickImage()
    }

    fun setPickedImage(uri: Uri?) {
        _pickedImageUri.postValue(uri)
    }

    fun addPlant(p: Plant) {
        appPlantList.add(p)
    }

    fun select(hose: Int, plant: Int) {
        appChosenPlantList[hose] = appPlantList[plant]
        appChosenPlants.value = appChosenPlantList
    }

    fun askForSystemStatus() {
        getInstance(null)?.bluetooth?.writeData("a")
    }

    fun processMessages(msg: String) {
        val strings = msg.split('\n')
        for (s in strings)
            processMessage(s)
    }

    private fun processMessage(msg: String): Boolean {
        Log.d("Model", "Processing string: $msg")
        if (msg.isNotEmpty()) {
            val strings = msg.substring(1, msg.length - (if (msg.last() == '\n')  2 else 1)).split(',')
            Log.d(_tag, "First char: ${msg[0]}")

            // System is sending us its data
            if (msg[0] == 'a') {
                val ints: Array<Int>
                try {
                    ints = strings.map { it.toInt() }.toTypedArray()
                }catch (e: java.lang.NumberFormatException) {
                    Log.d(_tag, "NumberFormatException while parsing string.")
                    return false
                }
                if (ints.size != numSystemProperties)
                    return false
                for (i in 0..2) {
                    if (systemPlantList[i].pin != ints[i * 5])
                        return false
                    systemPlantList[i].intervalDays = ints[i * 5 + 1]
                    systemPlantList[i].amount = ints[i * 5 + 2]
                    systemPlantList[i].hourOfDay = ints[i * 5 + 3]
                    systemPlantList[i].watered = ints[i * 5 + 4] != 0
                }
                systemPlants.postValue(systemPlantList)
                systemWaterLevel.postValue(ints[15])

            }
            // System is sending a debug message
            else if (msg[0] == 'd') {
                Log.d(_tag, "Arduino message:\n${msg.substring(1)}")
            }
            // System is sending a message to display in app
            else if (msg[0] == 'i') {
                Toast.makeText(instance!!._owner, msg.substring(1),Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }
}

// https://stackoverflow.com/questions/6602417/get-the-uri-of-an-image-stored-in-drawable
/**
 * get uri to drawable or any other resource type if u wish
 * @param context - context
 * @param drawableId - drawable res id
 * @return - uri
 */
fun getUriToDrawable(
    context: Context,
    @AnyRes drawableId: Int
): Uri {
    return Uri.parse(
        ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + context.resources.getResourcePackageName(drawableId)
                + '/' + context.resources.getResourceTypeName(drawableId)
                + '/' + context.resources.getResourceEntryName(drawableId)
    )
}