package com.tomclaw.cache.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.tomclaw.cache.demo.presentation.MainScreen
import com.tomclaw.cache.demo.presentation.MainViewModel
import com.tomclaw.cache.demo.ui.theme.DiskLruCacheTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as App
        val viewModel = ViewModelProvider(this, app.viewModelFactory)[MainViewModel::class.java]

        setContent {
            DiskLruCacheTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
