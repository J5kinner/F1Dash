package com.jskinner.f1dash.data.api

import com.jskinner.f1dash.BuildConfig

actual object BuildVariant {
    actual val isDebugBuild: Boolean = BuildConfig.DEBUG
    actual val buildType: String = if (BuildConfig.DEBUG) "debug" else "release"
}