package es.uva.sg.psm.planificadorfinanciero

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import es.uva.sg.psm.planificadorfinanciero.data.SessionDatabase
import es.uva.sg.psm.planificadorfinanciero.data.SessionRepository
import es.uva.sg.psm.planificadorfinanciero.viewModels.SessionViewModel
import es.uva.sg.psm.planificadorfinanciero.viewModels.SessionViewModelFactory
import es.uva.sg.psm.planificadorfinanciero.ui.theme.PlanificadorFinancieroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val context = this
            val sessionDao = SessionDatabase.getDatabase(context).sessionDao()
            val sessionRepository = SessionRepository(sessionDao)
            val sessionViewModel: SessionViewModel = viewModel(factory = SessionViewModelFactory(sessionRepository))
            PlanificadorFinancieroTheme {
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