package com.spiffos.workshop

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

private const val TERMUX_PACKAGE =
"com.termux"

private const val TERMUX_RUN_COMMAND_SERVICE =
"com.termux.app.RunCommandService"

private const val TERMUX_RUN_COMMAND =
"com.termux.RUN_COMMAND"

private const val TERMUX_RUN_COMMAND_PATH =
"com.termux.RUN_COMMAND_PATH"

private const val TERMUX_RUN_COMMAND_ARGUMENTS =
"com.termux.RUN_COMMAND_ARGUMENTS"

private const val TERMUX_RUN_COMMAND_WORKDIR =
"com.termux.RUN_COMMAND_WORKDIR"

private const val TERMUX_RUN_COMMAND_BACKGROUND =
"com.termux.RUN_COMMAND_BACKGROUND"

private const val TERMUX_RUN_COMMAND_PENDING_INTENT =
"com.termux.RUN_COMMAND_PENDING_INTENT"

private const val TERMUX_RESULT_ACTION =
"com.spiffos.workshop.TERMUX_RESULT"

private const val TERMUX_SCRIPT =
"/data/data/com.termux/files/home/baixar-mod.sh"

fun sendDownloadCommand(
context: Context,
workshopId: String,
onResult: (String) -> Unit
) {

try {

    if (workshopId.isBlank()) {

        onResult(
            "❌ Informe o Workshop ID."
        )

        return
    }

    val resultIntent =
        Intent(
            context,
            TermuxResultReceiver::class.java
        ).apply {

            action =
                TERMUX_RESULT_ACTION
        }

    val flags =
        PendingIntent.FLAG_ONE_SHOT or
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.S
            ) {
                PendingIntent.FLAG_MUTABLE
            } else {
                0
            }

    val pendingIntent =
        PendingIntent.getBroadcast(
            context,
            System.currentTimeMillis()
                .toInt(),
            resultIntent,
            flags
        )

    val intent =
        Intent().apply {

            setClassName(
                TERMUX_PACKAGE,
                TERMUX_RUN_COMMAND_SERVICE
            )

            action =
                TERMUX_RUN_COMMAND

            putExtra(
                TERMUX_RUN_COMMAND_PATH,
                TERMUX_SCRIPT
            )

            putExtra(
                TERMUX_RUN_COMMAND_ARGUMENTS,
                arrayOf(workshopId)
            )

            putExtra(
                TERMUX_RUN_COMMAND_WORKDIR,
                "/data/data/com.termux/files/home"
            )

            putExtra(
                TERMUX_RUN_COMMAND_BACKGROUND,
                true
            )

            putExtra(
                TERMUX_RUN_COMMAND_PENDING_INTENT,
                pendingIntent
            )
        }

    context.startService(intent)

    onResult(
        "📥 Download iniciado.\n\nID: $workshopId"
    )

} catch (e: Exception) {

    onResult(
        "❌ Erro ao iniciar download:\n" +
            (
                e.message
                    ?: "erro desconhecido"
            )
    )
}

}