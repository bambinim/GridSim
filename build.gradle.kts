plugins {
    id("com.dorongold.task-tree") version "4.0.1"
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("ch.epfl.scala:gradle-bloop_2.12:1.6.4")
    }
}

allprojects {
    apply(plugin = "bloop")
}
