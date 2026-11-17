package com.ians.halerchat.feature.rooms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ians.halerchat.core.data.room.RoomsRepository
import com.ians.halerchat.core.data.room.RoomsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class RoomsViewModel(private val repository: RoomsRepository) : ViewModel() {
    private val _state = MutableStateFlow(RoomsState())
    val state: StateFlow<RoomsState> = _state.asStateFlow()

    init {
        load()
    }

    fun onIntent(intent: RoomsIntent) {
        when (intent) {
            RoomsIntent.Retry -> load()
        }
    }

    private fun load() {
        if (_state.value.isLoading) return

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            when (val roomsResult = repository.getRooms()) {
                is RoomsResult.SessionExpired -> {}

                is RoomsResult.Failure -> {
                    _state.update { it.copy(isLoading = false, error = roomsResult) }
                }

                is RoomsResult.Success -> {
                    _state.update { it.copy(isLoading = false, rooms = roomsResult.rooms) }
                }
            }
        }
    }
}
