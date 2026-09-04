import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Credenciais da chave de upload. Ficam em local.properties, que não vai para o
// git — o .jks/.p12 mora fora do repositório. Quem não tem a chave (a CI, por
// exemplo) continua compilando: sem as propriedades, o release sai sem
// signingConfig, exatamente como era antes.
val chave = Properties().apply {
    val arquivo = rootProject.file("local.properties")
    if (arquivo.exists()) arquivo.inputStream().use { load(it) }
}
val temChaveDeUpload = chave.getProperty("cse.storeFile") != null

android {
    namespace = "com.cse.calculadora"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cse.calculadora"
        minSdk = 24
        targetSdk = 36

        // Configurações globais de versão do app
        versionCode = 7
        versionName = "1.5.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        if (temChaveDeUpload) {
            create("upload") {
                storeFile = file(chave.getProperty("cse.storeFile"))
                storePassword = chave.getProperty("cse.storePassword")
                keyAlias = chave.getProperty("cse.keyAlias") ?: "cse_key"
                keyPassword = chave.getProperty("cse.keyPassword")
            }
        }
    }

    buildTypes {
        release {
            if (temChaveDeUpload) {
                signingConfig = signingConfigs.getByName("upload")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    // BOM travado em 2024.09.03 (Compose UI 1.7.3) por necessidade, não por gosto.
    // O collectAsStateWithLifecycle da lifecycle 2.8.4 lê o CompositionLocal
    // androidx.lifecycle.compose.LocalLifecycleOwner, e quem fornece esse local é
    // o Compose UI a partir da 1.7. Com o BOM 2024.06.00 (UI 1.6.8) o app subia em
    // debug e morria no release, no primeiro frame, com "CompositionLocal
    // LocalLifecycleOwner not present". Se descer o BOM, desça a lifecycle junto.
    implementation(platform("androidx.compose:compose-bom:2024.09.03"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    // Só Add e Close são usados, e os dois estão no core que o Material3 já traz.
    // O -extended empacota ~1.500 vetores; no release o R8 corta, no debug não.

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.03"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
