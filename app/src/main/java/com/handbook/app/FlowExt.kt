package com.handbook.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.handbook.app.ui.insetNone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun <T> Flow<T>.launchWhenStarted(lifecycleOwner: LifecycleOwner) {
    lifecycleOwner.lifecycleScope.launchWhenStarted {
        collect()
    }
}

fun <T> Flow<T>.launchWhenCreated(lifecycleOwner: LifecycleOwner) {
    lifecycleOwner.lifecycleScope.launchWhenCreated {
        collect()
    }
}

fun <T> Flow<T>.launch(lifecycleOwner: LifecycleOwner) {
    lifecycleOwner.lifecycleScope.launch {
       collect()
    }
}

@Composable
fun <T> ObserverAsEvents(flow: Flow<T>, onEvent: (event: T) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(key1 = lifecycleOwner.lifecycle) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect(onEvent)
        }
    }
}

@Composable
fun WithLifecycle(
    state: Lifecycle.State = Lifecycle.State.STARTED,
    block: suspend CoroutineScope.() -> Unit
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    LaunchedEffect(key1 = lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(state) {
            block()
        }
    }
}
