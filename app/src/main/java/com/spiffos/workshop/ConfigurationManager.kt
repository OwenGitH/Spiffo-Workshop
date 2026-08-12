package com.spiffos.workshop

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
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

private const val TERMUX_RUN_COMMAND_BACKGROUND =
    "com.termux.RUN_COMMAND_BACKGROUND"

private const val TERMUX_RUN_COMMAND_PENDING_INTENT =
    "com.termux.RUN_COMMAND_PENDING_INTENT"

private const val TERMUX_RESULT_ACTION =
    "com.spiffos.workshop.TERMUX_RESULT"

private const val RESULT_EXTRA =
    "result"

private const val TEST_COMMAND =
    "/data/data/com.termux/files/usr/bin/echo"

private const val TEST_MESSAGE =
    "SPIFFO_TERMUX_OK"

class ConfigurationManager(
    private val context: Context
) {

    fun isTermuxInstalled(): Boolean {

        return try {

            context.packageManager.getPackageInfo(
                TERMUX_PACKAGE,
                0
            )

            true

        } catch (
            _: PackageManager.NameNotFoundException
        ) {

            false
        }
    }

    fun registerReceiver(
        onResult: (String) -> Unit
    ): BroadcastReceiver {

        val receiver =
            object : BroadcastReceiver() {

                override fun onReceive(
                    context: Context,
                    intent: Intent
                ) {

                    if (
                        intent.action !=
                        TERMUX_RESULT_ACTION
                    ) {
                        return
                    }

                    val result =
                        intent.getStringExtra(
                            RESULT_EXTRA
                        )

                    if (
                        !result.isNullOrBlank()
                    ) {

                        onResult(result)
                    }
                }
            }

        val filter =
            IntentFilter(
                TERMUX_RESULT_ACTION
            )

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            context.registerReceiver(
                receiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )

        } else {

            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(
                receiver,
                filter
            )
        }

        return receiver
    }

    fun testTermux(
        onStarted: () -> Unit,
        onError: (String) -> Unit
    ) {

        if (!isTermuxInstalled()) {

            onError(
                "❌ Termux não está instalado."
            )

            return
        }

        try {

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

            val commandIntent =
                Intent().apply {

                    setClassName(
                        TERMUX_PACKAGE,
                        TERMUX_RUN_COMMAND_SERVICE
                    )

                    action =
                        TERMUX_RUN_COMMAND

                    putExtra(
                        TERMUX_RUN_COMMAND_PATH,
                        TEST_COMMAND
                    )

                    putExtra(
                        TERMUX_RUN_COMMAND_ARGUMENTS,
                        arrayOf(
                            TEST_MESSAGE
                        )
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

            context.startService(
                commandIntent
            )

            onStarted()

        } catch (e: Exception) {

            onError(
                "❌ Erro ao enviar comando ao Termux:\n" +
                    (
                        e.message
                            ?: "erro desconhecido"
                    )
            )
        }
    }
}