package com.example.pdsafe

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class Classifier(private val context: Context, var modelPath: String, var labelPath: String) {
    private var interpreter: Interpreter? = null
    var isInitialised = false
        private set

    private val executorService = Executors.newCachedThreadPool()

    var labels = ArrayList<String>()


    private var inputImageWidth = 0
    private var inputImageHeight = 0
    private var modelSize = 0
    fun initialise(): Task<Void> {
        val task = TaskCompletionSource<Void>()
        try {
            initInterpreter()
            task.setResult(null)

        } catch (e: Exception) {
            task.setException(e)
        }
        return task.task
    }

    //load model
    private fun loadModel(assetManager: AssetManager, path: String): ByteBuffer {
        val fileDescriptor = assetManager.openFd(path)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffSet = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffSet, declaredLength)

    }

    @Throws(IOException::class)
    fun loadLabels(context: Context, filename: String): ArrayList<String> {
        val s = Scanner(InputStreamReader(context.assets.open(filename)))

        val labels = ArrayList<String>()
        while (s.hasNextLine()) {
            labels.add(s.nextLine())
        }
        return labels
    }

    @Throws(IOException::class)
    private fun initInterpreter() {

        val assetManager = context.assets

        val model = loadModel(assetManager, this.modelPath)
        labels = loadLabels(context, this.labelPath)
        //get Interpreter Options
        val ops = Interpreter.Options()

        //enable Android Neural Networks API
        ops.setUseNNAPI(true)

        //enable XNNPack Support(experimental)
        ops.setUseXNNPACK(true)
        val interpreter = Interpreter(model, ops)
        val inputShape = interpreter.getInputTensor(0).shape()
        inputImageWidth = inputShape[1]
        inputImageHeight = inputShape[2]
        modelSize = 4 * inputImageWidth * inputImageHeight * PIXEL_SIZE
        this.interpreter = interpreter
        isInitialised = true
    }

    private fun classify(bitmap: Bitmap): String {
        check(isInitialised)
        {
            "TFLite interpreter not initialised yet"
        }
        val image = Bitmap.createScaledBitmap(bitmap, inputImageWidth, inputImageHeight, true)
        val byteBuffer = convertToByteBuffer(image)
        val output = Array(1) { FloatArray(NUM_RESULTS) }
        interpreter?.run(byteBuffer, output)
        val result = output[0]
        val maxIndex = result.indices.maxByOrNull {
            result[it]
        } ?: -1

        return labels[maxIndex] + "Confidence=" + result[maxIndex]
    }

    fun classifyAsync(bitmap: Bitmap): Task<String> {
        val task = TaskCompletionSource<String>()
        executorService.execute {
            val result = classify(bitmap)
            task.setResult(result)
        }
        return task.task
    }

    fun convertToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(modelSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputImageHeight * inputImageWidth)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)


        for (pixelVal in pixels) {

            val r = ((pixelVal shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD
            val g = ((pixelVal shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD
            val b = ((pixelVal and 0xFF) - IMAGE_MEAN) / IMAGE_STD

            byteBuffer.putFloat(r + g + b)
            //byteBuffer.putFloat(g)
            //byteBuffer.putFloat(b)
        }
        bitmap.recycle()
        return byteBuffer

    }

    fun close() {
        executorService.execute {
            interpreter?.close()
            Log.e("Classifier", "TFLite Interpreter closed")
        }
    }

    companion object {
        const val NUM_RESULTS = 2
        const val IMAGE_MEAN = 127.5f
        const val IMAGE_STD = 127.5f
        const val THRESHOLD = 0.5f
        const val PIXEL_SIZE = 3
    }
}