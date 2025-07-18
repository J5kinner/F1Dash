package com.jskinner.f1dash.di.modules

import com.jskinner.f1dash.data.api.F1ApiClient
import com.jskinner.f1dash.data.repository.F1RepositoryImpl
import com.jskinner.f1dash.domain.repository.F1Repository
import org.koin.dsl.module

val f1Module = module {
    single<F1ApiClient> { F1ApiClient(get()) }
    single<F1Repository> { F1RepositoryImpl(get()) }
}