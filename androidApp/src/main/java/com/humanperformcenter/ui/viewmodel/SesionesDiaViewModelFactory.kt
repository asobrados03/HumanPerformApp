import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.humanperformcenter.shared.domain.usecase.SesionDiaUseCase
import com.humanperformcenter.ui.viewmodel.SesionesDiaViewModel

class SesionesDiaViewModelFactory(
    private val useCase: SesionDiaUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SesionesDiaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SesionesDiaViewModel(useCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
