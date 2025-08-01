package com.jskinner.f1dash.presentation.viewmodels

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.ContainerHost

abstract class BaseViewModel<State : Any, SideEffect : Any> : ViewModel(),
    ContainerHost<State, SideEffect> 