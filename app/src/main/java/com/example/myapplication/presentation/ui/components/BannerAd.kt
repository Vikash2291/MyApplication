package com.example.myapplication.presentation.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.presentation.viewmodel.BannerViewModel
import com.google.ads.mediation.admob.AdMobAdapter

/** Vertical gap that keeps the banner visually separated from tappable content above it. */
private val BannerSeparation = 8.dp

/**
 * Anchored adaptive banner inside a fixed-height slot. The slot height is reserved from the ad
 * size up front, so content never jumps when the ad loads and nothing can be overlapped.
 * Place it at the bottom of a screen (e.g. Scaffold bottomBar), away from buttons.
 */
@Composable
fun AdaptiveBannerSlot(adUnitId: String, modifier: Modifier = Modifier, collapsible: Boolean = false) {
    val context = LocalContext.current
    val widthDp = LocalConfiguration.current.screenWidthDp
    val adSize = remember(widthDp) { AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, widthDp) }
    val adView = remember(adUnitId, adSize) {
        AdView(context).apply {
            setAdSize(adSize)
            this.adUnitId = adUnitId
            val request = AdRequest.Builder().apply {
                // Collapsible banner: opens larger once, user collapses it with its own arrow.
                // Higher eCPM than a plain anchored banner; AdMob caps how often it expands.
                if (collapsible) {
                    addNetworkExtrasBundle(
                        AdMobAdapter::class.java,
                        Bundle().apply { putString("collapsible", "bottom") }
                    )
                }
            }.build()
            loadAd(request)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(adView, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> adView.resume()
                Lifecycle.Event.ON_PAUSE -> adView.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            adView.destroy()
        }
    }

    Surface(color = MaterialTheme.colorScheme.surface, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.navigationBarsPadding()) {
            HorizontalDivider()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = BannerSeparation)
                    .height(adSize.height.dp),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(factory = { adView }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

/** Self-contained bottom banner for secondary screens; hidden until consent allows ads. */
@Composable
fun AppBannerBar(modifier: Modifier = Modifier, viewModel: BannerViewModel = hiltViewModel()) {
    val show by viewModel.showBanner.collectAsStateWithLifecycle()
    if (show && viewModel.adUnitId.isNotBlank()) {
        AdaptiveBannerSlot(viewModel.adUnitId, modifier)
    }
}
