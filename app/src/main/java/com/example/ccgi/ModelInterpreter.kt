package com.example.ccgi

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.io.FileInputStream
import java.io.IOException

class ModelInterpreter(private val context: Context) {
    private lateinit var interpreter: Interpreter

    fun loadModel() {
        val modelBuffer = loadModelFile("compat_model.tflite")
        interpreter = Interpreter(modelBuffer)
    }

    @Throws(IOException::class)
    private fun loadModelFile(modelPath: String): ByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun predict(major1: Int, mbti1: Int, major2: Int, mbti2: Int): Float {
        val input0 = intArrayOf(major1)
        val input1 = intArrayOf(mbti1)
        val input2 = intArrayOf(major2)
        val input3 = intArrayOf(mbti2)

        val inputs = arrayOf<Any>(
            input0, input1, input2, input3
        )

        val output = Array(1) { FloatArray(1) }

        interpreter.runForMultipleInputsOutputs(inputs, mapOf(0 to output))

        return output[0][0]
    }
}
