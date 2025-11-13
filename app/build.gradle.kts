plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.proyectoZapateria"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.proyecto_zapateria"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        compose = true
        buildConfig = true // habilitar build config en gradle
    }

    buildTypes {
        // Bloque para desarrollo (cuando ejecutas en el emulador/teléfono)
        getByName("debug") {
            // URLs para desarrollo (usando IP de emulador 10.0.2.2)
            // (Si usas teléfono físico, cambia 10.0.2.2 por tu IP de WiFi, ej: "192.168.1.100")
            buildConfigField("String", "BASE_URL_GEOGRAFIA",  "\"http://10.0.2.2:8081/\"")
            buildConfigField("String", "BASE_URL_INVENTARIO", "\"http://10.0.2.2:8082/\"")
            buildConfigField("String", "BASE_URL_USUARIOS",   "\"http://10.0.2.2:8083/\"")

            // --- Puertos por Confirmar ---
            buildConfigField("String", "BASE_URL_PRODUCTOS",  "\"http://10.0.2.2:8084/\"")
            buildConfigField("String", "BASE_URL_VENTAS",     "\"http://10.0.2.2:8085/\"")
            buildConfigField("String", "BASE_URL_ENTREGAS",   "\"http://10.0.2.2:8086/\"")
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // URLs para producción (mismo que debug por ahora)
            buildConfigField("String", "BASE_URL_GEOGRAFIA",  "\"http://10.0.2.2:8081/\"")
            buildConfigField("String", "BASE_URL_INVENTARIO", "\"http://10.0.2.2:8082/\"")
            buildConfigField("String", "BASE_URL_USUARIOS",   "\"http://10.0.2.2:8083/\"")
            buildConfigField("String", "BASE_URL_PRODUCTOS",  "\"http://10.0.2.2:8084/\"")
            buildConfigField("String", "BASE_URL_VENTAS",     "\"http://10.0.2.2:8085/\"")
            buildConfigField("String", "BASE_URL_ENTREGAS",   "\"http://10.0.2.2:8086/\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)


    //librerias nuevas
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    // Material icons (necesarios para Visibility / VisibilityOff)
    implementation("androidx.compose.material:material-icons-extended")
    // Room (SQLite) - runtime y extensiones KTX
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    // Compilador de Room vía KSP
    ksp("androidx.room:room-compiler:2.6.1")
    // Hash para contraseñas
    implementation("org.mindrot:jbcrypt:0.4")

    // DataStore - Persistencia de datos (reemplazo de SharedPreferences)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coil - Carga de imágenes en Compose
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Accompanist - Permisos en Compose
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // Dependencias de Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler.ksp)

    // Para inyectar ViewModels en Compose
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // ==== AGREGADOS PARA REST ====
    // Retrofit base
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    // Convertidor JSON con Gson
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    // OkHttp y logging interceptor
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

}