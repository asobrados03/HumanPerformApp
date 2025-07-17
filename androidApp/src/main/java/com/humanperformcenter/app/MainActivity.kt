package com.humanperformcenter.app

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.humanperformcenter.app.navigation.Navigation
import com.humanperformcenter.data.SessionDatabase
import com.humanperformcenter.data.SessionRepository
import com.humanperformcenter.shared.domain.storage.DataStoreProvider
import com.humanperformcenter.shared.domain.storage.SecureStorage
import com.humanperformcenter.shared.domain.storage.createDataStore
import com.humanperformcenter.ui.theme.HumanPerformAppTheme
import com.humanperformcenter.ui.viewmodel.SessionViewModel
import com.humanperformcenter.ui.viewmodel.SessionViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SecureStorage.init(DataStoreProvider.get(applicationContext))

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val context = this
            val sessionDao = SessionDatabase.getDatabase(context).sessionDao()
            val sessionRepository = SessionRepository(sessionDao)
            val sessionViewModel: SessionViewModel = viewModel(
                factory = SessionViewModelFactory(sessionRepository, DataStoreProvider.get(applicationContext))
            )

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