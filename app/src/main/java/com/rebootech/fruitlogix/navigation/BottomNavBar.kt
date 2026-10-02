package com.rebootech.fruitlogix.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.rebootech.fruitlogix.ui.components.AppIcon
import com.rebootech.fruitlogix.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.ui.theme.PoppinsFontFamily

/**
 * FruitLogix Bottom Navigation Bar.
 * Background: Appbar dark color (#2D3F33).
 * 5 tabs: Home, Orders, Fleet, Producers, More.
 * Active tab shows a lime pill highlight behind icon (on-primary icon, white label).
 * Inactive tab uses textMuted icon & label.
 */
@Composable
fun BottomNavBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(FruitLogixTheme.colors.appbar)
            .padding(vertical = FruitLogixTheme.spacing.xs),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        bottomNavRoutes.forEach { routeItem ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == routeItem.route } == true
            BottomNavItem(
                routeItem = routeItem,
                isSelected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(routeItem.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    routeItem: Route,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val pillBackgroundColor by animateColorAsState(
        targetValue = if (isSelected) FruitLogixTheme.colors.primary else Color.Transparent,
        label = "BottomNavPillAnimation"
    )

    val iconTintColor by animateColorAsState(
        targetValue = if (isSelected) FruitLogixTheme.colors.onPrimary else FruitLogixTheme.colors.textMuted,
        label = "BottomNavIconAnimation"
    )

    val labelTextColor by animateColorAsState(
        targetValue = if (isSelected) FruitLogixTheme.colors.textOnDark else FruitLogixTheme.colors.textMuted,
        label = "BottomNavTextAnimation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .defaultMinSize(minWidth = 56.dp, minHeight = 48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .clip(FruitLogixTheme.shapes.Pill)
                .background(pillBackgroundColor)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(
                id = routeItem.iconRes,
                contentDescription = stringResource(id = routeItem.labelRes),
                tint = iconTintColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = stringResource(id = routeItem.labelRes),
            color = labelTextColor,
            fontFamily = PoppinsFontFamily,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            style = FruitLogixTheme.typography.labelSmall
        )
    }
}

