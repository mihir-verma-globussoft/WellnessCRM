package com.globussoft.wellness.patient.feature.membership.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globussoft.wellness.patient.core.util.Result
import com.globussoft.wellness.patient.feature.membership.domain.usecase.GetMembershipPlansUseCase
import com.globussoft.wellness.patient.feature.membership.domain.usecase.GetMyMembershipsUseCase
import com.globussoft.wellness.patient.feature.membership.presentation.state.MembershipsUiEvent
import com.globussoft.wellness.patient.feature.membership.presentation.state.MembershipsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MembershipsNavEvent {
    object Back : MembershipsNavEvent()
}

@HiltViewModel
class MembershipsViewModel @Inject constructor(
    private val getMyMemberships: GetMyMembershipsUseCase,
    private val getMembershipPlans: GetMembershipPlansUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MembershipsUiState())
    val uiState: StateFlow<MembershipsUiState> = _uiState.asStateFlow()

    private val _navEvent = Channel<MembershipsNavEvent>(Channel.BUFFERED)
    val navEvent = _navEvent.receiveAsFlow()

    init {
        load()
    }

    fun onEvent(event: MembershipsUiEvent) {
        when (event) {
            MembershipsUiEvent.Refresh -> load()
            is MembershipsUiEvent.SelectMembership -> _uiState.value = _uiState.value.copy(selectedMembership = event.membership)
            MembershipsUiEvent.DismissDetail -> _uiState.value = _uiState.value.copy(selectedMembership = null)
            MembershipsUiEvent.TogglePlans -> _uiState.value = _uiState.value.copy(showPlans = !_uiState.value.showPlans)
            MembershipsUiEvent.NavigateBack -> viewModelScope.launch { _navEvent.send(MembershipsNavEvent.Back) }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val membershipsDeferred = async { getMyMemberships() }
            val plansDeferred = async { getMembershipPlans() }
            val membershipsResult = membershipsDeferred.await()
            val plansResult = plansDeferred.await()
            _uiState.value = MembershipsUiState(
                isLoading = false,
                memberships = if (membershipsResult is Result.Success) membershipsResult.data else emptyList(),
                plans = if (plansResult is Result.Success) plansResult.data else emptyList(),
                error = if (membershipsResult is Result.Error) membershipsResult.message else null,
            )
        }
    }
}
