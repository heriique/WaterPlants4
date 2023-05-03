package com.example.waterplants.model

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log

object MessageType {
    const val READ = "MESSAGE_READ"
    const val WRITE = "MESSAGE_WRITE"
    const val TOAST = "MESSAGE_TOAST"
    const val PICK_IMAGE = "MESSAGE_PICK_IMAGE"
}

class MessageThread: Thread() {

    lateinit var handler: Handler
    var ready = false

    override fun run() {
        Looper.prepare()
        val looper = Looper.myLooper()
        if (looper != null) {
            handler = object : Handler(looper) {
                override fun handleMessage(msg: Message) {
                    val readString = msg.data.getString(MessageType.READ)
                    val writeString = msg.data.getString(MessageType.WRITE)
                    val pickImageString = msg.data.getString(MessageType.PICK_IMAGE)
                    if (readString != null) {
                        Log.d(MessageThread::class.qualifiedName,
                            "Handling ${MessageType.READ}: $readString")
                        Model.getInstance(null)?.processMessages(readString)
                    }
                    else if (writeString != null) {
                        Log.d(
                            MessageThread::class.qualifiedName,
                            "Handling ${MessageType.WRITE}: $writeString")
                    }
                    else if (pickImageString != null) {
                        Log.d(
                            MessageThread::class.qualifiedName,
                            "Handling ${MessageType.PICK_IMAGE}: $pickImageString")
                        if (pickImageString!= "null")
                            Model.getInstance(null)?.setPickedImage(Uri.parse(pickImageString))
                    }

                }
            }
            ready = true
        }
        else
            Log.d(MessageThread::class.qualifiedName, "myLooper is null")
        Looper.loop()
    }
}