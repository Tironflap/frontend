package com.tironflap.frontend.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tironflap.frontend.data.launcher.EmulatorLauncher
import com.tironflap.frontend.data.model.Game
import com.tironflap.frontend.data.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    libraryRepository: LibraryRepository,
    private val emulatorLauncher: EmulatorLauncher
) : ViewModel() {

    val games: StateFlow<List<Game>> = libraryRepository.games
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun launchGame(game: Game) {
        viewModelScope.launch {
            emulatorLauncher.launch(game)
        }
    }
}
