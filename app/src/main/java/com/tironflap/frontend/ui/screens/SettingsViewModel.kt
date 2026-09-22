package com.tironflap.frontend.ui.screens

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tironflap.frontend.data.model.RomDirectory
import com.tironflap.frontend.data.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    val directories: StateFlow<List<RomDirectory>> = libraryRepository.directories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _scanStatus = MutableStateFlow<String?>(null)
    val scanStatus: StateFlow<String?> = _scanStatus.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    init {
        viewModelScope.launch {
            libraryRepository.ensureDefaultSystems()
        }
    }

    fun addDirectory(uri: Uri, displayName: String) {
        viewModelScope.launch {
            libraryRepository.addDirectory(uri, displayName)
        }
    }

    fun removeDirectory(directory: RomDirectory) {
        viewModelScope.launch {
            libraryRepository.removeDirectory(directory)
        }
    }

    fun scanLibrary() {
        if (_isScanning.value) return
        viewModelScope.launch {
            _isScanning.value = true
            _scanStatus.value = "Starting scan..."
            try {
                // Clear old junk so re-scan applies new filters
                libraryRepository.clearLibrary()
                val result = libraryRepository.scanAndScrape { msg ->
                    _scanStatus.value = msg
                }
                _scanStatus.value =
                    "Done: ${result.added} games, ${result.skippedJunk} junk removed, ${result.verified} hash-verified"
            } catch (e: Exception) {
                _scanStatus.value = "Error: ${e.message}"
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun clearScanStatus() {
        _scanStatus.value = null
    }
}
