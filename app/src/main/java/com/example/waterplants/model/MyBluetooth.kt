package com.example.waterplants.model

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1
const val MESSAGE_TOAST: Int = 2
class MyBluetooth(private val appCompatActivity: AppCompatActivity, private val handler: Handler) {
    private var bluetoothAdapter: BluetoothAdapter
    private lateinit var btSocket : BluetoothSocket

    // Serial port UUID
    // https://stackoverflow.com/questions/4632524/how-to-find-the-uuid-of-serial-port-bluetooth-device
    private val _myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    // Tag for debug info
    private val _tag = MyBluetooth::class.qualifiedName

    // ActivityResultLauncher for requesting BT permissions with API < 33
    private var requestBluetooth = appCompatActivity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            //granted
        }else{
            //deny
        }
    }

    // ActivityResultLauncher for requesting BT permissions with API >= 33
    private val requestMultiplePermissions =
        appCompatActivity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d(_tag, "${it.key} = ${it.value}")
            }
        }

    init {
        //requestBTPermissions() // Seems not necessary here. Will be done later when needed.
        val bluetoothManager = appCompatActivity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    private fun requestBTPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT))
        }
        else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }
    }

    private var _isConnected = MutableLiveData(false)
    val isConnected : LiveData<Boolean>
        get() {
            return _isConnected
        }

    fun connect(): Boolean {
        // Address discovered with 3rd party Bluetooth Scanner app
        // Probably unique address for each HC-05 device.
        val device = bluetoothAdapter.getRemoteDevice("98:DA:50:01:33:48")
        Log.d(_tag, "Connecting to ... $device")
        Toast.makeText(appCompatActivity.applicationContext, "Connecting...", Toast.LENGTH_SHORT).show()
        if (ActivityCompat.checkSelfPermission(
                appCompatActivity,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // We don't have permissions.
            Toast.makeText(appCompatActivity.applicationContext, "BLUETOOTH_CONNECT permission is not granted! Please allow, and try again.",Toast.LENGTH_LONG).show()
            requestBTPermissions()
            _isConnected.value = false
            return false
        }
        //Toast.makeText(appCompatActivity.applicationContext, "Connecting to ... ${device.name}\n mac: ${device.uuids[0]}\n address: ${device.address}", Toast.LENGTH_SHORT).show()
        bluetoothAdapter.cancelDiscovery()

        // Establish connection
        try {
            btSocket = device.createRfcommSocketToServiceRecord(_myUUID)
            /* Here is the part the connection is made, by asking the device to create a RfcommSocket (Unsecure socket I guess), It map a port for us or something like that */
            btSocket.connect()
            Log.d(_tag, "Connection made.")
            Toast.makeText(appCompatActivity.applicationContext, "Connection made.", Toast.LENGTH_SHORT).show()
            _isConnected.value = true
            ConnectedThread().start()
            return true
        } catch (e: IOException) {
            try {
                btSocket.close()
            } catch (e2: IOException) {
                Log.d(_tag, "Unable to end the connection.\n" + e2.message)
                Toast.makeText(appCompatActivity.applicationContext, "Unable to end the connection.", Toast.LENGTH_SHORT).show()
                _isConnected.value = false
                return false
            }

            Log.d(_tag, "Socket creation failed.\n" + e.message)
            Toast.makeText(appCompatActivity.applicationContext, "Socket creation failed.", Toast.LENGTH_SHORT).show()
            _isConnected.value = false
            return false
        }

    }

    // Old write function, do not use
    fun writeData2(data: String): Boolean {
        if (!isConnected.value!!)
            return false
        var outStream: OutputStream
        try {
            outStream = btSocket.outputStream
        } catch (e: IOException) {
            Log.d(_tag, "Error before sending stuff", e)
            return false
        }
        val msgBuffer = data.toByteArray()

        try {
            outStream.write(msgBuffer)
        } catch (e: IOException) {
            Log.d(_tag, "Error while sending stuff", e)
            return false
        }
        return true
    }

    fun writeData(data: String) {
        ConnectedThread().write(data.toByteArray())
    }

    fun readData(): String {
        if (!isConnected.value!!)
            return ""
        var inStream = btSocket.inputStream
        try {
            inStream = btSocket.inputStream
        } catch (e: IOException) {
            Log.d(_tag, "Error before receiving stuff", e)
        }

        var s = ""

        try {
            while (inStream.available() > 0) {
                // https://developer.android.com/reference/java/io/InputStream#read()
                var c = inStream.read().toChar()
                s += c
                if (c == '\n')
                    break
            }
        } catch (e: IOException) {
            Log.d(_tag, "Error while receiving stuff", e)
        } finally {
            Log.i(_tag, "INFO: Read string: $s")
            return s
        }
    }

    // https://developer.android.com/guide/topics/connectivity/bluetooth/transfer-data
    // Inner class is static
    private inner class ConnectedThread : Thread() {
        private val inStream: InputStream = btSocket.inputStream
        private val outStream: OutputStream = btSocket.outputStream
        private val buffer: ByteArray = ByteArray(1024)

        override fun run() {
            var numBytes: Int // Bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    inStream.read(buffer)
                } catch (e: IOException) {
                    Log.d(_tag, "Input stream was disconnected", e)
                    _isConnected.value = false
                    break
                }

                // Send the obtained bytes to the message handler.
                val readMsg = handler.obtainMessage()
                val bundle = Bundle().apply { putString(MessageType.READ, buffer.decodeToString(0, numBytes)) }
                readMsg.data = bundle
                readMsg.sendToTarget()
            }
        }

        fun write(bytes: ByteArray) {
            try {
                outStream.write(bytes)
            } catch (e: IOException) {
                Log.e(_tag, "Error occurred when sending data", e)

                // Send a failure message back to the activity.
                val writeErrorMsg = handler.obtainMessage()
                val bundle = Bundle().apply {
                    putString(MessageType.TOAST, "Couldn't send data to the other device")
                }
                writeErrorMsg.data = bundle
                handler.sendMessage(writeErrorMsg)
                return
            }

            // Share the sent message with the message handler.
            /*val writtenMsg = handler.obtainMessage(
                MESSAGE_WRITE, -1, -1, buffer)*/
            val writtenMsg = handler.obtainMessage()
            val bundle = Bundle().apply { putString(MessageType.WRITE, String(bytes)) }
            writtenMsg.data = bundle
            writtenMsg.sendToTarget()
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                btSocket.close()
            } catch (e: IOException) {
                Log.e(_tag, "Could not close the connect socket", e)
            }
        }
    }
}