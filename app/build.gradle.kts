import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

val keystorePropsFile = rootProject.file("release.properties")
val keystoreProps = Properties()

if (keystorePropsFile.exists()) {
    FileInputStream(keystorePropsFile).use {
        keystoreProps.load(it)
    }
}

val hasValidSigningProps = keystorePropsFile.exists().also { exists ->
    if (exists) {
        FileInputStream(keystorePropsFile).use {
            keystoreProps.load(it)
        }
    }
}.let {
    listOf(
        "storeFile",
        "storePassword",
        "keyAlias",
        "keyPassword"
    ).all { key ->
        keystoreProps[key] != null
    }
}

android {
    namespace = "com.spiffos.workshop"
    compileSdk = 36

    lint {
        checkReleaseBuilds = false
    }

    signingConfigs {
        if (hasValidSigningProps) {
            create("release") {
                storeFile = rootProject.file(
                    keystoreProps["storeFile"] as String
                )
                storePassword = keystoreProps["storePassword"] as String
                keyAlias = keystoreProps["keyAlias"] as String
                keyPassword = keystoreProps["keyPassword"] as String
            }
        }
    }

    defaultConfig {
        applicationId = "com.spiffos.workshop"
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            if (hasValidSigningProps) {
                signingConfig = signingConfigs.getByName("release")
            }

            isMinifyEnabled = true

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    packaging {
        resources {
            resources.excludes.add(
                "/META-INF/{AL2.0,LGPL2.1}"
            )

            resources.excludes.add(
                "META-INF/kotlinx_coroutines_core.version"
            )

            resources.pickFirsts.add(
                "nonJvmMain/default/linkdata/package_androidx/0_androidx.knm"
            )

            resources.pickFirsts.add(
                "nonJvmMain/default/linkdata/root_package/0_.knm"
            )

            resources.pickFirsts.add(
                "nonJvmMain/default/linkdata/module"
            )

            resources.pickFirsts.add(
                "nativeMain/default/linkdata/root_package/0_.knm"
            )

            resources.pickFirsts.add(
                "nativeMain/default/linkdata/module"
            )

            resources.pickFirsts.add(
                "commonMain/default/linkdata/root_package/0_.knm"
            )

            resources.pickFirsts.add(
                "commonMain/default/linkdata/module"
            )

            resources.pickFirsts.add(
                "commonMain/default/linkdata/package_androidx/0_androidx.knm"
            )

            resources.pickFirsts.add(
                "META-INF/kotlin-project-structure-metadata.json"
            )

            resources.merges.add(
                "commonMain/default/manifest"
            )

            resources.merges.add(
                "nonJvmMain/default/manifest"
            )

            resources.merges.add(
                "nativeMain/default/manifest"
            )
        }
    }

    configurations.all {
        resolutionStrategy {
            force(
                "org.jetbrains.kotlin:kotlin-stdlib:1.9.22"
            )

            force(
                "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.22"
            )

            force(
                "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.22"
            )

            force(
                "androidx.collection:collection:1.4.2"
            )

            force(
                "androidx.collection:collection-ktx:1.4.2"
            )

            force(
                "androidx.annotation:annotation:1.8.1"
            )

            force(
                "androidx.core:core-ktx:1.8.0"
            )

            /*
             * Lifecycle alinhado em 2.7.0.
             *
             * Não usar 2.3.1 aqui, pois outras dependências
             * trazem lifecycle-common 2.7.0.
             */
            force(
                "androidx.lifecycle:lifecycle-common:2.7.0"
            )

            force(
                "androidx.lifecycle:lifecycle-runtime:2.7.0"
            )

            force(
                "androidx.lifecycle:lifecycle-runtime-ktx:2.7.0"
            )
        }
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:deprecation")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "17"
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.collection.ktx)
    implementation(libs.androidx.material3)
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("androidx.compose.material:material-icons-extended")

    /*
     * Coil
     *
     * Necessário para AsyncImage no DownloadsScreen.
     */
    implementation("io.coil-kt:coil-compose:2.7.0")

    implementation(libs.kotlin.stdlib) {
        exclude(
            group = "org.jetbrains.kotlin",
            module = "kotlin-stdlib-jdk7"
        )

        exclude(
            group = "org.jetbrains.kotlin",
            module = "kotlin-stdlib-jdk8"
        )
    }

    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.androidx.ui.tooling)
}