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
        versionCode = 10
        versionName = "1.8.0"

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

    // Java 11, não 1.8: as AndroidX atuais são compiladas para JVM 11, e o
    // Kotlin recusa embutir função inline de bytecode 11 num build 1.8
    // ("Cannot inline bytecode built with JVM target 11"). Não muda o minSdk.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
    // core-ktx 1.18.0 e lifecycle 2.10.0 são o teto com compileSdk 36, medido em
    // nov/2026: core 1.19 e os artefatos Compose da lifecycle 2.11 exigem 37 no
    // metadata do AAR, e o checkAarMetadata recusa o build. Sobem junto com o
    // compileSdk, depois de instalar a plataforma android-37.
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.10.0")
    implementation("androidx.activity:activity-compose:1.13.0")

    // BOM 2026.06.01 (Compose UI 1.11.4) é o último que fecha com compileSdk 36: do
    // 2026.08.00 em diante vem o Compose 1.12, que exige 37. Mesma regra das linhas
    // de cima — sobe junto com o compileSdk.
    //
    // Lifecycle e Compose UI andam casados. O collectAsStateWithLifecycle lê o
    // CompositionLocal LocalLifecycleOwner, que só o Compose UI 1.7+ fornece. Com o
    // BOM 2024.06.00 (UI 1.6.8) o app subia em debug e morria no release, no
    // primeiro frame, com "CompositionLocal LocalLifecycleOwner not present". Se
    // descer o BOM, desça a lifecycle junto.
    implementation(platform("androidx.compose:compose-bom:2026.06.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    // Só Add, Close e Star são usados, todos do core. Nas versões de 2024 o
    // material-icons-core vinha de graça pela transitiva do Material3; nas atuais
    // não vem mais, e sem a linha abaixo o import de Icons nem resolve. 1.7.8 é a
    // última publicada (a lib foi congelada), por isso versão fixa e fora do BOM.
    // O -extended empacota ~1.500 vetores; no release o R8 corta, no debug não.
    implementation("androidx.compose.material:material-icons-core:1.7.8")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.06.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
