plugins {
    war
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    providedCompile(libs.jakarta)
    providedCompile(libs.resteasy.core)
    providedCompile(libs.resteasy.jackson2)
    providedCompile(libs.hibernate.core)
    providedCompile(libs.bundles.hibernate.search)
    implementation(libs.bundles.jackson)
    implementation(libs.commons.lang3)
    implementation(libs.postgres)
    implementation(libs.jdom2)
    implementation(libs.intellij.annotations)
}
