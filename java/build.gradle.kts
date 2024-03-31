plugins {
    application
}

application {
    mainClass.set("com.bifffly.canterbury.Canterbury")
}

sourceSets.main {
    java.srcDirs("src")
}

sourceSets.test {
    java.srcDirs("tst")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    implementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    implementation("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}