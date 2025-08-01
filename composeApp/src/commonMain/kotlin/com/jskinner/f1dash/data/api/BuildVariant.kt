package com.jskinner.f1dash.data.api

expect object BuildVariant {
    val isDebugBuild: Boolean
    val buildType: String
}