#!/bin/bash
cd "$(dirname "$0")"
echo "Building server..."
../gradlew :server:jvmJar
echo "Starting F1Dash Mock Server on http://localhost:3000"
java -cp "build/libs/server-jvm.jar:$(../gradlew :server:printClasspath -q)" com.jskinner.f1dash.server.ServerKt