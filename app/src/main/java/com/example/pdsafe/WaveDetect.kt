package com.example.pdsafe

import android.os.Bundle
import android.util.Log
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.pdsafe.databinding.ActivityWaveDetectBinding

class WaveDetect : AppCompatActivity() {
    lateinit var binding: ActivityWaveDetectBinding
    private var classifier: Classifier = Classifier(this, "wave_model.tflite", "wave_labels.txt")
    private lateinit var anim: Animation


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_wave_detect)

        //setup canvas for drawing
        binding.canvasWave.apply {
            setColor(resources.getColor(R.color.purple_700))
            setStrokeWidth(40.0f)
            setAlpha(60)
        }

        binding.waveClassifyButton.setOnClickListener {
            val t1 = System.currentTimeMillis()
            classify()
            val t2 = System.currentTimeMillis()
            Toast.makeText(this, "Time taken=" + (t2 - t1).toString() + "ms", Toast.LENGTH_SHORT)
                .show()
            binding.canvasWave.clearCanvas()
        }

        //Initialising Tensorflow Lite Classifier
        classifier.initialise()
            .addOnFailureListener {
                Log.e("WaveDetect", "Fail", it)
            }

        //setting up animation for dynamic test
        setupAnimation()
        binding.waveSwitchDynamic.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.waveImage.startAnimation(anim)
            } else {
                //stop and reset animation
                anim.cancel()
                anim.reset()
            }
        }

    }

    fun classify() {
        val bitmap = binding.canvasWave.getBitmap()
        classifier.classifyAsync(bitmap)
            .addOnSuccessListener { result ->
                if (result.contains("1"))
                    binding.waveResults.text = "Negative"
                else
                    binding.waveResults.text = "Positive"
                Toast.makeText(this, result.substring(result.indexOf('C')), Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                Log.e("WaveDetect", "Fail", it)
                binding.waveResults.text = "Fail"
            }
    }

    override fun onDestroy() {
        classifier.close()
        super.onDestroy()
    }


    fun setupAnimation() {
        //setup animation for blinking effect
        anim = AlphaAnimation(1f, 0f)//start from fully opaque to fully transparent
        anim.apply {
            duration = 800
            interpolator = LinearInterpolator()
            repeatMode = Animation.REVERSE
            repeatCount = Animation.INFINITE//repeat forever
        }
    }
}