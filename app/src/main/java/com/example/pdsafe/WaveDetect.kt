package com.example.pdsafe

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.pdsafe.databinding.ActivityWaveDetectBinding

class WaveDetect : AppCompatActivity() {
    lateinit var binding: ActivityWaveDetectBinding
    private var classifier: Classifier = Classifier(this, "wave_model.tflite", "wave_labels.txt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_wave_detect)
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
        classifier.initialise()
            .addOnFailureListener {
                Log.e("WaveDetect", "Fail", it)
            }
    }

    fun classify() {
        var bitmap = binding.canvasWave.getBitmap()
        classifier.classifyAsync(bitmap)
            .addOnSuccessListener { result ->
                if (result.contains("1"))
                    binding.waveResults.text = "Negative"
                else
                    binding.waveResults.text = "Positive"
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

}