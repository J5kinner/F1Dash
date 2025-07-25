package com.jskinner.f1dash.di

import com.jskinner.f1dash.di.modules.appModules
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(appModules)
}

fun initKoin() = initKoin {}