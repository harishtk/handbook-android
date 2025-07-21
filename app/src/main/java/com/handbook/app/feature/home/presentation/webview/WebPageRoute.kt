package com.handbook.app.feature.home.presentation.webview

import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.handbook.app.Constant
import com.handbook.app.core.designsystem.HandbookTopAppBarState
import com.handbook.app.core.designsystem.component.HandbookSimpleTopAppBar
import com.handbook.app.ui.theme.HandbookTheme
import timber.log.Timber

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun WebPageRoute(
    modifier: Modifier = Modifier,
    url: String = Constant.LANDING_URL,
    onNavUp: () -> Unit,
) {

    val topAppBarState = remember {
        HandbookTopAppBarState(
            title = "Handbook near me",
            showNavigationIcon = true,
            onNavigationIconClick = onNavUp,
        )
    }

    WebPageScreen(
        modifier = modifier,
        url = url,
        topAppBarState = topAppBarState,
        onNavUp = onNavUp,
    )
}

@ExperimentalLayoutApi
@Composable
internal fun WebPageScreen(
    modifier: Modifier = Modifier,
    topAppBarState: HandbookTopAppBarState,
    onNavUp: () -> Unit = {},
    url: String,
) {
    Timber.d("WebView: loading url: $url")
    var isFirstLoad = remember { true }
    var progress by remember { mutableFloatStateOf(0f) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                HandbookSimpleTopAppBar(state = topAppBarState)
                Box(modifier = Modifier.height(IntrinsicSize.Min)) {
                    if (progress <= 0.99F) {
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Vertical
                    )
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            var backEnabled by remember { mutableStateOf(false) }
            var webView: WebView? = null

            AndroidView(
                modifier = modifier,
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        val chromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)

                                progress = newProgress / 100F
                                Timber.tag("WebView").d("Progress: $progress")
                                /*if (newProgress < 100 && isFirstLoad) {
                                    toolbarIncluded.toolbarTitle.text = getString(R.string.label_loading)
                                    progressBar.isVisible = true
                                    isFirstLoad = false
                                } else {
                                    progressBar.isVisible = false
                                    val currentUrl = view?.url
                                    Timber.tag(TAG).d("Current url: $currentUrl")
                                    // TODO: handle url
                                }*/
                            }

                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                super.onReceivedTitle(view, title)
                                Timber.tag("WebView").d("onReceivedTitle() title = $title")
                                /*toolbarIncluded.toolbarTitle.text = title*/
                            }
                        }

                        val webClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                                backEnabled = view.canGoBack()
                            }
                        }
                        this.webViewClient = webClient
                        this.webChromeClient = chromeClient
                        this.settings.apply {
                            javaScriptEnabled = true
                            setSupportZoom(false)
                        }

                        loadUrl(url)
                        webView = this
                    }
                }, update = {
                    webView = it
                })

            BackHandler(enabled = backEnabled) {
                webView?.goBack()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true)
@Composable
fun WebPageScreenPreview() {
    Box {
        HandbookTheme {
            WebPageScreen(
                topAppBarState = HandbookTopAppBarState(
                    title = "Handbook near me",
                    showNavigationIcon = true,
                    onNavigationIconClick = {}
                ),
                url = Constant.LANDING_URL,
            )
        }
    }
}
