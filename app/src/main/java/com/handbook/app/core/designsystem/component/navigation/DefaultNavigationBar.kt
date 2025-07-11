package com.handbook.app.core.designsystem.component.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.handbook.app.core.designsystem.component.HandbookNavigationDefaults
import com.handbook.app.ui.defaultCornerSize
import kotlin.math.roundToInt

@Composable
fun DefaultNavigationBar(
    modifier: Modifier,
    containerColor: Color = HandbookNavigationDefaults.containerColor,
    contentColor: Color = HandbookNavigationDefaults.navigationContentColor(),
    tonalElevation: Dp = HandbookNavigationDefaults.Elevation,
    windowInsets: WindowInsets = HandbookNavigationDefaults.windowInsets,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(windowInsets)
                .height(HandbookNavigationDefaults.NavigationBarHeight)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(
                HandbookNavigationDefaults.NavigationBarItemHorizontalPadding
            ),
            content = content
        )
    }
}

/**
 * Handbook navigation bar items with icon and label content. Mimics Material 3
 * [NavigationBarItem].
 *
 * @param selected Whether this item is selected.
 * @param onClick The callback to the invoked when this item is selected.
 * @param icon The item icon content.
 * @param selectedIcon the item icon content when selected.
 * @param enabled controls the enabled state of this item. When `false`, this item will not be
 * clickable and will appear disabled to accessibility services.
 * @param label The item text label content.
 * @param alwaysShowLabel Whether to always show the label for this item. If false, the label will
 * only be shown when this item is selected.
 */
@Composable
fun RowScope.DefaultNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    alwaysShowLabel: Boolean = true,
    colors: HandbookNavigationBarItemColors = HandbookNavigationBarItemDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val styledIcon = @Composable {
        val iconColor by colors.iconColor(selected = selected, enabled = enabled)
        val clearSemantics = label != null && (alwaysShowLabel || selected)
        Box(modifier = if (clearSemantics) Modifier.clearAndSetSemantics {} else Modifier) {
            CompositionLocalProvider(LocalContentColor provides iconColor, content = icon)
        }
    }

    val styledLabel: @Composable (() -> Unit)? = label?.let { 
        @Composable {
            val style = MaterialTheme.typography.labelMedium
            val textColor by colors.textColor(selected = selected, enabled = enabled)
            CompositionLocalProvider(LocalContentColor provides textColor) {
                ProvideTextStyle(style, content = label)
            }
        }
    }

    var itemWidth by remember { mutableStateOf(0) }
    
    Box(
        modifier
            .selectable(
                selected = selected,
                onClick = onClick,
                enabled = enabled,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = null,
            )
            .weight(1f)
            .onSizeChanged {
                itemWidth = it.width
            },
        contentAlignment = Alignment.Center
    ) {
        val animationProgress: Float by animateFloatAsState(
            targetValue = if (selected) 1f else 0f,
            animationSpec = tween(ItemAnimationDurationMillis),
            label = "Item Animation State",
        )

        // The entire item is selectable, but only the indicator pill shows the ripple. To achieve
        // this, we re-map the coordinates of the item's InteractionSource into the coordinates of
        // the indicator
        val deltaOffset: Offset
        with(LocalDensity.current) {
            val indicatorWidth = 3.dp.roundToPx()
            deltaOffset = Offset(
                (itemWidth - indicatorWidth).toFloat() / 2,
                IndicatorVerticalOffset.toPx()
            )
        }
        val offsetInteractionSource = remember(interactionSource, deltaOffset) {
            MappedInteractionSource(interactionSource, deltaOffset)
        }

        // The indicator has a width-expansion animation which interferes with the timing of the
        // ripple, which is why they are separate composables
        /* **Currently we don't have indicators in the app ** */
        /*val indicatorRipple = @Composable {
            Box(
                Modifier
                    .layoutId(IndicatorRippleLayoutIdTag)
                    .clip(ActiveIndicatorShape)
                    .indication(offsetInteractionSource, rememberRipple())
            )
        }
        val indicator = @Composable {
            Box(
                Modifier
                    .layoutId(IndicatorLayoutIdTag)
                    .background(
                        color = colors.indicatorColor.copy(alpha = animationProgress),
                        shape = ActiveIndicatorShape
                    )
            )
        }*/
        val scale by animateFloatAsState(
            targetValue = if (selected) 1.2f else 1f,
            animationSpec = tween(ItemAnimationDurationMillis),
            label = "Icon Scale Animation"
        )

        HandbookNavigationBarItemBaselineLayout(
            icon = styledIcon,
            label = styledLabel,
            alwaysShowLabel = alwaysShowLabel,
            scaleProvider = { scale },
            animationProgressProvider = { animationProgress }
        )
    }
}

@Composable
private fun HandbookNavigationBarItemBaselineLayout(
    icon: @Composable () -> Unit,
    label: @Composable (() -> Unit)?,
    alwaysShowLabel: Boolean,
    scaleProvider: () -> Float,
    animationProgressProvider: () -> Float,
) {
    val animationProgress = animationProgressProvider()
    Layout({
        Box(
            Modifier
                .layoutId(IconLayoutIdTag)
                .scale(scaleProvider()),
        ) { icon() }

        if (label != null) {
            Box(
                Modifier
                    .layoutId(LabelLayoutIdTag)
                    .alpha(if (alwaysShowLabel) 1f else animationProgress)
                    .padding(horizontal = NavigationBarItemHorizontalPadding / 2)
            ) { label() }
        }
    }) { measurables, constraints ->
        val iconPlaceable =
            measurables.first { it.layoutId == IconLayoutIdTag }.measure(constraints)

        /* **We don't want indicator as of now in this app ** */
        /*val totalIndicatorWidth = iconPlaceable.width + (IndicatorHorizontalPadding * 2).roundToPx()
        val animatedIndicatorWidth = (totalIndicatorWidth * animationProgress).roundToInt()
        val indicatorHeight = iconPlaceable.height + (IndicatorVerticalPadding * 2).roundToPx()
        val indicatorRipplePlaceable =
            measurables
                .first { it.layoutId == IndicatorRippleLayoutIdTag }
                .measure(
                    Constraints.fixed(
                        width = totalIndicatorWidth,
                        height = indicatorHeight
                    )
                )
        val indicatorPlaceable =
            measurables
                .firstOrNull { it.layoutId == IndicatorLayoutIdTag }
                ?.measure(
                    Constraints.fixed(
                        width = animatedIndicatorWidth,
                        height = indicatorHeight
                    )
                )*/

        val labelPlaceable =
            label?.let {
                measurables
                    .first { it.layoutId == LabelLayoutIdTag }
                    .measure(
                        // Measure with loose constraints for height as we don't want the label to
                        // take up more space than it needs,
                        constraints.copy(minHeight = 0)
                    )
            }

        if (label == null) {
            placeIcon(iconPlaceable, constraints)
        } else {
            placeLabelAndIcon(
                labelPlaceable!!,
                iconPlaceable,
                constraints,
                alwaysShowLabel,
                animationProgress
            )
        }
    }
}

/**
 * Places the provided [Placeable]s in the center of the provided [constraints].
 */
private fun MeasureScope.placeIcon(
    iconPlaceable: Placeable,
    constraints: Constraints
): MeasureResult {
    val width = constraints.maxWidth
    val height = constraints.maxHeight

    val iconX = (width - iconPlaceable.width) / 2
    val iconY = (height - iconPlaceable.height) / 2

    return layout(width, height) {
        iconPlaceable.placeRelative(iconX, iconY)
    }
}

/**
 * Places the provided [Placeable]s in the correct position, depending on [alwaysShowLabel] and
 * [animationProgress].
 *
 * When [alwaysShowLabel] is true, the positions do not move. The [iconPlaceable] will be placed
 * near the top of the item and the [labelPlaceable] will be placed near the bottom, according to
 * the spec.
 *
 * When [animationProgress] is 1 (representing the selected state), the positions will be the same
 * as above.
 *
 * Otherwise, when [animationProgress] is 0, [iconPlaceable] will be placed in the center, like in
 * [placeIcon], and [labelPlaceable] will not be shown.
 *
 * When [animationProgress] is animating between these values, [iconPlaceable] and [labelPlaceable]
 * will be placed at a corresponding interpolated position.
 *
 * [indicatorRipplePlaceable] and [indicatorPlaceable] will always be placed in such a way that to
 * share the same center as [iconPlaceable].
 *
 * @param labelPlaceable text label placeable inside this item
 * @param iconPlaceable icon placeable inside this item
 * @param indicatorRipplePlaceable indicator ripple placeable inside this item
 * @param indicatorPlaceable indicator placeable inside this item, if it exists
 * @param constraints constraints of the item
 * @param alwaysShowLabel whether to always show the label for this item. If true, icon and label
 * positions will not change. If false, positions transition between 'centered icon with no label'
 * and 'top aligned icon with label'.
 * @param animationProgress progress of the animation, where 0 represents the unselected state of
 * this item and 1 represents the selected state. Values between 0 and 1 interpolate positions of
 * the icon and label.
 */
private fun MeasureScope.placeLabelAndIcon(
    labelPlaceable: Placeable,
    iconPlaceable: Placeable,
    constraints: Constraints,
    alwaysShowLabel: Boolean,
    animationProgress: Float,
): MeasureResult {
    val height = constraints.maxHeight

    // Label should be `ItemVerticalPadding` from the bottom
    val labelY = height - labelPlaceable.height - NavigationBarItemVerticalPadding.roundToPx()

    // Icon (when selected) should be `ItemVerticalPadding` from the top
    val selectedIconY = NavigationBarItemVerticalPadding.roundToPx()
    val unselectedIconY =
        if (alwaysShowLabel) selectedIconY else (height - iconPlaceable.height) / 2

    // How far the icon needs to move between unselected and selected states.
    val iconDistance = unselectedIconY - selectedIconY

    // The interpolated fraction of iconDistance that all placeables need to move based on
    // animationProgress.
    val offset = (iconDistance * (1 - animationProgress)).roundToInt()

    val containerWidth = constraints.maxWidth

    val labelX = (containerWidth - labelPlaceable.width) / 2
    val iconX = (containerWidth - iconPlaceable.width) / 2

    return layout(containerWidth, height) {
        if (alwaysShowLabel || animationProgress != 0f) {
            labelPlaceable.placeRelative(labelX, labelY + offset)
        }
        iconPlaceable.placeRelative(iconX, selectedIconY + offset)
    }
}

@Stable
class HandbookNavigationBarItemColors internal constructor(
    private val selectedIconColor: Color,
    private val selectedTextColor: Color,
    private val selectedIndicatorColor: Color,
    private val unselectedIconColor: Color,
    private val unselectedTextColor: Color,
    private val disabledIconColor: Color,
    private val disabledTextColor: Color,
) {

    /**
     * Represents the icon color for this item, depending on whether it is [selected].
     *
     * @param selected whether the item is selected
     * @param enabled whether the item is enabled
     */
    @Composable
    internal fun iconColor(selected: Boolean, enabled: Boolean): State<Color> {
        val targetValue = when {
            !enabled -> disabledIconColor
            selected -> selectedIconColor
            else -> unselectedIconColor
        }
        return animateColorAsState(
            targetValue = targetValue,
            animationSpec = tween(ItemAnimationDurationMillis),
            label = "Icon Color State"
        )
    }

    /**
     * Represents the text color for this item, depending on whether it is [selected].
     *
     * @param selected whether the item is selected
     * @param enabled whether the item is enabled
     */
    @Composable
    internal fun textColor(selected: Boolean, enabled: Boolean): State<Color> {
        val targetValue = when {
            !enabled -> disabledTextColor
            selected -> selectedTextColor
            else -> unselectedTextColor
        }
        return animateColorAsState(
            targetValue = targetValue,
            animationSpec = tween(ItemAnimationDurationMillis),
            label = "Text Color State"
        )
    }

    /** Represents the color of the indicator used for selected items. */
    internal val indicatorColor: Color
        get() = selectedIndicatorColor

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is HandbookNavigationBarItemColors) return false

        if (selectedIconColor != other.selectedIconColor) return false
        if (unselectedIconColor != other.unselectedIconColor) return false
        if (selectedTextColor != other.selectedTextColor) return false
        if (unselectedTextColor != other.unselectedTextColor) return false
        if (selectedIndicatorColor != other.selectedIndicatorColor) return false
        if (disabledIconColor != other.disabledIconColor) return false
        if (disabledTextColor != other.disabledTextColor) return false

        return true
    }
    override fun hashCode(): Int {
        var result = selectedIconColor.hashCode()
        result = 31 * result + unselectedIconColor.hashCode()
        result = 31 * result + selectedTextColor.hashCode()
        result = 31 * result + unselectedTextColor.hashCode()
        result = 31 * result + selectedIndicatorColor.hashCode()
        result = 31 * result + disabledIconColor.hashCode()
        result = 31 * result + disabledTextColor.hashCode()

        return result
    }
}

object HandbookNavigationBarItemDefaults {
    @Composable
    fun colors(
        selectedIconColor: Color = HandbookNavigationBarItemDefaults.selectedIconColor,
        selectedTextColor: Color = HandbookNavigationBarItemDefaults.selectedTextColor,
        indicatorColor: Color = HandbookNavigationBarItemDefaults.indicatorColor,
        unselectedIconColor: Color = HandbookNavigationBarItemDefaults.unselectedIconColor,
        unselectedTextColor: Color = HandbookNavigationBarItemDefaults.unselectedTextColor,
        disabledIconColor: Color = HandbookNavigationBarItemDefaults.disabledIconColor,
        disabledTextColor: Color = HandbookNavigationBarItemDefaults.disabledTextColor,
    ): HandbookNavigationBarItemColors = HandbookNavigationBarItemColors(
        selectedIconColor = selectedIconColor,
        selectedTextColor = selectedTextColor,
        selectedIndicatorColor = indicatorColor,
        unselectedIconColor = unselectedIconColor,
        unselectedTextColor = unselectedTextColor,
        disabledIconColor = disabledIconColor,
        disabledTextColor = disabledTextColor,
    )

    val selectedIconColor = Color.White
    val selectedTextColor = Color.White
    val indicatorColor = Color(0xFFFF0000)
    val unselectedIconColor = Color(0xFF6C7078)
    val unselectedTextColor = Color(0xFF6C7078)
    val disabledIconColor = Color(0xFF6C7078)
    val disabledTextColor = Color(0xFF6C7078)
}

private val IconSize = 24.0.dp

private val ActiveIndicatorWidth = 64.dp

private val ActiveIndicatorHeight = 32.dp

private val ActiveIndicatorShape = RoundedCornerShape(defaultCornerSize)

private const val IndicatorRippleLayoutIdTag: String = "indicatorRipple"

private const val IndicatorLayoutIdTag: String = "indicator"

private const val IconLayoutIdTag: String = "icon"

private const val LabelLayoutIdTag: String = "label"

private const val ItemAnimationDurationMillis: Int = 100

/*@VisibleForTesting*/
internal val NavigationBarItemHorizontalPadding: Dp = 8.dp

/*@VisibleForTesting*/
internal val NavigationBarItemVerticalPadding: Dp = 8.dp

private val IndicatorHorizontalPadding: Dp =
    (ActiveIndicatorWidth - IconSize) / 2

private val IndicatorVerticalPadding: Dp =
    (ActiveIndicatorHeight - IconSize) / 2

private val IndicatorVerticalOffset: Dp = 12.dp