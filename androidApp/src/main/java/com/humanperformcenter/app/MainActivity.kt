package com.humanperformcenter.app

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.humanperformcenter.app.navigation.Navigation
import com.humanperformcenter.shared.data.persistence.GooglePayRepository
import com.humanperformcenter.shared.domain.storage.DataStoreProvider
import com.humanperformcenter.shared.domain.storage.SecureStorage
import com.humanperformcenter.ui.theme.HumanPerformAppTheme
import com.humanperformcenter.shared.presentation.viewmodel.PaymentViewModel
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheet.Builder
import com.stripe.android.paymentsheet.PaymentSheetResult
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private lateinit var paymentSheet: PaymentSheet
    val viewModel: PaymentViewModel by viewModel()
    private var onPaymentSheetResult: (PaymentSheetResult) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SecureStorage.initialize(DataStoreProvider.get(applicationContext))

        // Esto prepara GooglePayRepository.paymentsClient
        GooglePayRepository.init(this)
        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51Ki0ZIBrFtqPM4kirIXy3Sb9VQ4XXCz4MJuHTTRYFrrPyIkppkGdlErMqbmM6w3vuybjUw4N75g7ACMlNXzGuxaY00Mmh1jBQ2"
        )

        paymentSheet = Builder { result ->
            viewModel.onPaymentSheetResult(result)
        }.build(this)

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            HumanPerformAppTheme {
                Navigation(
                    navController = navController,
                    paymentSheet = paymentSheet,
                    registerPaymentSheetResult = { handler ->
                        // la UI nos pasa su handler y lo enganchamos
                        onPaymentSheetResult = handler
                    },
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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        GooglePayRepository.handleGooglePayResult(requestCode, resultCode, data)
    }
}