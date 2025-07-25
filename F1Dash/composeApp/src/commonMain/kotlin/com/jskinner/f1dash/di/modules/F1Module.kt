package com.jskinner.f1dash.di.modules

import com.jskinner.f1dash.data.api.F1ApiClient
import com.jskinner.f1dash.data.repository.F1RepositoryImpl
import com.jskinner.f1dash.domain.repository.F1Repository
import com.jskinner.f1dash.presentation.viewmodels.F1DriversViewModel
import com.jskinner.f1dash.presentation.viewmodels.F1RaceViewModel
import org.koin.dsl.module

val f1Module = module {
    single<F1ApiClient> { F1ApiClient(get()) }
    single<F1Repository> { F1RepositoryImpl(get()) }
    factory { F1DriversViewModel(get()) }
    factory { F1RaceViewModel(get()) }
}