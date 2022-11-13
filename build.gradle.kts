import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("org.jetbrains.dokka") version "1.6.20"
    kotlin("multiplatform") version "1.6.20"
    kotlin("kapt") version "1.6.20"
    kotlin("plugin.serialization") version "1.6.20"
}

group = "com.jeffpdavidson.kotwords"
version = "1.2.14-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        withJava()

        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    js(IR) {
        browser {}
        binaries.executable()
    }

    @Suppress("UNUSED_VARIABLE") // https://youtrack.jetbrains.com/issue/KT-38871
    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.RequiresOptIn")
                optIn("kotlin.js.ExperimentalJsExport")
                optIn("com.jeffpdavidson.kotwords.KotwordsInternal")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            }
        }

        val commonMain by getting {
            dependencies {
                implementation("com.squareup.okio:okio:3.1.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
                implementation("net.mamoe.yamlkt:yamlkt:0.12.0")
                implementation("io.github.pdvrieze.xmlutil:serialization:0.84.2")
                implementation("com.github.ajalt.colormath:colormath:3.2.0")

                // TODO: Migrate to kotlinx-datetime if parsing/formatting support is added.
                implementation("com.soywiz.korlibs.klock:klock:2.7.0")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.1")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("org.apache.pdfbox:pdfbox:2.0.26")
                implementation("org.glassfish:javax.json:1.1.4")
                implementation("org.jsoup:jsoup:1.15.1")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
                implementation("org.jetbrains.kotlin:kotlin-test-junit")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(npm("jszip", "3.10.0"))
                implementation(npm("jspdf", "2.5.1"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.6.1")
                implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.5")
            }
        }

        val jsTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-js")
                implementation(npm("pdfjs-dist", "2.14.305"))
            }
        }
    }
}

tasks {
    // Omit .web package from documentation
    dokkaHtml.configure {
        dokkaSourceSets {
            configureEach {
                perPackageOption {
                    matchingRegex.set("""com.jeffpdavidson\.kotwords\.web.*""")
                    suppress.set(true)
                }
            }
        }
    }

    val browserProductionWebpackTask = getByName("jsBrowserProductionWebpack", KotlinWebpack::class)

    @Suppress("UNUSED_VARIABLE") // https://youtrack.jetbrains.com/issue/KT-38871
    val browserDistributionZip by creating(Zip::class) {
        dependsOn(browserProductionWebpackTask)
        from (browserProductionWebpackTask.destinationDirectory)
        destinationDirectory.set(file("${buildDir}/zip"))
        archiveAppendix.set("browser-distribution")
    }

    assemble {
        dependsOn(browserDistributionZip)
    }
}

val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
}

publishing {
    publications.withType<MavenPublication> {
        artifact(dokkaJar)
        pom {
            name.set("Kotwords")
            description.set("Collection of crossword puzzle file format converters and other utilities, written in Kotlin.")
            url.set("https://jpd236.github.io/kotwords")
            developers {
                developer {
                    id.set("jpd236")
                    name.set("Jeff Davidson")
                }
            }
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            scm {
                connection.set("scm:git:git://github.com/jpd236/kotwords.git")
                developerConnection.set("scm:git:ssh://git@github.com/jpd236/kotwords.git")
                url.set("https://github.com/jpd236/kotwords")
            }
        }
    }
}

if (System.getenv("PGP_KEY_ID") != null) {
    signing {
        useInMemoryPgpKeys(System.getenv("PGP_KEY_ID"), System.getenv("PGP_KEY"), System.getenv("PGP_PASSPHRASE"))
        sign(publishing.publications)
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("OSSRH_DEPLOY_USERNAME"))
            password.set(System.getenv("OSSRH_DEPLOY_PASSWORD"))
        }
    }
}
