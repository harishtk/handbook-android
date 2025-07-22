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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
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

/**
 * Transforms the flow to apply a minimum delay after the last item that matches the [loadingItemPredicate].
 *
 * This function ensures that after an item that satisfies the [loadingItemPredicate] (i.e., a "loading" item),
 * the subsequent items will not be emitted until the specified [minDelayFromLoadingItem] has passed. This is
 * particularly useful for reducing UI flicker by introducing a delay after loading states before showing
 * other items, thus improving the user experience.
 *
 * The delay is applied only if there was a loading item immediately before a non-loading item. If the current
 * item is a loading item, it is emitted immediately, and the timestamp of this event is updated. For subsequent
 * items that are not loading items, the function calculates the elapsed time since the last loading item and
 * applies the delay if the elapsed time is less than the specified minimum delay.
 *
 * ### Parameters:
 * - `loadingItemPredicate`: A predicate function that determines if an item is a "loading" item. It is a function
 *   that takes an item of type [T] and returns `true` if the item is considered loading.
 * - `minDelayFromLoadingItem`: The minimum delay in milliseconds that must be applied after a loading item before
 *   any subsequent items are emitted. This delay helps prevent flickering or rapid state changes in the UI.
 *
 * ### Usage Example:
 * ```
 * flowOf(Loading, Success, Error)
 *     .filteredDelay(
 *         loadingItemPredicate = { it is Loading },
 *         minDelayFromLoadingItem = 500
 *     )
 *     .collect { item ->
 *         // Handle the item
 *     }
 * ```
 *
 * In the example above, the function will ensure that after a `Loading` item, there will be a 500-millisecond
 * delay before any `Success` or `Error` items are emitted. If no loading item is detected before a non-loading
 * item, it will be emitted immediately.
 *
 * @param loadingItemPredicate A function that returns `true` if the item is considered a loading item.
 * @param minDelayFromLoadingItem The minimum time in milliseconds to wait after a loading item before emitting
 *        any subsequent items.
 * @return A new flow that applies the specified delay logic based on the loading items.
 */
inline fun <T> Flow<T>.filteredDelay(
    crossinline loadingItemPredicate: (T) -> Boolean,
    minDelayFromLoadingItem: Long,
): Flow<T> = flow {
    var lastLoadingTimestamp = 0L

    collect { item ->
        if (loadingItemPredicate(item)) { // Received loading item
            emit(item)
            lastLoadingTimestamp = System.currentTimeMillis()
        } else { // Received non-loading item
            val ts = System.currentTimeMillis()
            val delayDuration = (minDelayFromLoadingItem - (ts - lastLoadingTimestamp)).coerceAtLeast(0L)
            if (delayDuration > 0) {
                delay(delayDuration)
            }
            emit(item)
        }
    }
}