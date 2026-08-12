package com.spiffos.workshop.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import coil.compose.AsyncImage
import com.spiffos.workshop.SpiffoAppState
import com.spiffos.workshop.ui.theme.PontBackground
import com.spiffos.workshop.ui.theme.PontOrange
import com.spiffos.workshop.ui.theme.PontSurface
import com.spiffos.workshop.ui.theme.PontSurfaceLight
import com.spiffos.workshop.ui.theme.PontText
import com.spiffos.workshop.ui.theme.PontTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipFile

private const val PREFS_NAME =
"spiffo_workshop_prefs"

private const val PREF_TREE_URI =
"workshop_tree_uri"

private data class WorkshopDownload(
val name: String,
val zip: DocumentFile,
val image: File?
)

@Composable
fun DownloadsScreen(
innerPadding: PaddingValues
) {
val context =
LocalContext.current

var downloads by remember {
    mutableStateOf<List<WorkshopDownload>>(
        emptyList()
    )
}

var workshopFolder by remember {
    mutableStateOf<DocumentFile?>(null)
}

var waitingForFolder by remember {
    mutableStateOf(false)
}

var knownDownloadUris by remember {
    mutableStateOf<Set<String>>(
        emptySet()
    )
}

val folderPicker =
    rememberLauncherForActivityResult(
        contract =
            ActivityResultContracts.OpenDocumentTree()
    ) { uri ->

        if (uri == null) {
            waitingForFolder = false
            return@rememberLauncherForActivityResult
        }

        try {
            context.contentResolver
                .takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
        } catch (_: Exception) {

            try {
                context.contentResolver
                    .takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
            } catch (_: Exception) {
            }
        }

        saveWorkshopTreeUri(
            context,
            uri
        )

        workshopFolder =
            try {
                DocumentFile.fromTreeUri(
                    context,
                    uri
                )
            } catch (_: Exception) {
                null
            }

        waitingForFolder = false

        knownDownloadUris =
            emptySet()

        if (workshopFolder != null) {

            Toast.makeText(
                context,
                "Pasta Spiffo Workshop conectada.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

/*
 * Recupera a pasta salva anteriormente.
 */
LaunchedEffect(Unit) {

    val savedUri =
        getSavedWorkshopTreeUri(
            context
        )

    if (savedUri != null) {

        val folder =
            try {
                DocumentFile.fromTreeUri(
                    context,
                    savedUri
                )
            } catch (_: Exception) {
                null
            }

        if (
            folder != null &&
            folder.exists() &&
            folder.isDirectory
        ) {
            workshopFolder =
                folder
        } else {

            removeSavedWorkshopTreeUri(
                context
            )

            workshopFolder =
                null
        }
    }
}

/*
 * Monitora continuamente a pasta.
 *
 * O primeiro scan apenas registra o que já existia.
 * Os scans seguintes detectam somente ZIPs novos.
 */
LaunchedEffect(workshopFolder) {

    val folder =
        workshopFolder
            ?: return@LaunchedEffect

    var firstScan = true

    while (true) {

        val result =
            withContext(Dispatchers.IO) {

                scanWorkshopFolder(
                    context,
                    folder
                )
            }

        val currentUris =
            result
                .map {
                    it.zip.uri.toString()
                }
                .toSet()

        if (firstScan) {

            knownDownloadUris =
                currentUris

            firstScan = false

        } else {

            val newDownloads =
                currentUris
                    .minus(
                        knownDownloadUris
                    )

            if (
                newDownloads.isNotEmpty()
            ) {

                newDownloads.forEach {
                    uriString ->

                    val newDownload =
                        result.firstOrNull {
                            it.zip.uri.toString() ==
                                uriString
                        }

                    if (
                        newDownload != null
                    ) {

                        SpiffoAppState
                            .notifyDownloadCompleted(
                                name =
                                    newDownload.name,

                                uri =
                                    uriString
                            )
                    }
                }
            }

            knownDownloadUris =
                currentUris
        }

        downloads =
            result

        delay(1500)
    }
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
        text = "DOWNLOADS",
        color = PontText,
        fontSize = 24.sp
    )

    Text(
        text =
            "PROJECT ZOMBOID WORKSHOP",
        color =
            PontTextSecondary,
        fontSize = 11.sp
    )

    Spacer(
        modifier =
            Modifier.height(
                16.dp
            )
    )

    /*
     * Pasta não conectada.
     */
    if (
        workshopFolder == null
    ) {

        Card(
            modifier =
                Modifier.fillMaxWidth(),

            colors =
                CardDefaults.cardColors(
                    containerColor =
                        PontSurface
                ),

            shape =
                RoundedCornerShape(
                    10.dp
                )
        ) {

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            16.dp
                        ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {

                Text(
                    text =
                        "Pasta não conectada",

                    color =
                        PontText,

                    fontSize =
                        16.sp
                )

                Text(
                    text =
                        "Selecione a pasta \"Spiffo Workshop\" " +
                            "onde o Spiffo salva os downloads.",

                    color =
                        PontTextSecondary,

                    fontSize =
                        12.sp
                )

                Button(
                    modifier =
                        Modifier.fillMaxWidth(),

                    enabled =
                        !waitingForFolder,

                    onClick = {

                        waitingForFolder =
                            true

                        folderPicker.launch(
                            null
                        )
                    }
                ) {

                    Text(
                        text =
                            if (
                                waitingForFolder
                            ) {
                                "AGUARDANDO..."
                            } else {
                                "CONECTAR PASTA"
                            }
                    )
                }
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )
    }

    /*
     * Cabeçalho da pasta conectada.
     */
    if (
        workshopFolder != null
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {

                Text(
                    text =
                        "Spiffo Workshop",

                    color =
                        PontText,

                    fontSize =
                        14.sp
                )

                Text(
                    text =
                        "${downloads.size} download(s)",

                    color =
                        PontTextSecondary,

                    fontSize =
                        10.sp
                )
            }

            OutlinedButton(
                onClick = {

                    removeSavedWorkshopTreeUri(
                        context
                    )

                    workshopFolder =
                        null

                    knownDownloadUris =
                        emptySet()
                }
            ) {

                Text(
                    "ALTERAR"
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    14.dp
                )
        )
    }

    /*
     * Nenhum download.
     */
    if (
        downloads.isEmpty()
    ) {

        Box(
            modifier =
                Modifier.fillMaxSize(),

            contentAlignment =
                Alignment.Center
        ) {

            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(
                    text =
                        if (
                            workshopFolder == null
                        ) {
                            "Conecte a pasta Spiffo Workshop."
                        } else {
                            "Nenhum download encontrado."
                        },

                    color =
                        PontTextSecondary
                )

                if (
                    workshopFolder != null
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(
                                6.dp
                            )
                    )

                    Text(
                        text =
                            "Copie um ID numérico do Workshop " +
                                "para iniciar o download.",

                        color =
                            PontTextSecondary,

                        fontSize =
                            11.sp
                    )
                }
            }
        }

    } else {

        /*
         * Lista de downloads.
         */
        LazyColumn(
            modifier =
                Modifier.fillMaxSize(),

            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {

            items(
                items =
                    downloads,

                key = {
                    it.zip.uri.toString()
                }
            ) { download ->

                DownloadCard(
                    download
                )
            }
        }
    }
}

}

@Composable
private fun DownloadCard(
download: WorkshopDownload
) {

Card(
    modifier =
        Modifier.fillMaxWidth(),

    colors =
        CardDefaults.cardColors(
            containerColor =
                PontSurface
        ),

    shape =
        RoundedCornerShape(
            8.dp
        )
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    10.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        if (
            download.image != null
        ) {

            AsyncImage(
                model =
                    download.image,

                contentDescription =
                    download.name,

                modifier =
                    Modifier
                        .size(
                            82.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                6.dp
                            )
                        )
                        .background(
                            PontSurfaceLight
                        ),

                contentScale =
                    ContentScale.Crop
            )

        } else {

            Box(
                modifier =
                    Modifier
                        .size(
                            82.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                6.dp
                            )
                        )
                        .background(
                            PontSurfaceLight
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        "MOD",

                    color =
                        PontTextSecondary,

                    fontSize =
                        11.sp
                )
            }
        }

        Spacer(
            modifier =
                Modifier.width(
                    12.dp
                )
        )

        Column(
            modifier =
                Modifier.weight(
                    1f
                )
        ) {

            Text(
                text =
                    download.name,

                color =
                    PontText,

                fontSize =
                    15.sp
            )

            Spacer(
                modifier =
                    Modifier.height(
                        4.dp
                    )
            )

            Text(
                text =
                    "ZIP • Download concluído",

                color =
                    PontTextSecondary,

                fontSize =
                    10.sp
            )

            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )

            /*
             * Barra de progresso.
             *
             * Como esta lista representa somente arquivos
             * ZIP já encontrados na pasta, eles estão em 100%.
             */
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            6.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                3.dp
                            )
                        )
                        .background(
                            PontSurfaceLight
                        )
            ) {

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(
                                6.dp
                            )
                            .clip(
                                RoundedCornerShape(
                                    3.dp
                                )
                            )
                            .background(
                                PontOrange
                            )
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        5.dp
                    )
            )

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text =
                        "Download concluído",

                    color =
                        PontOrange,

                    fontSize =
                        10.sp
                )

                Spacer(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )

                Text(
                    text =
                        "100%",

                    color =
                        PontTextSecondary,

                    fontSize =
                        10.sp
                )
            }
        }
    }
}

}

private fun scanWorkshopFolder(
context: Context,
folder: DocumentFile
): List<WorkshopDownload> {

if (
    !folder.exists() ||
    !folder.isDirectory
) {
    return emptyList()
}

val files =
    try {
        folder.listFiles()
    } catch (_: Exception) {
        return emptyList()
    }

return files
    .filter {
        it.isFile &&
            isZipFile(
                it.name
            )
    }
    .map { zip ->

        val baseName =
            removeExtension(
                zip.name ?: ""
            )

        WorkshopDownload(
            name =
                baseName,

            zip =
                zip,

            image =
                extractWorkshopImage(
                    context,
                    zip,
                    baseName
                )
        )
    }
    .sortedBy {
        it.name.lowercase()
    }

}

private fun extractWorkshopImage(
context: Context,
zipDocument: DocumentFile,
baseName: String
): File? {

val imageDirectory =
    File(
        context.filesDir,
        "workshop_images"
    )

if (
    !imageDirectory.exists()
) {
    imageDirectory.mkdirs()
}

/*
 * Se a imagem já foi extraída anteriormente,
 * reutiliza o arquivo.
 */
val existing =
    imageDirectory
        .listFiles()
        ?.firstOrNull {
            it.nameWithoutExtension ==
                baseName &&
                it.length() > 0
        }

if (
    existing != null
) {
    return existing
}

val temporaryZip =
    try {
        File.createTempFile(
            "spiffo_",
            ".zip",
            context.cacheDir
        )
    } catch (_: Exception) {
        return null
    }

try {

    /*
     * Copia o DocumentFile para um ZIP temporário
     * porque ZipFile trabalha com File.
     */
    val copied =
        context.contentResolver
            .openInputStream(
                zipDocument.uri
            )
            ?.use { input ->

                temporaryZip
                    .outputStream()
                    .use { output ->

                        input.copyTo(
                            output
                        )
                    }

                true
            }
            ?: false

    if (!copied) {
        return null
    }

    ZipFile(
        temporaryZip
    ).use { zip ->

        val entries =
            zip.entries()
                .asSequence()
                .filter {
                    !it.isDirectory &&
                        isImageFile(
                            it.name
                        )
                }
                .toList()

        /*
         * Primeiro tenta encontrar poster/preview.
         * Caso não exista, usa a primeira imagem encontrada.
         */
        val imageEntry =
            entries.firstOrNull {
                isPreferredImage(
                    it.name
                )
            }
                ?: entries.firstOrNull()

        if (
            imageEntry == null
        ) {
            return null
        }

        val extension =
            getImageExtension(
                imageEntry.name
            )

        val outputFile =
            File(
                imageDirectory,
                "$baseName$extension"
            )

        zip.getInputStream(
            imageEntry
        ).use { input ->

            outputFile
                .outputStream()
                .use { output ->

                    input.copyTo(
                        output
                    )
                }
        }

        return if (
            outputFile.exists() &&
            outputFile.length() > 0
        ) {
            outputFile
        } else {
            null
        }
    }

} catch (_: Exception) {

    return null

} finally {

    temporaryZip.delete()
}

}

private fun isPreferredImage(
name: String
): Boolean {

val value =
    name.lowercase()

return value.contains(
    "poster"
) ||
    value.contains(
        "preview"
    )

}

private fun getImageExtension(
name: String
): String {

val value =
    name.lowercase()

return when {

    value.endsWith(
        ".jpeg"
    ) ->
        ".jpeg"

    value.endsWith(
        ".jpg"
    ) ->
        ".jpg"

    value.endsWith(
        ".png"
    ) ->
        ".png"

    value.endsWith(
        ".webp"
    ) ->
        ".webp"

    else ->
        ".jpg"
}

}

private fun isZipFile(
name: String?
): Boolean {

return name
    ?.lowercase()
    ?.endsWith(
        ".zip"
    )
    ?: false

}

private fun isImageFile(
name: String?
): Boolean {

val value =
    name
        ?.lowercase()
        ?: return false

return value.endsWith(
    ".jpg"
) ||
    value.endsWith(
        ".jpeg"
    ) ||
    value.endsWith(
        ".png"
    ) ||
    value.endsWith(
        ".webp"
    )

}

private fun removeExtension(
name: String
): String {

val index =
    name.lastIndexOf(
        '.'
    )

return if (
    index > 0
) {
    name.substring(
        0,
        index
    )
} else {
    name
}

}

private fun saveWorkshopTreeUri(
context: Context,
uri: Uri
) {

context
    .getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    .edit()
    .putString(
        PREF_TREE_URI,
        uri.toString()
    )
    .apply()

}

private fun getSavedWorkshopTreeUri(
context: Context
): Uri? {

val value =
    context
        .getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        .getString(
            PREF_TREE_URI,
            null
        )
        ?: return null

return try {
    Uri.parse(
        value
    )
} catch (_: Exception) {
    null
}

}

private fun removeSavedWorkshopTreeUri(
context: Context
) {

val prefs =
    context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

val uriString =
    prefs.getString(
        PREF_TREE_URI,
        null
    )

if (
    uriString != null
) {

    try {

        val uri =
            Uri.parse(
                uriString
            )

        context.contentResolver
            .releasePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

    } catch (_: Exception) {
    }
}

prefs
    .edit()
    .remove(
        PREF_TREE_URI
    )
    .apply()

}