package com.humanperformcenter.shared.presentation.viewmodel

import com.humanperformcenter.shared.SessionNotificationManager
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.domain.usecase.AccountUseCase
import com.humanperformcenter.shared.domain.usecase.AuthUseCase
import com.humanperformcenter.shared.domain.usecase.BookingsUseCase
import com.humanperformcenter.shared.domain.usecase.CoachesUseCase
import com.humanperformcenter.shared.domain.usecase.CouponUseCase
import com.humanperformcenter.shared.domain.usecase.ProfileUseCase
import com.humanperformcenter.shared.domain.usecase.UserDocumentUseCase
import com.humanperformcenter.shared.domain.usecase.WalletUseCase
import com.humanperformcenter.shared.presentation.ui.CoachState
import com.humanperformcenter.shared.presentation.ui.CouponUiState
import com.humanperformcenter.shared.presentation.ui.DeleteProfilePicState
import com.humanperformcenter.shared.presentation.ui.DeleteUserState
import com.humanperformcenter.shared.presentation.ui.EwalletUiState
import com.humanperformcenter.shared.presentation.ui.FetchUserBookingsState
import com.humanperformcenter.shared.presentation.ui.GetPreferredCoachState
import com.humanperformcenter.shared.presentation.ui.MarkFavoriteState
import com.humanperformcenter.shared.presentation.ui.UpdateState
import com.humanperformcenter.shared.presentation.ui.UploadState
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class UserViewModel(
    profileUseCase: ProfileUseCase,
    accountUseCase: AccountUseCase,
    authUseCase: AuthUseCase,
    coachesUseCase: CoachesUseCase,
    private val couponUseCase: CouponUseCase,
    userDocumentUseCase: UserDocumentUseCase,
    bookingsUseCase: BookingsUseCase,
    walletUseCase: WalletUseCase,
    notificationManager: SessionNotificationManager
) : ViewModel() {
    private val sessionViewModel = UserSessionViewModel(
        accountUseCase = accountUseCase,
        authUseCase = authUseCase
    )
    private val documentsViewModel = UserDocumentsViewModel(userDocumentUseCase)
    private val bookingsViewModel = UserBookingsViewModel(bookingsUseCase, notificationManager)
    private val walletViewModel = UserWalletViewModel(walletUseCase)
    private val favoritesViewModel = UserFavoritesViewModel(coachesUseCase)
    private val profileViewModel = UserProfileViewModel(profileUseCase)

    val isLoggedInFlow: Flow<Boolean> = sessionViewModel.isLoggedInFlow

    @NativeCoroutinesState
    val userData: StateFlow<User?> = sessionViewModel.userData

    @NativeCoroutinesState
    val userBookings: StateFlow<FetchUserBookingsState> = bookingsViewModel.userBookings

    private val _couponUiState = MutableStateFlow(CouponUiState())
    @NativeCoroutinesState
    val couponUiState: StateFlow<CouponUiState> = _couponUiState

    @NativeCoroutinesState
    val uploadState: StateFlow<UploadState> = documentsViewModel.uploadState

    @NativeCoroutinesState
    val isLoading: StateFlow<Boolean> = sessionViewModel.isLoading

    @NativeCoroutinesState
    val updateState: StateFlow<UpdateState> = profileViewModel.updateState

    @NativeCoroutinesState
    val deleteState: StateFlow<DeleteUserState> = sessionViewModel.deleteState

    @NativeCoroutinesState
    val isLoggingOut: StateFlow<Boolean> = sessionViewModel.isLoggingOut

    @NativeCoroutinesState
    val coachesState: StateFlow<CoachState> = favoritesViewModel.coachesState

    @NativeCoroutinesState
    val markFavoriteState: StateFlow<MarkFavoriteState> = favoritesViewModel.markFavoriteState

    @NativeCoroutinesState
    val deleteProfilePicState: StateFlow<DeleteProfilePicState> = profileViewModel.deleteProfilePicState

    @NativeCoroutinesState
    val getPreferredCoachState: StateFlow<GetPreferredCoachState> = favoritesViewModel.getPreferredCoachState

    @NativeCoroutinesState
    val balance: StateFlow<Double?> = walletViewModel.balance

    @NativeCoroutinesState
    val eWalletTransactions: StateFlow<EwalletUiState> = walletViewModel.eWalletTransactions

    fun updateUser(candidate: User, profilePicBytes: ByteArray?) {
        profileViewModel.updateUser(candidate, profilePicBytes, sessionViewModel.currentUserState())
    }

    fun clearUpdateState() {
        profileViewModel.clearUpdateState()
    }

    fun fetchUserProfile() {
        profileViewModel.fetchUserProfile(sessionViewModel.currentUserState())
    }

    fun deleteUser(email: String) {
        sessionViewModel.deleteUser(email)
    }

    fun resetDeleteState() {
        sessionViewModel.resetDeleteState()
    }

    fun logout(onSuccess: () -> Unit) {
        sessionViewModel.logout(onSuccess)
    }

    fun getCoaches() {
        favoritesViewModel.getCoaches()
    }

    fun deleteProfilePic(user: User) {
        profileViewModel.deleteProfilePic(user, sessionViewModel.currentUserState())
    }

    fun clearDeleteProfilePicState() {
        profileViewModel.clearDeleteProfilePicState()
    }

    fun markFavorite(coachId: Int, serviceName: String?, userId: Int?) {
        favoritesViewModel.markFavorite(coachId, serviceName, userId)
    }

    fun clearMarkFavoriteState() {
        favoritesViewModel.clearMarkFavoriteState()
    }

    fun getPreferredCoach(userId: Int?) {
        favoritesViewModel.getPreferredCoach(userId)
    }

    fun clearGetPreferredCoachState() {
        favoritesViewModel.clearGetPreferredCoachState()
    }

    fun loadUserCoupon(userId: Int) = viewModelScope.launch {
        _couponUiState.update {
            it.copy(
                isLoading = true,
                error = null
            )
        }
        couponUseCase.getUserCoupons(userId).onSuccess { coupons ->
            _couponUiState.update {
                it.copy(
                    isLoading = false,
                    currentCoupons = coupons
                )
            }
        }.onFailure { ex ->
            _couponUiState.update {
                it.copy(
                    isLoading = false,
                    error = ex.message
                )
            }
        }
    }

    fun onCouponCodeChanged(code: String) {
        _couponUiState.update { it.copy(code = code, error = null) }
    }

    fun addCouponToUser(userId: Int, code: String) = viewModelScope.launch {
        _couponUiState.update { it.copy(isLoading = true, error = null) }

        couponUseCase.addCouponToUser(userId, code).onSuccess {
            couponUseCase.getUserCoupons(userId).onSuccess { updatedCoupons ->
                _couponUiState.update {
                    it.copy(
                        isLoading = false,
                        currentCoupons = updatedCoupons,
                        code = ""
                    )
                }
            }.onFailure { ex ->
                _couponUiState.update {
                    it.copy(
                        isLoading = false,
                        error = ex.message
                    )
                }
            }
        }.onFailure { ex ->
            _couponUiState.update {
                it.copy(
                    isLoading = false,
                    error = ex.message
                )
            }
        }
    }

    fun uploadDocument(userId: Int, name: String, data: ByteArray) {
        documentsViewModel.uploadDocument(userId, name, data)
    }

    fun resetUploadState() {
        documentsViewModel.resetUploadState()
    }

    fun fetchUserBookings(userId: Int) {
        bookingsViewModel.fetchUserBookings(userId)
    }

    fun cancelUserBooking(bookingId: Int) {
        bookingsViewModel.cancelUserBooking(bookingId, sessionViewModel.currentUserState().value)
    }

    fun loadBalance(userId: Int) {
        walletViewModel.loadBalance(userId)
    }

    fun loadEwalletTransactions(userId: Int) {
        walletViewModel.loadEwalletTransactions(userId)
    }
}
