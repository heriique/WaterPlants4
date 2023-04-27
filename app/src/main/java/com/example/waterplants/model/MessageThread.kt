package com.example.waterplants.model

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log

object MessageType {
    val READ = "MESSAGE_READ"
    val WRITE = "MESSAGE_WRITE"
    val TOAST = "MESSAGE_TOAST"
}

class MessageThread: Thread() {

    lateinit var handler: Handler
    var ready = false

    override fun run() {
        Looper.prepare()
        val looper = Looper.myLooper()
        if (looper != null) {
            handler = object : Handler(looper!!) {
                override fun handleMessage(msg: Message) {
                    val readString = msg.data.getString(MessageType.READ)
                    val writeString = msg.data.getString(MessageType.WRITE)
                    if (readString != null) {
                        Log.d(MessageThread::class.qualifiedName,
                            "Handling ${MessageType.READ}: $readString")
                        Model.getInstance(null)?.processMessages(readString)
                    }
                    else if (writeString != null)
                        Log.d(MessageThread::class.qualifiedName,
                            "Handling ${MessageType.WRITE}: $writeString")

                }
            }
            ready = true
        }
        else
            Log.d(MessageThread::class.qualifiedName, "myLooper is null")
        Looper.loop()
    }
}