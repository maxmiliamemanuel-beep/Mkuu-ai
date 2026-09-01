#!/bin/sh
# Lightweight local launcher; CI/Android Studio may use its installed Gradle.
exec gradle "$@"
