package com.handbook.app.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.handbook.app.R
import com.handbook.app.core.domain.model.ShopData
import com.handbook.app.getInitialCharacter
import com.handbook.app.navigation.NavigationDrawerDestination
import com.handbook.app.titleCase
import com.handbook.app.ui.defaultCornerSize
import com.handbook.app.ui.insetSmall
import com.handbook.app.ui.insetVeryLarge
import com.handbook.app.ui.insetVerySmall
import com.handbook.app.ui.theme.HandbookTheme
import com.handbook.app.ui.theme.HandbookTheme
import com.handbook.app.ui.theme.TextSecondary

@Composable
internal fun DefaultNavigationDrawer(
    shopData: ShopData,
    destinations: List<NavigationDrawerDestination>,
    onNavigateToDestination: (NavigationDrawerDestination) -> Unit,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(0.8f)
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(insetSmall),
        ) {
            StoreOverview(shopData = shopData)
        }
        // Spacer(modifier = Modifier.height(spacerSizeTiny))
        destinations.forEach { destination ->
            val selected = currentDestination.isDrawerDestinationInHierarchy(destination)
            DefaultNavigationDrawerItem(
                label = { Text(text = stringResource(id = destination.labelTextId)) },
                selected = selected,
                icon = {
                    Icon(
                        painter = painterResource(id = destination.selectedIcon),
                        contentDescription = stringResource(id = destination.iconTextId),
                    )
                },
                onClick = { onNavigateToDestination(destination) }
            )
        }
    }
}

@Composable
internal fun DefaultNavigationDrawerItem(
    label: @Composable () -> Unit,
    selected: Boolean,
    icon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    NavigationDrawerItem(
        label = label,
        selected = selected,
        onClick = onClick,
        icon = icon,
        shape = RoundedCornerShape(defaultCornerSize),
        modifier = Modifier.padding(insetSmall),
        colors = NavigationDrawerItemDefaults.colors(
            selectedIconColor = Color(0xFF71839B),
            unselectedIconColor = Color(0xFF71839B),
            selectedTextColor = Color(0xFF71839B),
            unselectedTextColor = Color(0xFF71839B),
            selectedContainerColor = Color(0x2671839B)
        )
    )
}
@Composable
private fun StoreOverview(
    shopData: ShopData,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(insetSmall),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (shopData.thumbnail.isBlank()) {
            Box(modifier = Modifier
                .size(56.dp)
                .aspectRatio(1f)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
                contentAlignment = Alignment.Center,
            ) {
                val initials = getInitialCharacter(shopData.name, 2)
                Text(
                    text = initials,
                    style = MaterialTheme.typography.labelLarge
                        .copy(fontWeight = FontWeight.W700, letterSpacing = 1.sp)
                )
            }
        } else {
            // TODO: load image
            Box(modifier = Modifier
                .size(56.dp)
                .background(color = Color.LightGray, shape = CircleShape))
        }

        Column {
            Text(
                text = shopData.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = shopData.category,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun NavDestination?.isDrawerDestinationInHierarchy(destination: NavigationDrawerDestination): Boolean {
    return this?.hierarchy
        ?.filterNot { it.route?.contains("_graph") == true }
        ?.any {
            it.route?.contains(destination.name, ignoreCase = true) ?: false
        } ?: false
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(device = "id:pixel_3a", showBackground = true)
private fun DefaultNavigationDrawerPreview() {
    HandbookTheme {
        Column(
            Modifier.fillMaxWidth()
        ) {
            val sampleShopData = ShopData(
                id = "0",
                name = "Sarathas Clothing",
                thumbnail = "",
                category = "Textile & Cloting",
                description = "Sarees, Shirts, Pants",
                address = "600097",
                image = "",
            )
            DefaultNavigationDrawer(
                shopData = sampleShopData,
                destinations = NavigationDrawerDestination.values().asList(),
                currentDestination = NavDestination("my_store"),
                onNavigateToDestination = {},
            )
        }
    }
}

@Composable
@Preview(widthDp = 230, showBackground = true)
private fun StoreDecorPreview() {
    HandbookTheme {
        StoreOverview(shopData =
            ShopData(
                id = "0",
                name = "Sarathas Clothing",
                thumbnail = "",
                category = "Textile & Cloting",
                description = "Sarees, Shirts, Pants",
                address = "600097",
                image = "",
            )
        )
    }
}