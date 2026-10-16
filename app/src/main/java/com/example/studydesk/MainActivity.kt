package com.example.studydesk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.studydesk.ui.Apuntes
import com.example.studydesk.ui.Entregas
import com.example.studydesk.ui.Inicio
import com.example.studydesk.ui.Login
import com.example.studydesk.ui.theme.StudyDeskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyDeskTheme{
                //Login()
                //Registro()
                //Inicio()
                //Entregas()
                Apuntes()


            }
        }
    }
}

