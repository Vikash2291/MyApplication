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

/** Vertical gap that keeps the banner visually separated from tappable content above it. */
private val BannerSeparation = 8.dp

/**
 * Anchored adaptive banner inside a fixed-height slot. The slot height is reserved from the ad
 * size up front, so content never jumps when the ad loads and nothing can be overlapped.
 * Place it at the bottom of a screen (e.g. Scaffold bottomBar), away from buttons.
 */
@Composable
fun AdaptiveBannerSlot(adUnitId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val widthDp = LocalConfiguration.current.screenWidthDp
    val adSize = remember(widthDp) { AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, widthDp) }
    val adView = remember(adUnitId, adSize) {
        AdView(context).apply {
            setAdSize(adSize)
            this.adUnitId = adUnitId
            loadAd(AdRequest.Builder().build())
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
