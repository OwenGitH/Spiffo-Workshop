package com.spiffos.workshop

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

data class DownloadEvent(
    val name: String,
    val uri: String
)

object SpiffoAppState {

    private val _downloadEvents =
        MutableSharedFlow<DownloadEvent>(
            replay = 0,
            extraBufferCapacity = 16
        )

    val downloadEvents =
        _downloadEvents.asSharedFlow()

    private val _downloadVersion =
        MutableStateFlow(0L)

    val downloadVersion =
        _downloadVersion.asStateFlow()

    fun notifyDownloadCompleted(
        name: String,
        uri: String
    ) {
        _downloadVersion.value =
            System.currentTimeMillis()

        _downloadEvents.tryEmit(
            DownloadEvent(
                name = name,
                uri = uri
            )
        )
    }
}