package com.jskinner.f1dash.data.cache

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

actual fun createDataStore(name: String): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            val documentDirectory: NSURL? = NSFileManager.defaultManager.URLsForDirectory(
                directory = NSDocumentDirectory,
                inDomains = NSUserDomainMask
            ).firstOrNull() as? NSURL

            val filePath = documentDirectory?.path + "/$name.preferences_pb"
            filePath?.toPath() ?: "$name.preferences_pb".toPath()
        }
    )
}