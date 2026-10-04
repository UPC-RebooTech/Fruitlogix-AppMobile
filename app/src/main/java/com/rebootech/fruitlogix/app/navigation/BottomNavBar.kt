package com.rebootech.fruitlogix.app.navigation

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.fleetManagement.FleetManagementRoutes
import com.rebootech.fruitlogix.infrastructureIot.InfrastructureIotRoutes
import com.rebootech.fruitlogix.profilesManagement.ProfilesManagementRoutes
import com.rebootech.fruitlogix.qualityControl.QualityControlRoutes
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.RobotoFontFamily
import kotlinx.coroutines.delay

/**
 * Data model for items in the simplified expanded More menu.
 */
data class MoreMenuItem(
    val id: String,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
    val route: String,
    val badgeCount: Int? = null
)

/**
 * Set of routes belonging to the 5 More destinations.
 */
private val moreSectionRoutes = setOf(
    InfrastructureIotRoutes.SensorsAlerts,
    QualityControlRoutes.QualityControl,
    ProfilesManagementRoutes.Producers,
    FleetManagementRoutes.FleetResources,
    ProfilesManagementRoutes.Profile
)

/**
 * FruitLogix Bottom Navigation Bar.
 *
 * BAR LAYOUT:
 * - 5 slots: Home, Orders, Fleet, Invoices, and [More button].
 * - First 4 are regular tabs (icon + 12sp label, textMuted inactive, white label + lime pill when active).
 * - 5th slot (far RIGHT end) is a lime circular button (52dp) with a menu icon, cleaner look (no label below).
 *
 * EXPANDED STATE (More button tapped):
 * - Icon animates between menu and close (X) with rotation + crossfade.
 * - Dark scrim (~60% alpha) covers screen above the bar; tapping it or Back closes the menu.
 * - Vertical column of secondary items appears aligned to the RIGHT edge over the More button center axis.
 * - Staggered entrance/exit animation (slide up + fade, ~50ms delay between items, tween 250ms; reverse on close).
 * - UNIFIED STYLE: Every bubble uses surface-dark background (#1F2D23) with thin lime outline glow and lime icon.
 *   Label pill uses translucent surface-dark with white text (Roboto 14sp). Lime is the only accent (red only for critical badge dot).
 *
 * TOP-LEVEL NAVIGATION BEHAVIOR:
 * - The 5 More destinations (Sensors, Quality, Producers, Drivers, Profile) behave as top-level screens:
 *   Bottom navigation bar stays visible, navigation uses launchSingleTop and saveState/restoreState.
 * - While active, none of the 4 tabs shows the lime pill highlight; the More button shows an active lime ring.
 */
@Composable
fun BottomNavBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    initialMenuOpen: Boolean = false
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var isMenuOpen by remember { mutableStateOf(initialMenuOpen) }
    var isPopupMounted by remember { mutableStateOf(initialMenuOpen) }

    val isMoreSectionActive = currentDestination?.hierarchy?.any { it.route in moreSectionRoutes } == true

    LaunchedEffect(isMenuOpen) {
        if (isMenuOpen) {
            isPopupMounted = true
        } else {
            delay(350)
            isPopupMounted = false
        }
    }

    BackHandler(enabled = isMenuOpen) {
        isMenuOpen = false
    }

    // 5 Simplified Items: Sensors, Quality, Producers, Drivers, Profile
    val moreMenuItems = remember {
        listOf(
            MoreMenuItem(
                id = "sensors",
                labelRes = R.string.menu_label_sensors,
                iconRes = R.drawable.ic_sensor_waves,
                route = InfrastructureIotRoutes.SensorsAlerts
            ),
            MoreMenuItem(
                id = "quality",
                labelRes = R.string.menu_label_quality,
                iconRes = R.drawable.ic_shield_check,
                route = QualityControlRoutes.QualityControl
            ),
            MoreMenuItem(
                id = "producers",
                labelRes = R.string.menu_label_producers,
                iconRes = R.drawable.ic_producers,
                route = ProfilesManagementRoutes.Producers
            ),
            MoreMenuItem(
                id = "drivers",
                labelRes = R.string.menu_label_drivers,
                iconRes = R.drawable.ic_dispatch_truck,
                route = FleetManagementRoutes.FleetResources
            ),
            MoreMenuItem(
                id = "profile",
                labelRes = R.string.menu_label_profile,
                iconRes = R.drawable.ic_contact_driver,
                route = ProfilesManagementRoutes.Profile
            )
        )
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val totalWidth = maxWidth
        val bubbleEndPadding = ((totalWidth / 5) - 48.dp) / 2

        // Expanded State Overlay (Scrim + Secondary Menu Column)
        if (isPopupMounted) {
            Popup(
                onDismissRequest = { isMenuOpen = false },
                properties = PopupProperties(
                    focusable = true,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                val scrimAlpha by animateFloatAsState(
                    targetValue = if (isMenuOpen) 0.60f else 0f,
                    animationSpec = tween(250),
                    label = "ScrimAlphaAnimation"
                )

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Dark Scrim (~60% alpha) covering the screen above the bar
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = scrimAlpha))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                isMenuOpen = false
                            }
                    )

                    // Vertical Column of Secondary Items aligned to the RIGHT edge over More button axis
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .navigationBarsPadding()
                            .padding(bottom = 82.dp, end = bubbleEndPadding)
                    ) {
                        moreMenuItems.reversed().forEachIndexed { reverseIndex, item ->
                            val originalIndex = moreMenuItems.size - 1 - reverseIndex
                            SecondaryNavItemRow(
                                item = item,
                                indexFromBottom = originalIndex,
                                totalItems = moreMenuItems.size,
                                isMenuOpen = isMenuOpen,
                                onItemClick = {
                                    isMenuOpen = false
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Bottom Navigation Surface (72dp + insets)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = FruitLogixTheme.colors.appbar,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(72.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 4 Regular Primary Tabs
                primaryNavRoutes.forEach { routeItem ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == routeItem.route } == true
                    BottomNavItem(
                        routeItem = routeItem,
                        isSelected = isSelected && !isMenuOpen && !isMoreSectionActive,
                        onClick = {
                            if (isMenuOpen) {
                                isMenuOpen = false
                            }
                            if (!isSelected) {
                                navController.navigate(routeItem.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 5th Slot: More Button (52dp lime circular button, vertically centered)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    MoreButtonSlot(
                        isMenuOpen = isMenuOpen,
                        isMoreSectionActive = isMoreSectionActive,
                        onClick = { isMenuOpen = !isMenuOpen }
                    )
                }
            }
        }
    }
}

/**
 * 5th Slot Lime Circular Button (52dp) with animated Menu <-> Close icon.
 * Shows active lime ring border when a More section destination is active.
 */
@Composable
private fun MoreButtonSlot(
    isMenuOpen: Boolean,
    isMoreSectionActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isMenuOpen) 180f else 0f,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "MoreButtonRotation"
    )

    Box(
        modifier = modifier
            .size(52.dp)
            .shadow(
                elevation = if (isMoreSectionActive || isMenuOpen) 8.dp else 4.dp,
                shape = CircleShape,
                spotColor = FruitLogixTheme.colors.primary
            )
            .clip(CircleShape)
            .background(FruitLogixTheme.colors.primary)
            .then(
                if (isMoreSectionActive && !isMenuOpen) {
                    Modifier.border(BorderStroke(2.5.dp, Color.White), CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(
            targetState = isMenuOpen,
            animationSpec = tween(durationMillis = 250),
            label = "MoreButtonIconCrossfade"
        ) { open ->
            AppIcon(
                id = if (open) R.drawable.ic_close else R.drawable.ic_menu,
                contentDescription = if (open) "Close menu" else "More options",
                tint = FruitLogixTheme.colors.onPrimary,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer { rotationZ = rotation }
            )
        }
    }
}

/**
 * Unified Style Secondary Item Row in the expanded vertical column.
 * Layout: [translucent surface-dark pill label on LEFT] + [48dp bubble with lime outline & lime icon on RIGHT].
 */
@Composable
private fun SecondaryNavItemRow(
    item: MoreMenuItem,
    indexFromBottom: Int,
    totalItems: Int,
    isMenuOpen: Boolean,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enterDelay = indexFromBottom * 50
    val exitDelay = (totalItems - 1 - indexFromBottom) * 50

    AnimatedVisibility(
        visible = isMenuOpen,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(durationMillis = 250, delayMillis = enterDelay, easing = FastOutSlowInEasing)
        ) + fadeIn(
            animationSpec = tween(durationMillis = 250, delayMillis = enterDelay)
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(durationMillis = 200, delayMillis = exitDelay, easing = FastOutLinearInEasing)
        ) + fadeOut(
            animationSpec = tween(durationMillis = 200, delayMillis = exitDelay)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onItemClick
            )
        ) {
            // Label Pill: Translucent surface-dark background with white text (Roboto 14sp)
            Surface(
                shape = CircleShape,
                color = Color(0xEE1F2D23),
                border = BorderStroke(1.dp, FruitLogixTheme.colors.primary.copy(alpha = 0.35f)),
                shadowElevation = 4.dp
            ) {
                Text(
                    text = stringResource(id = item.labelRes),
                    color = FruitLogixTheme.colors.textOnDark,
                    fontFamily = RobotoFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Icon Bubble (48dp): Surface-dark background with thin lime outline glow & lime icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape, spotColor = FruitLogixTheme.colors.primary)
                    .clip(CircleShape)
                    .background(FruitLogixTheme.colors.surfaceDark)
                    .border(BorderStroke(1.dp, FruitLogixTheme.colors.primary.copy(alpha = 0.5f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(
                    id = item.iconRes,
                    contentDescription = stringResource(id = item.labelRes),
                    tint = FruitLogixTheme.colors.primary,
                    modifier = Modifier.size(22.dp)
                )

                // Optional critical red badge dot
                if (item.badgeCount != null && item.badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(FruitLogixTheme.colors.dangerStrong)
                    )
                }
            }
        }
    }
}

/**
 * Regular Tab Item for the 4 main bottom navigation routes.
 */
@Composable
private fun BottomNavItem(
    routeItem: Route,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(FruitLogixTheme.shapes.Pill)
                .background(pillBackgroundColor)
                .padding(horizontal = 14.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(
                id = routeItem.iconRes,
                contentDescription = stringResource(id = routeItem.labelRes),
                tint = iconTintColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = stringResource(id = routeItem.labelRes),
            color = labelTextColor,
            fontFamily = RobotoFontFamily,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1F2D23)
@Composable
fun BottomNavBarPreview() {
    FruitLogixTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(FruitLogixTheme.colors.bg)
        ) {
            val navController = rememberNavController()
            BottomNavBar(
                navController = navController,
                initialMenuOpen = false
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1F2D23)
@Composable
fun BottomNavBarOpenMenuPreview() {
    FruitLogixTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FruitLogixTheme.colors.bg)
        ) {
            val navController = rememberNavController()
            BottomNavBar(
                navController = navController,
                initialMenuOpen = true
            )
        }
    }
}
