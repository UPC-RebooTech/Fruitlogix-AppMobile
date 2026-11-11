package com.rebootech.fruitlogix.auth.presentation.login

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.auth.data.DataStoreSessionRepository
import com.rebootech.fruitlogix.auth.data.FakeAuthRepository
import com.rebootech.fruitlogix.auth.domain.model.AuthResult
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionRepo = DataStoreSessionRepository.getInstance(context)
    val authRepo = FakeAuthRepository.instance

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
                    text = stringResource(R.string.auth_login_title),
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = FruitLogixTheme.typography.displayMedium,
                    color = FruitLogixTheme.colors.textOnDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.auth_login_subtitle),
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = FruitLogixTheme.colors.textMuted
                )

                Spacer(modifier = Modifier.height(24.dp))

                // DEV ONLY shortcuts for testing
                PrimaryButton(
                    text = "DEV: Entrar como Productor",
                    onClick = {
                        scope.launch {
                            val res = authRepo.signIn("alexandra.jimenez", "Producer123")
                            if (res is AuthResult.Success) {
                                sessionRepo.save(res.session, remember = true)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                PrimaryButton(
                    text = "DEV: Entrar como Distribuidor",
                    onClick = {
                        scope.launch {
                            val res = authRepo.signIn("carlos.mendoza", "Distributor123")
                            if (res is AuthResult.Success) {
                                sessionRepo.save(res.session, remember = true)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                SecondaryButton(
                    text = stringResource(R.string.auth_sign_up_button),
                    onClick = onNavigateToSignUp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

