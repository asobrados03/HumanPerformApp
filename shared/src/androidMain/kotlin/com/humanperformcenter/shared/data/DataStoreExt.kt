package com.humanperformcenter.shared.data.network

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.humanperformcenter.shared.domain.storage.DATA_STORE_FILE_NAME

val Context.dataStore by preferencesDataStore(name = DATA_STORE_FILE_NAME)