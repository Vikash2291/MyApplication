package com.example.myapplication.presentation.ui.components

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.presentation.viewmodel.BannerViewModel
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * Native ad shown as a clearly labelled card inside a list (Result review, Bookmarks).
 * Nothing is drawn until an ad loads, and nothing at all without ad consent.
 */
@Composable
fun NativeAdCard(modifier: Modifier = Modifier, viewModel: BannerViewModel = hiltViewModel()) {
    val show by viewModel.showBanner.collectAsStateWithLifecycle()
    if (!show) return
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    DisposableEffect(Unit) {
        var disposed = false
        val loader = AdLoader.Builder(context, context.getString(R.string.admob_native_ad_unit_id))
            .forNativeAd { ad -> if (disposed) ad.destroy() else nativeAd = ad }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) = Unit
            })
            .withNativeAdOptions(NativeAdOptions.Builder().setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT).build())
            .build()
        loader.loadAd(AdRequest.Builder().build())
        onDispose {
            disposed = true
            nativeAd?.destroy()
        }
    }

    val ad = nativeAd ?: return
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx -> buildNativeAdView(ctx) },
        update = { view -> bind(view, ad) }
    )
}

private fun dp(ctx: android.content.Context, v: Int) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), ctx.resources.displayMetrics).toInt()

private fun buildNativeAdView(ctx: android.content.Context): NativeAdView {
    val adView = NativeAdView(ctx)
    val root = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(ctx, 12), dp(ctx, 12), dp(ctx, 12), dp(ctx, 12))
        background = GradientDrawable().apply {
            cornerRadius = dp(ctx, 16).toFloat()
            setStroke(dp(ctx, 1), Color.parseColor("#33888888"))
        }
    }
    val header = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
    val badge = TextView(ctx).apply {
        text = ctx.getString(R.string.native_ad_badge)
        setTextColor(Color.WHITE)
        textSize = 11f
        setPadding(dp(ctx, 6), dp(ctx, 2), dp(ctx, 6), dp(ctx, 2))
        background = GradientDrawable().apply { cornerRadius = dp(ctx, 4).toFloat(); setColor(Color.parseColor("#F9A825")) }
    }
    val icon = ImageView(ctx).apply { id = android.view.View.generateViewId() }
    val headline = TextView(ctx).apply { textSize = 16f; setTypeface(typeface, android.graphics.Typeface.BOLD); maxLines = 2 }
    header.addView(badge)
    header.addView(icon, LinearLayout.LayoutParams(dp(ctx, 36), dp(ctx, 36)).apply { marginStart = dp(ctx, 8) })
    header.addView(headline, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dp(ctx, 8) })
    val media = MediaView(ctx)
    val body = TextView(ctx).apply { textSize = 13f; maxLines = 3 }
    val cta = Button(ctx)
    root.addView(header)
    root.addView(media, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 160)).apply { topMargin = dp(ctx, 8) })
    root.addView(body, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(ctx, 8) })
    root.addView(cta, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(ctx, 8) })
    adView.addView(root)
    adView.headlineView = headline
    adView.iconView = icon
    adView.mediaView = media
    adView.bodyView = body
    adView.callToActionView = cta
    return adView
}

private fun bind(view: NativeAdView, ad: NativeAd) {
    (view.headlineView as TextView).text = ad.headline
    (view.bodyView as TextView).apply {
        text = ad.body
        visibility = if (ad.body.isNullOrBlank()) android.view.View.GONE else android.view.View.VISIBLE
    }
    (view.callToActionView as Button).apply {
        text = ad.callToAction
        visibility = if (ad.callToAction.isNullOrBlank()) android.view.View.GONE else android.view.View.VISIBLE
    }
    (view.iconView as ImageView).apply {
        setImageDrawable(ad.icon?.drawable)
        visibility = if (ad.icon == null) android.view.View.GONE else android.view.View.VISIBLE
    }
    view.mediaView?.mediaContent = ad.mediaContent
    view.setNativeAd(ad)
}
