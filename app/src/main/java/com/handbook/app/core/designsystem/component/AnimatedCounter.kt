package com.handbook.app.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.handbook.app.core.designsystem.component.text.TextFieldState
import com.handbook.app.core.designsystem.component.text.textFieldStateSaver

@Composable
fun AnimatedText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    var oldText by remember { mutableStateOf(text) }
    SideEffect {
        oldText = text
    }
    Row(modifier) {
        for (i in text.indices) {
            val oldChar = oldText.getOrNull(i)
            val newChar = text[i]
            val char = if (oldChar == newChar) {
                oldText[i]
            } else {
                text[i]
            }
            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    slideInVertically { it } togetherWith slideOutVertically { -it }
                },
                label = "Counter Animation $char"
            ) { char ->
                Text(
                    text = char.toString(),
                    style = style,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
fun AnimatedCounter(
    count: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    var oldCount by remember {
        mutableIntStateOf(count)
    }
    SideEffect {
        oldCount = count
    }
    Row(modifier = modifier) {
        val countString = count.toString()
        val oldCountString = count.toString()
        for (i in countString.indices) {
            val oldChar = oldCountString.getOrNull(i)
            val newChar = countString[i]
            val char = if (oldChar == newChar) {
                oldCountString[i]
            } else {
                countString[i]
            }

            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    slideInVertically { it } togetherWith slideOutVertically { -it }
                },
                label = "Counter Animation $char"
            ) { char ->
                Text(
                    text = char.toString(),
                    style = style,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun AnimatedCounterPreview() {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val counterState by remember {
            mutableStateOf(
                DefaultTextState("200").apply {
                    enableShowErrors()
                }
            )
        }

        val counter by remember {
            derivedStateOf {
                if (counterState.text.matches(Regex("^\\d+$"))) {
                    counterState.text.toIntOrNull() ?: 0
                } else {
                    0
                }
            }
        }

        AnimatedCounter(
            count = counter,
            style = MaterialTheme.typography.displayMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = counterState.text,
            onValueChange = {
                counterState.text = it
            }
        )
    }
}

private class DefaultTextState(initialValue: String)
    : TextFieldState(::isValidText, ::defaultExtErrorMessage) {
    init {
        text = initialValue
    }
}

private fun isValidText(text: String): Boolean {
    return text.isNotBlank()
}

private fun defaultExtErrorMessage(text: String): String {
    return "Cannot be blank"
}

private val DefaultTextStateSaver = textFieldStateSaver(DefaultTextState(""))