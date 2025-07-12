package com.handbook.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.handbook.app.ui.theme.HandbookTheme

@Composable
fun UserAvatar(
    modifier: Modifier = Modifier,
    name: String,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val initials = name.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
        Text(
            text = initials.ifEmpty { "?" },
            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onPrimaryContainer)
        )
    }
}

@ThemePreviews
@Composable
private fun UserAvatarDefault() {
    HandbookTheme(disableDynamicTheming = true) {
        Box(Modifier.padding(8.dp)) {
            UserAvatar(name = "John Doe", modifier = Modifier.size(48.dp))
        }
    }
}

@ThemePreviews
@Composable
private fun UserAvatarDynamic() {
    HandbookTheme(disableDynamicTheming = false) {
        Box(Modifier.padding(8.dp)) {
            UserAvatar(name = "John Doe", modifier = Modifier.size(48.dp))
        }
    }
}

@ThemePreviews
@Composable
private fun UserAvatarAndroid() {
    HandbookTheme(androidTheme = true) {
        Box(Modifier.padding(8.dp)) {
            UserAvatar(name = "John Doe", modifier = Modifier.size(48.dp))
        }
    }
}

@ThemePreviews
@Composable
private fun GradientUserAvatarDefault() {
    HandbookTheme(disableDynamicTheming = true) {
        Box(Modifier.padding(8.dp)) {
            UserAvatar(name = "John Doe", modifier = Modifier.size(48.dp))
        }
    }
}

@ThemePreviews
@Composable
private fun GradientUserAvatarDynamic() {
    HandbookTheme(disableDynamicTheming = false) {
        Box(Modifier.padding(8.dp)) {
            UserAvatar(name = "John Doe", modifier = Modifier.size(48.dp))
        }
    }
}

@ThemePreviews
@Composable
private fun GradientUserAvatarAndroid() {
    HandbookTheme(androidTheme = true) {
        Box(Modifier.padding(8.dp)) {
            UserAvatar(name = "John Doe", modifier = Modifier.size(48.dp))
        }
    }
}