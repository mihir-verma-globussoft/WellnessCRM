package com.crm.enhance_wellness

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crm.enhance_wellness.core.storage.DataStoreManager
import com.crm.enhance_wellness.feature.notifications.data.local.dao.NotificationDao
import com.crm.enhance_wellness.feature.notifications.domain.usecase.SyncPortalNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val notificationDao: NotificationDao,
    private val syncPortalNotifications: SyncPortalNotificationsUseCase,
) : ViewModel() {

    init {
        // Pull notifications as soon as there is a session, and again on every sign-in.
        // The unread badge is backed by Room, so without this it stayed stale until the
        // user happened to open the inbox — which is the only other place sync runs.
        viewModelScope.launch {
            dataStoreManager.tokenFlow()
                .map { !it.isNullOrBlank() }
                .distinctUntilChanged()
                .collect { signedIn -> if (signedIn) syncPortalNotifications() }
        }
    }

    val isDarkTheme = dataStoreManager.isDarkThemeFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val clinicName = dataStoreManager.clinicNameFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "Wellness")

    val unreadNotificationCount = notificationDao.getUnreadCountAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val isAuthenticated: StateFlow<Boolean?> = dataStoreManager.tokenFlow()
        .map { !it.isNullOrBlank() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun toggleDarkTheme() {
        viewModelScope.launch { dataStoreManager.setDarkTheme(!isDarkTheme.value) }
    }
}
