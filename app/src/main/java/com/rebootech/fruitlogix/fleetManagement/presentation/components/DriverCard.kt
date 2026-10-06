package com.rebootech.fruitlogix.fleetManagement.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.fleetManagement.domain.model.Driver
import com.rebootech.fruitlogix.fleetManagement.domain.model.DriverAssignment
import com.rebootech.fruitlogix.fleetManagement.domain.model.DriverStatus
import com.rebootech.fruitlogix.fleetManagement.domain.service.LicenseValidity
import com.rebootech.fruitlogix.fleetManagement.domain.service.LicenseValidityState
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily
import java.time.LocalDate
import java.util.Locale

@Composable
fun DriverCard(
    driver: Driver,
    onDetailClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpired = driver.status == DriverStatus.EXPIRED
    val validityState = LicenseValidity.checkValidity(driver.licenseExpiry)

    val cardBg = if (isExpired) {
        Color(0xFF2A1C1C)
    } else {
        FruitLogixTheme.colors.surfaceDark
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (!isExpired) {
                    Modifier.clickable { onDetailClick(driver.id) }
                } else {
                    Modifier
                }
            ),
        shape = FruitLogixTheme.shapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = cardBg,
            contentColor = FruitLogixTheme.colors.textOnDark
        )
    ) {
        Column(
            modifier = Modifier.padding(FruitLogixTheme.spacing.sm)
        ) {
            // Top Row: Avatar + Name/Specialty + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Initials Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isExpired) {
                                FruitLogixTheme.colors.danger
                            } else {
                                FruitLogixTheme.colors.appbar
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = driver.initials,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        style = FruitLogixTheme.typography.titleMedium,
                        color = if (isExpired) {
                            FruitLogixTheme.colors.textOnDark
                        } else {
                            FruitLogixTheme.colors.primary
                        }
                    )
                }

                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))

                // Name & Specialty
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = driver.name,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        style = FruitLogixTheme.typography.titleMedium,
                        color = FruitLogixTheme.colors.textOnDark
                    )
                    Text(
                        text = "${driver.licenseClass} • ${driver.specialty}",
                        style = FruitLogixTheme.typography.bodySmall,
                        color = FruitLogixTheme.colors.textMuted
                    )
                }

                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))

                // Status Pill
                DriverStatusPill(status = driver.status)
            }

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))

            // Inner Rounded Row (Assignment Container)
            AssignmentContainer(
                driver = driver,
                validityState = validityState
            )

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))

            // Footer Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val formattedDate = String.format(
                    Locale.US,
                    "%02d/%d",
                    driver.licenseExpiry.monthValue,
                    driver.licenseExpiry.year
                )

                if (isExpired) {
                    Text(
                        text = stringResource(R.string.fleet_drivers_cannot_be_assigned_expired, formattedDate),
                        style = FruitLogixTheme.typography.bodySmall,
                        color = FruitLogixTheme.colors.dangerStrong
                    )
                } else {
                    Text(
                        text = stringResource(R.string.fleet_drivers_license_valid_until, formattedDate),
                        style = FruitLogixTheme.typography.bodySmall,
                        color = FruitLogixTheme.colors.textMuted
                    )

                    AppIcon(
                        id = R.drawable.ic_chevron_right,
                        contentDescription = "Details",
                        tint = FruitLogixTheme.colors.textMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverStatusPill(
    status: DriverStatus
) {
    val (bgColor, textColor, labelRes) = when (status) {
        DriverStatus.AVAILABLE -> Triple(
            FruitLogixTheme.colors.success.copy(alpha = 0.2f),
            FruitLogixTheme.colors.success,
            R.string.fleet_drivers_status_available
        )
        DriverStatus.ON_ROUTE -> Triple(
            FruitLogixTheme.colors.primary.copy(alpha = 0.2f),
            FruitLogixTheme.colors.primary,
            R.string.fleet_drivers_status_on_route
        )
        DriverStatus.OFF_DUTY -> Triple(
            FruitLogixTheme.colors.textMuted.copy(alpha = 0.2f),
            FruitLogixTheme.colors.textMuted,
            R.string.fleet_drivers_status_off_duty
        )
        DriverStatus.EXPIRED -> Triple(
            FruitLogixTheme.colors.dangerStrong.copy(alpha = 0.2f),
            FruitLogixTheme.colors.dangerStrong,
            R.string.fleet_drivers_status_expired
        )
    }

    Box(
        modifier = Modifier
            .clip(FruitLogixTheme.shapes.Pill)
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val prefix = if (status != DriverStatus.EXPIRED) "• " else ""
            Text(
                text = prefix + stringResource(labelRes),
                style = FruitLogixTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AssignmentContainer(
    driver: Driver,
    validityState: LicenseValidityState
) {
    val isExpired = driver.status == DriverStatus.EXPIRED
    val containerBg = if (isExpired) {
        Color(0xFF3B1A1A)
    } else {
        FruitLogixTheme.colors.appbar
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(containerBg)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    val iconRes = when (driver.status) {
                        DriverStatus.EXPIRED -> R.drawable.ic_fleet_warning
                        DriverStatus.OFF_DUTY -> R.drawable.ic_fleet_moon
                        else -> R.drawable.ic_fleet_truck
                    }
                    val iconTint = when (driver.status) {
                        DriverStatus.EXPIRED -> FruitLogixTheme.colors.dangerStrong
                        DriverStatus.OFF_DUTY -> FruitLogixTheme.colors.textMuted
                        else -> FruitLogixTheme.colors.primary
                    }

                    AppIcon(
                        id = iconRes,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    val textContent = when {
                        isExpired -> {
                            driver.currentAssignment?.cannotBeAssignedReason
                                ?: "Cannot be assigned • License expired"
                        }
                        driver.status == DriverStatus.OFF_DUTY -> {
                            driver.currentAssignment?.restPeriodEnds
                                ?: "Rest period ends tomorrow 06:00 PET"
                        }
                        driver.currentAssignment != null -> {
                            "Unit ${driver.currentAssignment.unitCode} • ${driver.currentAssignment.plateNumber}"
                        }
                        else -> {
                            stringResource(R.string.fleet_drivers_unassigned_ready)
                        }
                    }

                    Text(
                        text = textContent,
                        style = FruitLogixTheme.typography.bodyMedium,
                        color = if (isExpired) FruitLogixTheme.colors.dangerStrong else FruitLogixTheme.colors.textOnDark,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Amber Chip if Expiring Soon
                if (validityState is LicenseValidityState.ExpiringSoon) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(FruitLogixTheme.shapes.Pill)
                            .background(FruitLogixTheme.colors.warning.copy(alpha = 0.25f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.fleet_drivers_license_expires_in_days, validityState.daysRemaining),
                            style = FruitLogixTheme.typography.labelSmall,
                            color = FruitLogixTheme.colors.warning,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Route line if available
            driver.currentAssignment?.routeName?.let { route ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(
                        id = R.drawable.ic_logistics_route,
                        contentDescription = null,
                        tint = FruitLogixTheme.colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = route,
                        style = FruitLogixTheme.typography.bodySmall,
                        color = FruitLogixTheme.colors.textMuted
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun DriverCardListPreview() {
    FruitLogixTheme {
        Column(
            modifier = Modifier
                .background(FruitLogixTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Valid / On Route Card
            DriverCard(
                driver = Driver(
                    id = "1",
                    name = "Jorge Huamán",
                    licenseClass = "A-IIIc",
                    specialty = "Cold-chain certified",
                    licenseExpiry = LocalDate.of(2027, 11, 30),
                    status = DriverStatus.ON_ROUTE,
                    currentAssignment = DriverAssignment(
                        unitCode = "FL-102",
                        plateNumber = "BQK-482",
                        routeName = "Ica → Lima Central Hub"
                    )
                ),
                onDetailClick = {}
            )

            // Expiring Soon Card
            DriverCard(
                driver = Driver(
                    id = "3",
                    name = "Carlos Mendoza Jr.",
                    licenseClass = "A-IIIc",
                    specialty = "Freight senior",
                    licenseExpiry = LocalDate.now().plusDays(20),
                    status = DriverStatus.AVAILABLE,
                    currentAssignment = null
                ),
                onDetailClick = {}
            )

            // Expired Card
            DriverCard(
                driver = Driver(
                    id = "5",
                    name = "Ricardo Paredes",
                    licenseClass = "A-IIIc",
                    specialty = "Heavy cargo",
                    licenseExpiry = LocalDate.of(2023, 10, 12),
                    status = DriverStatus.EXPIRED,
                    currentAssignment = DriverAssignment(
                        cannotBeAssignedReason = "Cannot be assigned • License expired (12/10/2023)"
                    )
                ),
                onDetailClick = {}
            )
        }
    }
}
