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
    private val paymentViewModel: PaymentViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SecureStorage.initialize(DataStoreProvider.get(applicationContext))

        GooglePayRepository.init(this)

        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51SvlGzB2ovMjVN6tdZD5PPw4F5YBwyTVBvnwnDAl7LHO56HNLlpSKXQyNBjTYBC5FrpsHGT1eddIVWnxvt7CLdWO00nAEdbtM2"
        )

        // ✅ PaymentSheet con traducción Android → KMM
        paymentSheet = PaymentSheet(this) { result ->
            when (result) {
                is PaymentSheetResult.Completed -> {
                    paymentViewModel.onStripeCompleted()
                }

                is PaymentSheetResult.Canceled -> {
                    paymentViewModel.onStripeCanceled()
                }

                is PaymentSheetResult.Failed -> {
                    paymentViewModel.onStripeFailed(
                        result.error.localizedMessage ?: "Error en el pago"
                    )
                }
            }
        }

        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()

            HumanPerformAppTheme {
                Navigation(
                    navController = navController,
                    paymentSheet = paymentSheet,
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
