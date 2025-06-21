package com.example.ccgi

import android.content.Context
import java.util.Properties

object GptKeyProvider {
    private var apiKey: String? = null

    fun init(context: Context) {
        if (apiKey != null) return
        val props = Properties()
        context.assets.open("env.properties").bufferedReader().use {
            props.load(it)
        }
        apiKey = props.getProperty("GPT_API_KEY", "")
    }

    fun get(): String = apiKey ?: ""
}
