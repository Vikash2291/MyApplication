package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ads.AdsManager
import com.example.myapplication.data.local.preferences.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Shared banner state for secondary screens (bookmarks, stats, settings, about). */
@HiltViewModel
class BannerViewModel @Inject constructor(
    adsManager: AdsManager,
    preferencesRepository: PreferencesRepository
) : ViewModel() {
    val adUnitId: String = adsManager.bannerAdUnitId

    val showBanner: StateFlow<Boolean> = combine(
        adsManager.state,
        preferencesRepository.settings
    ) { ads, settings -> ads.canRequestAds && settings.adMobEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
}
