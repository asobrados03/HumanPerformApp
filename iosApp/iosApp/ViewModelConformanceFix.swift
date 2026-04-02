import shared
import KMPObservableViewModelCore

// Compatibilidad: algunas versiones de KMPObservableViewModelSwiftUI
// exigen conformidad explícita al protocolo `ViewModel` en Swift.
extension UserSessionViewModel: ViewModel {}
extension UserProfileViewModel: ViewModel {}
extension UserCouponsViewModel: ViewModel {}
extension AuthViewModel: ViewModel {}
extension UserWalletViewModel: ViewModel {}
extension UserFavoritesViewModel: ViewModel {}
extension StripeViewModel: ViewModel {}
