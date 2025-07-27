package com.jskinner.f1dash.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.jskinner.f1dash.data.models.ApiResult
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

abstract class BaseViewModel<State : Any, SideEffect : Any>(
    initialState: State
) : ViewModel(), ContainerHost<State, SideEffect> {

    override val container: Container<State, SideEffect> = container(initialState)

} 