package com.jskinner.f1dash.di.modules

import com.jskinner.f1dash.data.api.F1DriverApi
import com.jskinner.f1dash.data.api.F1SessionApi
import com.jskinner.f1dash.data.api.OpenF1Api
import com.jskinner.f1dash.data.cache.ResponseCache
import com.jskinner.f1dash.data.cache.ResponseCacheImpl
import com.jskinner.f1dash.data.repository.CachedF1RepositoryImpl
import com.jskinner.f1dash.domain.repository.F1Repository
import com.jskinner.f1dash.presentation.viewmodels.F1PreviousRacesViewModel
import com.jskinner.f1dash.presentation.viewmodels.F1RaceViewModel
import com.jskinner.f1dash.presentation.viewmodels.F1ReplayViewModel
import org.koin.dsl.module

val f1Module = module {
    single<F1DriverApi> { F1DriverApi(get()) }
    single<F1SessionApi> { F1SessionApi(get()) }
    single<OpenF1Api> { OpenF1Api(get()) }

    factory<ResponseCache<com.jskinner.f1dash.domain.models.F1RaceData>> { ResponseCacheImpl() }

    single<F1Repository> { CachedF1RepositoryImpl(get(), get(), get(), get()) }
    
    factory { F1PreviousRacesViewModel(get()) }
    factory { F1RaceViewModel(get()) }
    factory { F1ReplayViewModel(get()) }
}