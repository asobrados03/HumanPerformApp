package com.humanperformcenter.app

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.humanperformcenter.app.navigation.Navigation
import com.humanperformcenter.shared.domain.storage.DataStoreProvider
import com.humanperformcenter.shared.domain.storage.SecureStorage
import com.humanperformcenter.shared.presentation.viewmodel.PaymentViewModel
import com.humanperformcenter.ui.theme.HumanPerformAppTheme
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheet.Builder
import com.stripe.android.paymentsheet.PaymentSheetResult
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private lateinit var paymentSheet: PaymentSheet
    private val paymentViewModel: PaymentViewModel by viewModel()
    private lateinit var stripe: Stripe

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SecureStorage.initialize(DataStoreProvider.get(applicationContext))

        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51SvlGzB2ovMjVN6tdZD5PPw4F5YBwyTVBvnwnDAl7LHO56HNLlpSKXQyNBjTYBC5FrpsHGT1eddIVWnxvt7CLdWO00nAEdbtM2"
        )

        stripe = Stripe(
            applicationContext,
            PaymentConfiguration.getInstance(applicationContext).publishableKey
        )

        // ✅ PaymentSheet con traducción Android → KMM
        paymentSheet = Builder { result ->
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
        }.build(this)

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
}
