#!/bin/sh
# Gradle startup script
exec java -Dorg.gradle.appname=gradlew -classpath "$0/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
