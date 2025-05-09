package com.humanperformcenter

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.humanperformcenter.data.SessionDatabase
import com.humanperformcenter.data.SessionRepository
import com.humanperformcenter.ui.theme.HumanPerformAppTheme
import com.humanperformcenter.viewModels.SessionViewModel
import com.humanperformcenter.viewModels.SessionViewModelFactory

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val context = this
            val sessionDao = SessionDatabase.getDatabase(context).sessionDao()
            val sessionRepository = SessionRepository(sessionDao)
            val sessionViewModel: SessionViewModel = viewModel(factory = SessionViewModelFactory(sessionRepository))
            HumanPerformAppTheme {
                Navigation(
                    navController = navController,
                    sessionViewModel = sessionViewModel,
                    // Sonido de borrado
                    onPlaySound = { soundRes ->
                        val mediaPlayer = MediaPlayer.create(this, soundRes)
                        mediaPlayer.setOnCompletionListener { it.release() }
                        mediaPlayer.start()
                    }
                )
            }
        }
    }
}