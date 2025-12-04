package com.example.classroominformer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
//import com.example.classroominformer.ui.home.StudentMainScreen
 import com.example.classroominformer.ui.home.ProfessorMainScreen
// ( this is u have to be advanced based on login user ex.student or professor )
import com.example.classroominformer.ui.theme.ClassroomInformerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClassroomInformerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    //StudentMainScreen()
                    // OR:
                    ProfessorMainScreen()
                }
            }
        }
    }
}
