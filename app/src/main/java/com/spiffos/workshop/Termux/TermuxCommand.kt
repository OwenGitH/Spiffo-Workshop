package com.spiffos.workshop.Termux

import android.content.Context
import android.content.Intent

object TermuxCommand {

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

    private const val INSTALLER_URL =
        "https://raw.githubusercontent.com/OwenGitH/SpiffoConfigTermux/refs/heads/main/Spiffo%20installer"

    fun test(
        context: Context,
        onStarted: () -> Unit,
        onError: (String) -> Unit
    ) {

        try {

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
                        "/data/data/com.termux/files/usr/bin/bash"
                    )

                    putExtra(
                        TERMUX_RUN_COMMAND_ARGUMENTS,
                        arrayOf(
                            "-c",
                            """
                            if [ -f "/data/data/com.termux/files/home/.spiffo_configuration_ok" ] &&
                               [ -f "/data/data/com.termux/files/home/.spiffo_depot_ok" ] &&
                               [ -f "/data/data/com.termux/files/home/.spiffo_downloader_ok" ]; then
                                echo "SPIFFO_CONFIGURATION_OK"
                            else
                                echo "SPIFFO_CONFIGURATION_PENDING"
                            fi
                            """.trimIndent()
                        )
                    )

                    putExtra(
                        TERMUX_RUN_COMMAND_BACKGROUND,
                        true
                    )
                }

            context.startService(
                commandIntent
            )

            onStarted()

        } catch (e: Exception) {

            onError(
                e.message
                    ?: "Não foi possível iniciar o comando do Termux."
            )
        }
    }

    fun install(
        context: Context,
        onStarted: () -> Unit,
        onError: (String) -> Unit
    ) {

        try {

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
                        "/data/data/com.termux/files/usr/bin/bash"
                    )

                    putExtra(
                        TERMUX_RUN_COMMAND_ARGUMENTS,
                        arrayOf(
                            "-c",
                            """
                            bash <(curl -sSL "$INSTALLER_URL")
                            """.trimIndent()
                        )
                    )

                    putExtra(
                        TERMUX_RUN_COMMAND_BACKGROUND,
                        false
                    )
                }

            context.startService(
                commandIntent
            )

            val launchIntent =
                context.packageManager
                    .getLaunchIntentForPackage(
                        TERMUX_PACKAGE
                    )

            if (launchIntent != null) {

                launchIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                context.startActivity(
                    launchIntent
                )
            }

            onStarted()

        } catch (e: Exception) {

            onError(
                e.message
                    ?: "Não foi possível iniciar a instalação do Termux."
            )
        }
    }
}