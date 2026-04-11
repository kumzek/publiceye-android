package com.publiceye.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.publiceye.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    viewModel          : AuthViewModel,
    onSignUpSuccess    : () -> Unit,
    onNavigateToLogin  : () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var name            by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val passwordsMatch = password == confirmPassword || confirmPassword.isEmpty()
    val canSubmit = name.isNotBlank() && email.isNotBlank() &&
            password.length >= 8 && password == confirmPassword && !uiState.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sign_up)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))

            // Full name
            OutlinedTextField(
                value         = name,
                onValueChange = { name = it },
                label         = { Text(stringResource(R.string.full_name)) },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
            )

            Spacer(Modifier.height(12.dp))

            // Email
            OutlinedTextField(
                value         = email,
                onValueChange = { email = it },
                label         = { Text(stringResource(R.string.email)) },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
            )

            Spacer(Modifier.height(12.dp))

            // Password
            OutlinedTextField(
                value         = password,
                onValueChange = { password = it },
                label         = { Text(stringResource(R.string.password)) },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                        )
                    }
                },
                supportingText = {
                    if (password.isNotEmpty() && password.length < 8)
                        Text(stringResource(R.string.error_weak_password))
                },
                isError = password.isNotEmpty() && password.length < 8,
            )

            Spacer(Modifier.height(12.dp))

            // Confirm password
            OutlinedTextField(
                value         = confirmPassword,
                onValueChange = { confirmPassword = it },
                label         = { Text(stringResource(R.string.confirm_password)) },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (canSubmit) viewModel.signUp(name, email, password, onSignUpSuccess)
                    }
                ),
                isError = !passwordsMatch,
                supportingText = {
                    if (!passwordsMatch) Text(stringResource(R.string.error_passwords_mismatch))
                },
            )

            // API error
            if (uiState.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(28.dp))

            // Create Account button
            Button(
                onClick  = { viewModel.signUp(name, email, password, onSignUpSuccess) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled  = canSubmit,
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.sign_up))
                }
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text(stringResource(R.string.already_have_account))
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
