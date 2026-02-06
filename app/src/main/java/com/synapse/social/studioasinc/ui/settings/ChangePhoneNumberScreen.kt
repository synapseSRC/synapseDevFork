package com.synapse.social.studioasinc.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePhoneNumberScreen(
    viewModel: ChangePhoneNumberViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Number") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = {
            if (state.error != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(state.error ?: "")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state.step) {
                PhoneChangeStep.ENTER_PHONE -> EnterPhoneContent(
                    state = state,
                    onCountryCodeChange = viewModel::onCountryCodeChange,
                    onPhoneChange = viewModel::onPhoneNumberChange,
                    onSendCode = viewModel::sendVerificationCode
                )
                PhoneChangeStep.VERIFY_CODE -> VerifyCodeContent(
                    state = state,
                    onCodeChange = viewModel::onVerificationCodeChange,
                    onVerify = viewModel::verifyCode,
                    onResend = viewModel::resendCode
                )
                PhoneChangeStep.SUCCESS -> SuccessContent(
                    onBackClick = onBackClick
                )
            }

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun EnterPhoneContent(
    state: PhoneChangeState,
    onCountryCodeChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSendCode: () -> Unit
) {
    var showCountryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Enter your new phone number",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Country Code Picker
            OutlinedTextField(
                value = state.countryCode,
                onValueChange = {},
                label = { Text("Code") },
                readOnly = true,
                modifier = Modifier
                    .width(100.dp)
                    .clickable { showCountryDialog = true },
                enabled = false, // Make it look read-only but clickable via Box overlay if needed, or just clickable modifier
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // Overlay to catch clicks for read-only text field
            // Better approach: Box wrapping
        }
        // Retrying the Row with proper clickable behavior
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
             Box(modifier = Modifier.width(100.dp).clickable { showCountryDialog = true }) {
                 OutlinedTextField(
                     value = state.countryCode,
                     onValueChange = {},
                     label = { Text("Code") },
                     readOnly = true,
                     enabled = false,
                     modifier = Modifier.fillMaxWidth(),
                     colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                     )
                 )
             }

            OutlinedTextField(
                value = state.phoneNumber,
                onValueChange = onPhoneChange,
                label = { Text("Phone Number") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                singleLine = true
            )
        }

        Button(
            onClick = onSendCode,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.phoneNumber.length >= 7
        ) {
            Text("Send Verification Code")
        }
    }

    if (showCountryDialog) {
        CountryCodeDialog(
            onDismiss = { showCountryDialog = false },
            onSelect = {
                onCountryCodeChange(it)
                showCountryDialog = false
            }
        )
    }
}

@Composable
fun VerifyCodeContent(
    state: PhoneChangeState,
    onCodeChange: (String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Enter Verification Code",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "We sent a code to ${state.countryCode} ${state.phoneNumber}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = state.verificationCode,
            onValueChange = onCodeChange,
            label = { Text("6-digit Code") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onVerify,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.verificationCode.length == 6
        ) {
            Text("Verify")
        }

        TextButton(
            onClick = onResend,
            enabled = state.canResend
        ) {
            if (state.canResend) {
                Text("Resend Code")
            } else {
                Text("Resend in ${state.resendTimer}s")
            }
        }
    }
}

@Composable
fun SuccessContent(
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Phone Number Updated!",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your phone number has been successfully updated.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done")
        }
    }
}

@Composable
fun CountryCodeDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val commonCountries = listOf(
        "United States (+1)" to "+1",
        "Canada (+1)" to "+1",
        "United Kingdom (+44)" to "+44",
        "India (+91)" to "+91",
        "Australia (+61)" to "+61",
        "Germany (+49)" to "+49",
        "France (+33)" to "+33",
        "Japan (+81)" to "+81",
        "China (+86)" to "+86",
        "Brazil (+55)" to "+55",
        "Mexico (+52)" to "+52",
        "South Africa (+27)" to "+27"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Country Code",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(commonCountries) { (name, code) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(code) }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(name)
                        }
                        HorizontalDivider()
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
