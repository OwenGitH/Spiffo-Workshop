package com.spiffos.workshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import com.spiffos.workshop.ui.SpiffoApp

class MainActivity : ComponentActivity() {

override fun onCreate(
    savedInstanceState: Bundle?
) {
    super.onCreate(savedInstanceState)

    setContent {
        SpiffoApp(
            onExitApp = {
                finish()
            }
        )
    }
}

}