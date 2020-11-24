package com.example.pdsafe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.divyanshu.draw.widget.DrawView
import com.example.pdsafe.databinding.FragmentDrawBinding

class Draw : Fragment() {
    private var canvas: DrawView? = null
    lateinit var binding: FragmentDrawBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_draw, container, false);


        return binding.root
    }
}