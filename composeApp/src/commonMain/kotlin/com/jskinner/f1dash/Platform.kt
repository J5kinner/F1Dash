package com.jskinner.f1dash

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform