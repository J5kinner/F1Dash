package com.jskinner.f1dash.data.cache

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

expect fun createDataStore(name: String = "f1_cache"): DataStore<Preferences>