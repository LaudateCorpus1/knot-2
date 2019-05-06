plugins {
    kotlin("jvm")
    `maven-publish`
    id("org.gradle.signing")
    id("org.jetbrains.dokka") version Dep.Version.dokka
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = Dep.Version.jvmTarget
}

dependencies {
    implementation(project(":knot"))
    implementation(kotlin(Dep.kotlinJdk))
    implementation(Dep.rxJava)

    testImplementation(Dep.junit)
    testImplementation(Dep.truth)
    testImplementation(Dep.mockito)
    testImplementation(Dep.mockitoKotlin)
}

publishing {

    repositories {
        maven {
            name = "local"
            url = uri("$buildDir/repository")
        }
        maven {
            name = Pom.MavenCentral.name
            url = uri(Pom.MavenCentral.url)
            credentials {
                username = project.getNexusUser()
                password = project.getNexusPassword()
            }
        }
    }

    val dokka by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class) {
        outputFormat = "javadoc"
        outputDirectory = "$buildDir/javadoc"
    }

    val sourcesJar by tasks.creating(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    val javadocJar by tasks.creating(Jar::class) {
        archiveClassifier.set("javadoc")
        from(dokka)
    }

    publications {
        create("Composition", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)
            pom {
                name.set("Composition")
                description.set("Composition extension for Knot")
                url.set(Pom.url)
                licenses {
                    license {
                        name.set(Pom.License.name)
                        url.set(Pom.License.url)
                    }
                }
                developers {
                    developer {
                        id.set(Pom.Developer.id)
                        name.set(Pom.Developer.name)
                        email.set(Pom.Developer.email)
                    }
                }
                scm {
                    connection.set(Pom.Github.url)
                    developerConnection.set(Pom.Github.cloneUrl)
                    url.set(Pom.Github.url)
                }
            }
        }
    }

}

if (project.hasSigningKey()) {
    signing {
        sign(publishing.publications["Composition"])
    }
}