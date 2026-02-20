// Top-level build file where you can add configuration options common to all sub-projects/modules.
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    id("com.google.gms.google-services") version "4.4.4" apply false
}

val localProperties = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
}

// Store the Google API key in the rootProject extra properties
rootProject.extra["GOOGLE_API_KEY"] = localProperties.getProperty("GOOGLE_API_KEY", "")