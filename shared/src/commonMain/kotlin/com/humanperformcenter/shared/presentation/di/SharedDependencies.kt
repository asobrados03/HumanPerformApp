package com.humanperformcenter.shared.presentation.di

import com.humanperformcenter.shared.presentation.viewmodel.AuthViewModel
import com.humanperformcenter.shared.presentation.viewmodel.DaySessionViewModel
import com.humanperformcenter.shared.presentation.viewmodel.ServiceProductViewModel
import com.humanperformcenter.shared.presentation.viewmodel.StripeViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserBookingsViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserCouponsViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserDocumentsViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserFavoritesViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserProfileViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserSessionViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserStatsViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserWalletViewModel
import org.koin.core.Koin

private var isKoinInitialized = false

private var koinInstance: Koin? = null

fun initKoinIfNeeded() {
    if (!isKoinInitialized) {
        koinInstance = initKoin().koin
        isKoinInitialized = true
    }
}

object SharedDependencies {
    init {
        initKoinIfNeeded()
    }

    private fun koin(): Koin = koinInstance ?: error("Koin no está inicializado")

    fun makeAuthViewModel(): AuthViewModel = koin().get()
    fun makeDaySessionViewModel(): DaySessionViewModel = koin().get()
    fun makeServiceProductViewModel(): ServiceProductViewModel = koin().get()
    fun makeUserSessionViewModel(): UserSessionViewModel = koin().get()
    fun makeUserProfileViewModel(): UserProfileViewModel = koin().get()
    fun makeUserFavoritesViewModel(): UserFavoritesViewModel = koin().get()
    fun makeUserCouponsViewModel(): UserCouponsViewModel = koin().get()
    fun makeUserDocumentsViewModel(): UserDocumentsViewModel = koin().get()
    fun makeUserBookingsViewModel(): UserBookingsViewModel = koin().get()
    fun makeUserWalletViewModel(): UserWalletViewModel = koin().get()
    fun makeUserStatsViewModel(): UserStatsViewModel = koin().get()
    fun makeStripeViewModel(): StripeViewModel = koin().get()
}
