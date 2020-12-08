package com.example.pdsafe

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.pdsafe.databinding.ActivityMainBinding

class SpiralDetect : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private var classifier: Classifier =
        Classifier(this, "spiral_model.tflite", "spiral_labels.txt")
    private lateinit var anim: Animation


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.canvas.apply {
            setColor(resources.getColor(R.color.purple_700))
            setStrokeWidth(40.0f)
            setAlpha(60)
        }

        //setup canvas for drawing
        binding.classifyButton.setOnClickListener {
            val t1 = System.currentTimeMillis()
            classify()
            val t2 = System.currentTimeMillis()
            Log.e("Time taken", (t2 - t1).toString())
            Toast.makeText(this, "Time taken=" + (t2 - t1).toString() + "ms", Toast.LENGTH_SHORT)
                .show()
            binding.canvas.clearCanvas()
        }

        //setup Tensorflow Lite Classifier
        classifier.initialise()
            .addOnFailureListener {
                Log.e("Mainactivity", "Error", it)
            }

        //setup animation
        setupAnimation()
        binding.spiralDynamicSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.spiralImage.startAnimation(anim)
            } else {
                anim.cancel()
                anim.reset()
            }
        }
    }

    override fun onDestroy() {
        classifier.close()
        super.onDestroy()
    }

    fun classify() {
        var bitmap = binding.canvas.getBitmap()
        classifier.classifyAsync(bitmap)
            .addOnSuccessListener { result ->
                if (result.contains("0"))
                    binding.results.text = "Negative"
                else
                    binding.results.text = "Positive"
                binding.results.visibility = View.VISIBLE
                Log.e("Draw", result)
            }
            .addOnFailureListener {
                Log.e("MainActivity", "Fail", it)
                binding.results.text = "Fail"
            }
    }

    fun setupAnimation() {
        anim = AlphaAnimation(1f, 0f)
        anim.apply {
            duration = 800
            interpolator = LinearInterpolator()
            repeatCount = Animation.INFINITE
            repeatMode = Animation.REVERSE
        }
    }

}