package com.example.waterplants.model

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

object MessageType {
    const val READ = "MESSAGE_READ"
    const val WRITE = "MESSAGE_WRITE"
    const val TOAST = "MESSAGE_TOAST"
    const val PICK_IMAGE = "MESSAGE_PICK_IMAGE"
}

class MessageThread(private val owner: AppCompatActivity): Thread() {
    private var _tag = MessageThread::class.qualifiedName
    lateinit var handler: Handler
    var ready = false


    companion object {
        fun postMessage(msg: String, msgType: String, handler: Handler) {
            val myMsg = handler.obtainMessage()
            val bundle = Bundle().apply {
                putString(msgType, msg)
            }
            myMsg.data = bundle
            handler.sendMessage(myMsg)
        }
    }
    override fun run() {
        Looper.prepare()
        val looper = Looper.myLooper()
        if (looper != null) {
            handler = object : Handler(looper) {
                override fun handleMessage(msg: Message) {
                    val readString = msg.data.getString(MessageType.READ)
                    val writeString = msg.data.getString(MessageType.WRITE)
                    val pickImageString = msg.data.getString(MessageType.PICK_IMAGE)
                    val toastString = msg.data.getString(MessageType.TOAST)
                    if (readString != null) {
                        Log.d(_tag, "Handling ${MessageType.READ}: $readString")
                        Model.getInstance(null)?.processMessages(readString)
                    }
                    else if (writeString != null) {
                        Log.d(_tag, "Handling ${MessageType.WRITE}: $writeString")
                    }
                    else if (pickImageString != null) {
                        Log.d(_tag, "Handling ${MessageType.PICK_IMAGE}: $pickImageString")
                        if (pickImageString!= "null")
                            Model.getInstance(null)?.setPickedImage(Uri.parse(pickImageString))
                    } else if (toastString != null) {
                        Log.d(_tag, "Handling ${MessageType.TOAST}: $toastString")
                        Toast.makeText(owner, toastString, Toast.LENGTH_SHORT).show()
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