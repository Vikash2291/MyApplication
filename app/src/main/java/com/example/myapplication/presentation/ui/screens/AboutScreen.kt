package com.example.myapplication.presentation.ui.screens

import com.example.myapplication.presentation.ui.components.AppBannerBar
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import com.example.myapplication.presentation.ui.components.AppTopBar

@Composable
fun AboutScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = { AppTopBar(stringResource(R.string.about_title), onNavigateUp = onBackClick) },
        bottomBar = { AppBannerBar() }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(painterResource(R.drawable.ic_logo), contentDescription = null, modifier = Modifier.size(96.dp))
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.about_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                stringResource(R.string.about_body),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(stringResource(R.string.about_copyright), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
