package com.spiffos.workshop.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.spiffos.workshop.ConfigurationManager
import com.spiffos.workshop.Termux.TermuxCommand
import com.spiffos.workshop.ui.theme.PontBackground
import com.spiffos.workshop.ui.theme.PontOrange
import com.spiffos.workshop.ui.theme.PontSurface
import com.spiffos.workshop.ui.theme.PontText
import com.spiffos.workshop.ui.theme.PontTextSecondary

@Composable
fun ConfigurationScreen(
    innerPadding: PaddingValues
) {

    val context =
        LocalContext.current

    val manager =
        remember {
            ConfigurationManager(
                context.applicationContext
            )
        }

    /*
     * Único detector da tela:
     * verifica somente se o Termux está instalado.
     */
    var termuxInstalled by remember {
        mutableStateOf(
            manager.isTermuxInstalled()
        )
    }

    /*
     * Controla somente o envio do Installer.
     *
     * Não existe polling.
     * Não existe while.
     * Não existe test().
     */
    var installing by remember {
        mutableStateOf(false)
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    PontBackground
                )
                .padding(
                    innerPadding
                )
                .padding(
                    16.dp
                )
    ) {

        Text(
            text =
                "CONFIGURAÇÃO",

            color =
                PontText,

            fontSize =
                24.sp
        )

        Spacer(
            modifier =
                Modifier.height(
                    20.dp
                )
        )

        /*
         * DETECTOR DO TERMUX
         *
         * Card visual para mostrar apenas
         * se o Termux está instalado.
         */
        TermuxStatusCard(
            installed =
                termuxInstalled
        )

        Spacer(
            modifier =
                Modifier.height(
                    24.dp
                )
        )

        /*
         * SPIFFO INSTALLER
         *
         * Executa o instalador somente quando
         * o usuário toca no botão.
         */
        Button(
            modifier =
                Modifier.fillMaxWidth(),

            enabled =
                termuxInstalled &&
                    !installing,

            onClick = {

                /*
                 * Atualiza somente o detector
                 * do Termux.
                 */
                termuxInstalled =
                    manager.isTermuxInstalled()

                if (!termuxInstalled) {
                    return@Button
                }

                installing =
                    true

                TermuxCommand.install(

                    context =
                        context,

                    onStarted = {

                        /*
                         * Apenas libera o botão.
                         *
                         * Não executa outro comando.
                         * Não verifica configuração.
                         */
                        installing =
                            false
                    },

                    onError = {

                        installing =
                            false
                    }
                )
            }
        ) {

            if (installing) {

                CircularProgressIndicator(
                    modifier =
                        Modifier.padding(
                            end = 8.dp
                        ),

                    color =
                        Color.White,

                    strokeWidth =
                        2.dp
                )

                Text(
                    text =
                        "INICIANDO..."
                )

            } else {

                Text(
                    text =
                        "SPIFFO INSTALLER"
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    18.dp
                )
        )

        /*
         * PERMISSÕES
         *
         * Abre a página de informações do
         * próprio Spiffo Workshop no Android.
         *
         * O botão NÃO executa comandos no Termux.
         */
        Button(
            modifier =
                Modifier.fillMaxWidth(),

            onClick = {

                openAppPermissions(
                    context
                )
            }
        ) {

            Text(
                text =
                    "PERMISSÕES"
            )
        }

        Spacer(
            modifier =
                Modifier.height(
                    6.dp
                )
        )

        Text(
            text =
                "Permite que o app execute comandos no ambiente Termux",

            color =
                PontTextSecondary,

            fontSize =
                12.sp
        )
    }
}

/*
 * Card do detector do Termux.
 */
@Composable
private fun TermuxStatusCard(
    installed: Boolean
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                8.dp
            ),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    PontSurface
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        14.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {

            Box(
                modifier =
                    Modifier
                        .background(
                            color =
                                if (installed) {
                                    Color(0xFF4CAF50)
                                } else {
                                    PontOrange
                                },

                            shape =
                                RoundedCornerShape(
                                    50
                                )
                        )
                        .padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        )
            ) {

                Text(
                    text =
                        if (installed) {
                            "✓"
                        } else {
                            "!"
                        },

                    color =
                        Color.White,

                    fontSize =
                        13.sp
                )
            }

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {

                Text(
                    text =
                        "Termux",

                    color =
                        PontText,

                    fontSize =
                        15.sp
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            2.dp
                        )
                )

                Text(
                    text =
                        if (installed) {
                            "Termux foi encontrado no dispositivo."
                        } else {
                            "Termux não foi encontrado no dispositivo."
                        },

                    color =
                        PontTextSecondary,

                    fontSize =
                        11.sp
                )
            }
        }
    }
}

/*
 * Abre a página de informações do aplicativo.
 *
 * Caminho esperado:
 *
 * Configurações
 *    ↓
 * Apps
 *    ↓
 * Spiffo Workshop
 *    ↓
 * Permissões
 */
private fun openAppPermissions(
    context: Context
) {

    try {

        val intent =
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            ).apply {

                data =
                    Uri.parse(
                        "package:${context.packageName}"
                    )

                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
            }

        context.startActivity(
            intent
        )

    } catch (_: Exception) {
    }
}