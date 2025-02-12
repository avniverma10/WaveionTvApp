package com.example.tvapp

import LoginScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.tv.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.common.util.Log
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import com.example.tvapp.database.EPGScreen
import com.example.tvapp.navigation.AppNavGraph
import com.example.tvapp.ui.theme.TVAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val epgPrograms = XMLParser.readEPGFromAssets(this) // Parse XML
        setContent {
            TVAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    EPGScreen(epgPrograms) // Display parsed data
                }
            }
        }

//        val epgPrograms = XMLParser.readEPGFromAssets(this)
//
//        epgPrograms.forEach { program ->
//            Log.d("AVNI123", "${program.startTime} - ${program.endTime}: ${program.eventName}")
//        }
    }
}
