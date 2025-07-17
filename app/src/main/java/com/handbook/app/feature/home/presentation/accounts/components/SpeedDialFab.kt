package com.handbook.app.feature.home.presentation.accounts.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.handbook.app.core.designsystem.HandbookIcons

// Data class for individual FAB options
data class FabOption(
    val icon: ImageVector,
    val iconTint: Color = Color.Unspecified,
    val label: String,
    val onClick: () -> Unit,
)

@Composable
fun SpeedDialFab(
    modifier: Modifier = Modifier,
    initialIcon: ImageVector = Icons.Default.Edit,
    expandedIcon: ImageVector = Icons.Default.Close,
    options: List<FabOption>,
    onMainFabClickBehavior: OnMainFabClickBehavior = OnMainFabClickBehavior.TOGGLE_EXPANSION,
    onMainFabClickWhileCollapsed: (() -> Unit)? = null // If behavior is EXPAND_ONLY
) {
    var isExpanded by remember { mutableStateOf(false) }
    val transition = updateTransition(targetState = isExpanded, label = "FabExpansionTransition")

    // Scrim animation
    val scrimAlpha by transition.animateFloat(
        label = "ScrimAlpha",
        transitionSpec = { tween(durationMillis = 300) }
    ) { expanded ->
        if (expanded) 0.6f else 0f
    }

    // Main FAB icon rotation
    val fabIconRotation by transition.animateFloat(
        label = "FabIconRotation",
        transitionSpec = { tween(durationMillis = 200) }
    ) { expanded ->
        if (expanded) 45f else 0f // Rotate 45 deg for close, or adjust as needed
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        // Scrim - clickable to collapse
        if (scrimAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(scrimAlpha)
                    .background(Color.Black)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null, // No ripple for scrim click
                        onClick = { isExpanded = false }
                    )
            )
        }

        // Column for options and main FAB
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(16.dp) // Adjust padding as needed for placement
        ) {
            // Animated options
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(150, delayMillis = 50)) + expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    expandFrom = Alignment.Bottom
                ),
                exit = fadeOut(animationSpec = tween(150, delayMillis = 50)) + shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    shrinkTowards = Alignment.Bottom
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp) // Spacing between mini-fabs
                ) {
                    options.forEachIndexed { index, option ->
                        MiniFabItem(
                            item = option,
                            onFabOptionSelected = {
                                option.onClick()
                                isExpanded = false // Collapse after option click
                            },
                            // Add a slight delay for staggered animation if desired
                            // You might need a more complex transition for this.
                            // This basic AnimatedVisibility applies to the whole column.
                        )
                    }
                }
            }

            // Main FAB
            ExtendedFloatingActionButton(
                onClick = {
                    when (onMainFabClickBehavior) {
                        OnMainFabClickBehavior.TOGGLE_EXPANSION -> isExpanded = !isExpanded
                        OnMainFabClickBehavior.EXPAND_ONLY_AND_EXECUTE_PRIMARY -> {
                            if (isExpanded) {
                                isExpanded = false // If expanded, collapse
                            } else {
                                // If collapsed, execute primary action AND expand
                                onMainFabClickWhileCollapsed?.invoke()
                                isExpanded = true
                            }
                        }

                        OnMainFabClickBehavior.EXPAND_ONLY_OR_EXECUTE_PRIMARY_WHEN_COLLAPSED -> {
                            if (isExpanded) {
                                isExpanded = false // If expanded, just collapse
                            } else {
                                // If collapsed, and we have options, expand.
                                // If no options, or specific need, execute primary.
                                if (options.isNotEmpty()) {
                                    isExpanded = true
                                } else {
                                    onMainFabClickWhileCollapsed?.invoke()
                                }
                            }
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isExpanded && options.isNotEmpty()) expandedIcon else initialIcon,
                        contentDescription = if (isExpanded) "Close options" else "Open options",
                        modifier = Modifier.rotate(if (options.isNotEmpty()) fabIconRotation else 0f)
                    )
                },
                text = {
                    // Show text only when not expanded or if it's the primary action without options
                    if (!isExpanded || options.isEmpty()) {
                        Text(text = "Add") // Your original FAB text
                    }
                },
                expanded = !isExpanded || options.isEmpty() // FAB is "extended" when options are hidden
            )
        }
    }
}

@Composable
fun MiniFabItem(
    item: FabOption,
    onFabOptionSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = modifier.clickable(onClick = onFabOptionSelected)
    ) {
        // Optional Label for Mini FABs
        if (item.label.isNotEmpty()) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer, // Or surface variant
                tonalElevation = 2.dp,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        FloatingActionButton(
            onClick = onFabOptionSelected,
            shape = CircleShape, // Use CircleShape for mini FABs
            containerColor = MaterialTheme.colorScheme.surfaceVariant, // Example color
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 4.dp,
                pressedElevation = 6.dp
            ),
            modifier = Modifier.size(40.dp) // Smaller size for mini FABs
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (item.iconTint != Color.Unspecified) item.iconTint else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Enum to control behavior of the main FAB click
enum class OnMainFabClickBehavior {
    TOGGLE_EXPANSION, // Default: Clicking main FAB toggles options
    EXPAND_ONLY_AND_EXECUTE_PRIMARY, // Clicking main FAB when collapsed executes primary action AND expands
    EXPAND_ONLY_OR_EXECUTE_PRIMARY_WHEN_COLLAPSED // Clicking main FAB when collapsed expands (if options exist), or executes primary action (if no options / specific need)
}

// --- Preview ---
@Preview(showBackground = true)
@Composable
fun SpeedDialFabPreview() {
    val fabOptions = listOf(
        FabOption(Icons.Default.NorthEast, label = "Add Expense") { println("Expense clicked") },
        FabOption(Icons.Default.SouthWest, label = "Add Income") { println("Income clicked") },
        // Add more options
    )

    MaterialTheme { // Ensure a MaterialTheme is applied for previews
        Box(modifier = Modifier.fillMaxSize()) { // Fill the preview area
            SpeedDialFab(
                initialIcon = Icons.Default.Edit,
                expandedIcon = Icons.Default.Close,
                options = fabOptions,
                onMainFabClickBehavior = OnMainFabClickBehavior.EXPAND_ONLY_OR_EXECUTE_PRIMARY_WHEN_COLLAPSED,
                onMainFabClickWhileCollapsed = { println("Main FAB 'Write' action executed while collapsed") }
            )

            // Example of a FAB with no extra options, just the primary action
//            SpeedDialFab(
//                modifier = Modifier.padding(top = 150.dp), // Offset for preview
//                initialIcon = Icons.Default.Add,
//                options = emptyList(),
//                onMainFabClickWhileCollapsed = { println("Primary Add action executed") }
//            )
        }
    }
}