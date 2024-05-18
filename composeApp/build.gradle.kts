import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(project.projectDir.path)
                    }
                }
            }
        }
        binaries.executable()
    }
    
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting
        
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation("com.mohamedrejeb.richeditor:richeditor-compose:1.0.0-rc04")
            implementation("com.eygraber:compose-color-picker:0.0.19")
            implementation("io.ktor:ktor-client-core:3.0.0-wasm2")
            implementation("io.coil-kt.coil3:coil-network-ktor:3.0.0-alpha06")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.3.8")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation("io.ktor:ktor-client-android:3.0.0-beta-1")
        }
    }
}


compose.desktop {
    application {
        mainClass = "com.momo.cardmaker.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.momo.cardmaker"
            packageVersion = "1.0.0"
        }
    }
}

compose.experimental {
    web.application {}
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "com.momo.cardmaker.MainKt"
    }
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    from(kotlin.targets["desktop"].compilations["main"].output.allOutputs)
    configurations = listOf(project.configurations.getByName("desktopRuntimeClasspath"))
}