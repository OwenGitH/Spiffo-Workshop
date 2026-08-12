package com.spiffos.workshop.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

private const val WORKSHOP_URL =
"https://steamcommunity.com/app/108600/workshop/"

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkshopScreen(
innerPadding: PaddingValues,
visible: Boolean,
onNavigationStateChanged: (
Boolean,
() -> Unit
) -> Unit
) {
val context = LocalContext.current

var workshopId by remember {
    mutableStateOf("")
}

var showDownloadDialog by remember {
    mutableStateOf(false)
}

/*
 * O alpha serve somente para esconder visualmente
 * o Workshop quando outra aba está aberta.
 *
 * O WebView continua montado e vivo.
 */
val workshopAlpha =
    if (visible) {
        1f
    } else {
        0f
    }

Box(
    modifier =
        Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .alpha(workshopAlpha)
) {

    /*
     * WEBVIEW DO WORKSHOP
     *
     * O WebView continua montado enquanto
     * a aba Workshop não for removida da composição.
     */
    AndroidView(
        modifier =
            Modifier.fillMaxSize(),

        factory = { viewContext ->

            WebView(viewContext).apply {

                settings.javaScriptEnabled =
                    true

                settings.domStorageEnabled =
                    true

                settings.allowContentAccess =
                    true

                settings.allowFileAccess =
                    true

                settings.loadsImagesAutomatically =
                    true

                settings.javaScriptCanOpenWindowsAutomatically =
                    true

                settings.setSupportMultipleWindows(
                    false
                )

                webViewClient =
                    object : WebViewClient() {

                        override fun onPageFinished(
                            view: WebView?,
                            url: String?
                        ) {
                            super.onPageFinished(
                                view,
                                url
                            )

                            val webView =
                                view ?: return

                            onNavigationStateChanged(
                                webView.canGoBack()
                            ) {
                                if (
                                    webView.canGoBack()
                                ) {
                                    webView.goBack()
                                }
                            }
                        }

                        /*
                         * Mantém o estado do histórico
                         * atualizado durante a navegação.
                         *
                         * Isso faz com que o botão voltar
                         * do celular saiba imediatamente
                         * quando o WebView possui uma página
                         * anterior.
                         */
                        override fun doUpdateVisitedHistory(
                            view: WebView?,
                            url: String?,
                            isReload: Boolean
                        ) {
                            super.doUpdateVisitedHistory(
                                view,
                                url,
                                isReload
                            )

                            val webView =
                                view ?: return

                            onNavigationStateChanged(
                                webView.canGoBack()
                            ) {
                                if (
                                    webView.canGoBack()
                                ) {
                                    webView.goBack()
                                }
                            }
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            url: String?
                        ): Boolean {
                            return false
                        }
                    }

                webChromeClient =
                    WebChromeClient()

                isFocusable =
                    true

                isFocusableInTouchMode =
                    true

                loadUrl(
                    WORKSHOP_URL
                )
            }
        },

        /*
         * Não recria nem recarrega o WebView
         * durante as recomposições.
         */
        update = {}
    )

    /*
     * BOTÃO +
     */
    FloatingActionButton(
        onClick = {
            workshopId = ""
            showDownloadDialog = true
        },

        modifier =
            Modifier
                .align(
                    Alignment.BottomEnd
                )
                .padding(
                    end = 20.dp,
                    bottom = 20.dp
                )
    ) {
        Icon(
            imageVector =
                Icons.Default.Add,

            contentDescription =
                "Adicionar ID do Workshop"
        )
    }
}

/*
 * DIÁLOGO DE DOWNLOAD
 */
if (showDownloadDialog) {

    AlertDialog(
        onDismissRequest = {
            showDownloadDialog = false
        },

        title = {
            Text("Baixar mod")
        },

        text = {
            Column(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalArrangement =
                    Arrangement.Center
            ) {

                Text(
                    text =
                        "Digite o ID do item do Steam Workshop."
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value =
                        workshopId,

                    onValueChange = { value ->

                        workshopId =
                            value.filter {
                                it.isDigit()
                            }
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    singleLine =
                        true,

                    label = {
                        Text(
                            "ID do Workshop"
                        )
                    },

                    placeholder = {
                        Text(
                            "Ex: 1234567890"
                        )
                    },

                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Number
                        )
                )
            }
        },

        confirmButton = {

            TextButton(
                onClick = {

                    val id =
                        workshopId.trim()

                    if (id.isEmpty()) {

                        Toast.makeText(
                            context,
                            "Digite um ID.",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@TextButton
                    }

                    if (id.length < 5) {

                        Toast.makeText(
                            context,
                            "ID do Workshop inválido.",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@TextButton
                    }

                    startSpiffoDownload(
                        context,
                        id
                    )

                    showDownloadDialog =
                        false

                    Toast.makeText(
                        context,
                        "Download iniciado",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            ) {
                Text("Baixar")
            }
        },

        dismissButton = {

            TextButton(
                onClick = {
                    showDownloadDialog = false
                }
            ) {
                Text("Cancelar")
            }
        }
    )
}

}

private fun startSpiffoDownload(
context: Context,
workshopId: String
) {
try {

    val intent =
        Intent(
            "com.termux.RUN_COMMAND"
        ).apply {

            setPackage(
                "com.termux"
            )

            putExtra(
                "com.termux.RUN_COMMAND_PATH",
                "/data/data/com.termux/files/home/spiffo"
            )

            putExtra(
                "com.termux.RUN_COMMAND_ARGUMENTS",
                arrayOf(workshopId)
            )

            putExtra(
                "com.termux.RUN_COMMAND_BACKGROUND",
                true
            )
        }

    context.startService(
        intent
    )

} catch (_: Exception) {

    Toast.makeText(
        context,
        "Não foi possível iniciar o Spiffo.",
        Toast.LENGTH_LONG
    ).show()
}

}