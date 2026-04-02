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
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

private var isKoinInitialized = false

fun initKoinIfNeeded() {
    if (!isKoinInitialized) {
        initKoin()
        isKoinInitialized = true
    }
}

object SharedDependencies : KoinComponent {
    init {
        initKoinIfNeeded()
    }

    fun makeAuthViewModel(): AuthViewModel = get()
    fun makeDaySessionViewModel(): DaySessionViewModel = get()
    fun makeServiceProductViewModel(): ServiceProductViewModel = get()
    fun makeUserSessionViewModel(): UserSessionViewModel = get()
    fun makeUserProfileViewModel(): UserProfileViewModel = get()
    fun makeUserFavoritesViewModel(): UserFavoritesViewModel = get()
    fun makeUserCouponsViewModel(): UserCouponsViewModel = get()
    fun makeUserDocumentsViewModel(): UserDocumentsViewModel = get()
    fun makeUserBookingsViewModel(): UserBookingsViewModel = get()
    fun makeUserWalletViewModel(): UserWalletViewModel = get()
    fun makeUserStatsViewModel(): UserStatsViewModel = get()
    fun makeStripeViewModel(): StripeViewModel = get()
}
