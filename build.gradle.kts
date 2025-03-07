plugins {
    id("java")
    id("me.champeau.jmh") version("0.6.8")
}

group = "dev.schakr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Vavr
    implementation("io.vavr:vavr:0.10.6")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
    testCompileOnly("org.projectlombok:lombok:1.18.36")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.36")

    // JUnit
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // JMH
    testImplementation("org.openjdk.jmh:jmh-core:1.36")
    testAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.36")

}

tasks.test {
    useJUnitPlatform()
}