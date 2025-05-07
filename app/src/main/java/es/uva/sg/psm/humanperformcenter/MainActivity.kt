package es.uva.sg.psm.humanperformcenter

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import es.uva.sg.psm.humanperformcenter.data.SessionDatabase
import es.uva.sg.psm.humanperformcenter.data.SessionRepository
import es.uva.sg.psm.humanperformcenter.ui.theme.HumanPerformAppTheme
import es.uva.sg.psm.humanperformcenter.viewModels.SessionViewModel
import es.uva.sg.psm.humanperformcenter.viewModels.SessionViewModelFactory

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