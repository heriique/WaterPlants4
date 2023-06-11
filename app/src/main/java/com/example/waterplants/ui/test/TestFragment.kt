package com.example.waterplants.ui.test

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.waterplants.databinding.FragmentTestBinding
import com.example.waterplants.model.MessageThread
import com.example.waterplants.model.MessageType
import com.example.waterplants.model.Model

class TestFragment : Fragment() {

    private var _binding: FragmentTestBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val testViewModel =
            ViewModelProvider(this)[TestViewModel::class.java]

        _binding = FragmentTestBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.buttonTest1.setOnClickListener {
            val handler = Model.getInstance(null)?.getMessageHandler()!!
            MessageThread.postMessage("b4,1,211,10,0,5,1,222,11,0,6,1,233,12,1\n", MessageType.WRITE, handler)
        }

        binding.buttonTest2.setOnClickListener {
            val handler = Model.getInstance(null)?.getMessageHandler()!!
            MessageThread.postMessage("t2", MessageType.WRITE, handler)
        }

        binding.buttonTest3.setOnClickListener {
            val handler = Model.getInstance(null)?.getMessageHandler()!!
            MessageThread.postMessage("t3", MessageType.WRITE, handler)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}