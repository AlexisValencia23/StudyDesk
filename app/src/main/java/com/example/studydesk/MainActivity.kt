package com.example.studydesk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.studydesk.ui.AppNavigation
import com.example.studydesk.ui.theme.StudyDeskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyDeskTheme {
                AppNavigation()
            }
        }
    }
}