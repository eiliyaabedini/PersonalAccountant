package ir.act.personalAccountant.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.act.personalAccountant.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    val isUserSignedIn = getCurrentUserUseCase().map { user -> user != null }
}