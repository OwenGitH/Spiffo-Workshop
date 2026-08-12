@file:OptIn(
androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.spiffos.workshop.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.zIndex
import com.spiffos.workshop.ui.theme.PontBackground
import com.spiffos.workshop.ui.theme.PontSurface

@Composable
fun SpiffoApp(
onExitApp: () -> Unit
) {
val context = LocalContext.current

var selectedTab by remember {
    mutableStateOf(0)
}

var workshopCanGoBack by remember {
    mutableStateOf(false)
}

var workshopGoBack by remember {
    mutableStateOf<(() -> Unit)?>(null)
}

var lastBackPressTime by remember {
    mutableLongStateOf(0L)
}

BackHandler {
    /*
     * Downloads ou Configuração:
     * sempre retorna para o Workshop.
     */
    if (selectedTab != 0) {
        selectedTab = 0
        return@BackHandler
    }

    /*
     * Se o Workshop tiver histórico,
     * navega para a página anterior.
     */
    if (workshopCanGoBack) {
        workshopGoBack?.invoke()
        return@BackHandler
    }

    /*
     * Caso contrário, dois toques para sair.
     */
    val currentTime =
        System.currentTimeMillis()

    if (
        currentTime - lastBackPressTime < 2000L
    ) {
        onExitApp()
    } else {
        lastBackPressTime = currentTime

        Toast.makeText(
            context,
            "Pressione voltar novamente para sair",
            Toast.LENGTH_SHORT
        ).show()
    }
}

Scaffold(
    containerColor = PontBackground,

    bottomBar = {
        NavigationBar(
            containerColor = PontSurface
        ) {
            NavigationBarItem(
                selected = selectedTab == 0,

                onClick = {
                    selectedTab = 0
                },

                icon = {
                    Icon(
                        imageVector =
                            Icons.Default.ShoppingCart,

                        contentDescription =
                            "Workshop"
                    )
                },

                label = {
                    Text("Workshop")
                }
            )

            NavigationBarItem(
                selected = selectedTab == 1,

                onClick = {
                    selectedTab = 1
                },

                icon = {
                    Text("↓")
                },

                label = {
                    Text("Downloads")
                }
            )

            NavigationBarItem(
                selected = selectedTab == 2,

                onClick = {
                    selectedTab = 2
                },

                icon = {
                    Icon(
                        imageVector =
                            Icons.Default.Settings,

                        contentDescription =
                            "Configuração"
                    )
                },

                label = {
                    Text("Configuração")
                }
            )
        }
    }
) { innerPadding ->

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    PontBackground
                )
    ) {

        /*
         * =====================================================
         * WORKSHOP
         * =====================================================
         *
         * O WorkshopScreen fica SEMPRE montado.
         *
         * Portanto o WebView não é destruído quando o usuário
         * entra em Downloads ou Configuração.
         *
         * Não usamos "visible" no WorkshopScreen.
         */
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .alpha(
                        if (selectedTab == 0) {
                            1f
                        } else {
                            0f
                        }
                    )
                    .zIndex(
                        if (selectedTab == 0) {
                            2f
                        } else {
                            0f
                        }
                    )
        ) {
            WorkshopScreen(
                innerPadding =
                    innerPadding,

                visible =
                    selectedTab == 0,

                onNavigationStateChanged = {
                    canGoBack,
                    goBack ->

                    workshopCanGoBack =
                        canGoBack

                    workshopGoBack =
                        goBack
                }
            )
        }

        /*
         * =====================================================
         * DOWNLOADS
         * =====================================================
         *
         * O DownloadsScreen também fica SEMPRE montado.
         *
         * Isso é o que elimina o problema:
         *
         * antes:
         *
         * Downloads -> destruído
         * volta     -> downloads = emptyList()
         *            -> scan
         *            -> arquivos aparecem
         *
         * agora:
         *
         * Downloads -> apenas fica invisível
         * volta     -> mesma instância
         *            -> mesma lista
         *            -> arquivos continuam imediatamente
         */
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .alpha(
                        if (selectedTab == 1) {
                            1f
                        } else {
                            0f
                        }
                    )
                    .zIndex(
                        if (selectedTab == 1) {
                            2f
                        } else {
                            0f
                        }
                    )
        ) {
            DownloadsScreen(
                innerPadding =
                    innerPadding
            )
        }

        /*
         * =====================================================
         * CONFIGURAÇÃO
         * =====================================================
         *
         * Também permanece montada enquanto a aba está
         * apenas invisível.
         */
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .alpha(
                        if (selectedTab == 2) {
                            1f
                        } else {
                            0f
                        }
                    )
                    .zIndex(
                        if (selectedTab == 2) {
                            2f
                        } else {
                            0f
                        }
                    )
        ) {
            ConfigurationScreen(
                innerPadding =
                    innerPadding
            )
        }
    }
}

}