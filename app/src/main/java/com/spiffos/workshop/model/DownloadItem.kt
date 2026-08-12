package com.spiffos.workshop

data class DownloadItem(
val id: String,
val progress: Int = 0,
val status: String = "queued",
val message: String = "Na fila",
val modName: String = "",
val elapsed: Long = 0
)