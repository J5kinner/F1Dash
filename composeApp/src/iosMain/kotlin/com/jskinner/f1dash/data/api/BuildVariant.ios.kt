package com.jskinner.f1dash.data.api

import platform.Foundation.NSBundle

actual object BuildVariant {
    actual val isDebugBuild: Boolean by lazy {
        val bundle = NSBundle.mainBundle
        val bundleId = bundle.bundleIdentifier ?: ""
        val executablePath = bundle.executablePath ?: ""

        val isDebugBuild = bundleId.contains("debug", ignoreCase = true) ||
                executablePath.contains("Debug-", ignoreCase = false) ||
                executablePath.contains("/Debug/", ignoreCase = false) ||
                executablePath.contains("DerivedData", ignoreCase = false) ||
                bundleId.endsWith(".debug", ignoreCase = true) ||
                executablePath.contains("Build/Products/Debug", ignoreCase = false)

        println("🍎 iOS BuildVariant Detection:")
        println("   Bundle ID: $bundleId")
        println("   Executable Path: $executablePath")
        println("   Is Debug Build: $isDebugBuild")

        isDebugBuild
    }

    actual val buildType: String by lazy {
        if (isDebugBuild) "debug" else "release"
    }
}