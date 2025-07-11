@file:OptIn(ExperimentalAnimationApi::class)

package com.handbook.app.core.designsystem.component.text

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.handbook.app.ui.defaultCornerSize
import com.handbook.app.ui.insetLarge

private const val DEFAULT_OTP_LENGTH = 6

@Composable
internal fun OtpTextField(
    otpState: TextFieldState,
    modifier: Modifier = Modifier,
    onOtpChange: (String) -> Unit = {},
    otpLength: Int = DEFAULT_OTP_LENGTH,
) {
    BasicTextField(
        value = otpState.text,
        onValueChange = { typed: String ->
            otpState.text = typed.take(otpLength).replace(Regex("\\D"), "")
            onOtpChange(otpState.text)
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
        ),
        modifier = modifier,
    ) { innerTextField ->
        Row(horizontalArrangement = Arrangement.Center) {
            repeat(otpLength) { index ->
                val char = when {
                    index >= otpState.text.length -> ""
                    else -> otpState.text[index].toString()
                }
                val isFocused = otpState.text.length == index
                val shouldAnimate = otpState.text.length - 1 == index

                Box(
                    modifier = Modifier.width(40.dp)
                    .aspectRatio(1f)
                    .border(
                        width = if (isFocused) 2.dp
                        else 1.dp,
                        color = if (isFocused) Color.DarkGray
                        else Color.LightGray,
                        shape = RoundedCornerShape(defaultCornerSize)
                    )
                    .padding(2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AnimatedContent(
                        targetState = char,
                        transitionSpec = {
                            (fadeIn() + slideInVertically(animationSpec = tween(400),
                                initialOffsetY = { fullHeight -> fullHeight })).togetherWith(
                                fadeOut(
                                    animationSpec = tween(200)
                                )
                            )
                        },
                        label = "Otp char animation"
                    ) { char ->
                        Text(
                            modifier = Modifier,
                            text = char,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                if (index < otpLength - 1)
                    Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Preview(name = "otp text field", device = "id:pixel_3a", showBackground = true)
@Composable
private fun OtpTextFieldPreview() {
    BoxWithConstraints(
        modifier = Modifier.padding(insetLarge)
    ) {
        val otpState by rememberSaveable(stateSaver = OtpFieldStateSaver) {
            mutableStateOf(OtpFieldState("12"))
        }
        OtpTextField(
            otpState = otpState,
        )
    }
}