package com.humanperformcenter.app

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.humanperformcenter.app.navigation.Navigation
import com.humanperformcenter.shared.data.local.impl.AuthLocalDataSourceImpl
import com.humanperformcenter.shared.domain.storage.DataStoreProvider
import com.humanperformcenter.ui.theme.HumanPerformAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AuthLocalDataSourceImpl.initialize(DataStoreProvider.get(applicationContext))

        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()

            HumanPerformAppTheme {
                Navigation(
                    navController = navController,
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
