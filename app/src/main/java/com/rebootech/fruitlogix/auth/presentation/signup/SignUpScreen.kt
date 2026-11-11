package com.rebootech.fruitlogix.auth.presentation.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily

@Composable
fun SignUpScreen(
    onNavigateBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FruitLogixTheme.colors.surfaceDark),
        contentAlignment = Alignment.Center
    ) {
        FruitLogixCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.auth_signup_title),
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = FruitLogixTheme.typography.displayMedium,
                    color = FruitLogixTheme.colors.textOnDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.auth_signup_subtitle),
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = FruitLogixTheme.colors.textMuted
                )

                Spacer(modifier = Modifier.height(24.dp))

                SecondaryButton(
                    text = stringResource(R.string.auth_back_to_login),
                    onClick = onNavigateBackToLogin,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

