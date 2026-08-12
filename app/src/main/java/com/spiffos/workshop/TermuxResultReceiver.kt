package com.spiffos.workshop

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

private const val TERMUX_RESULT_ACTION =
"com.spiffos.workshop.TERMUX_RESULT"

private const val TERMUX_RESULT_CODE =
"com.termux.RUN_COMMAND_RESULT"

private const val TERMUX_RESULT_STDOUT =
"com.termux.RUN_COMMAND_RESULT_STDOUT"

private const val TERMUX_RESULT_STDERR =
"com.termux.RUN_COMMAND_RESULT_STDERR"

class TermuxResultReceiver : BroadcastReceiver() {

override fun onReceive(
    context: Context,
    intent: Intent
) {

    if (intent.action != TERMUX_RESULT_ACTION) {
        return
    }

    val exitCode =
        intent.getIntExtra(
            TERMUX_RESULT_CODE,
            -1
        )

    val stdout =
        intent.getStringExtra(
            TERMUX_RESULT_STDOUT
        ) ?: ""

    val stderr =
        intent.getStringExtra(
            TERMUX_RESULT_STDERR
        ) ?: ""

    val result =
        buildString {

            append("Código: ")
            append(exitCode)

            append("\n\nSaída:\n")

            append(
                if (stdout.isBlank()) {
                    "(vazio)"
                } else {
                    stdout.trim()
                }
            )

            append("\n\nErro:\n")

            append(
                if (stderr.isBlank()) {
                    "(vazio)"
                } else {
                    stderr.trim()
                }
            )
        }

    val callbackIntent =
        Intent(
            TERMUX_RESULT_ACTION
        ).apply {

            setPackage(
                context.packageName
            )

            putExtra(
                "result",
                result
            )
        }

    context.sendBroadcast(
        callbackIntent
    )
}

}