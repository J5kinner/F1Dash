package com.jskinner.f1dash.di.modules

import com.jskinner.f1dash.data.api.F1DriverApi
import com.jskinner.f1dash.data.api.F1LapApi
import com.jskinner.f1dash.data.api.F1PositionApi
import com.jskinner.f1dash.data.api.F1SessionApi
import com.jskinner.f1dash.data.api.F1StintApi
import com.jskinner.f1dash.data.api.F1WeatherApi
import com.jskinner.f1dash.data.repository.F1RepositoryImpl
import com.jskinner.f1dash.domain.models.F1Driver
import com.jskinner.f1dash.domain.repository.F1Repository
import com.jskinner.f1dash.presentation.viewmodels.F1DriversViewModel
import com.jskinner.f1dash.presentation.viewmodels.F1RaceViewModel
import org.koin.dsl.module

val f1Module = module {
    single<F1DriverApi> { F1DriverApi(get()) }
    single<F1LapApi> { F1LapApi(get()) }
    single<F1PositionApi> { F1PositionApi(get()) }
    single<F1SessionApi> { F1SessionApi(get()) }
    single<F1StintApi> { F1StintApi(get()) }
    single<F1WeatherApi> { F1WeatherApi(get()) }
    single<F1Repository> { F1RepositoryImpl(get(), get(), get(), get(), get(), get()) }
    factory { F1DriversViewModel(get()) }
    factory { F1RaceViewModel(get()) }
}