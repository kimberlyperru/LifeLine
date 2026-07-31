package com.perru.lifeline.presentation.hospital

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.perru.lifeline.R
import com.perru.lifeline.domain.model.BloodComponent
import com.perru.lifeline.domain.model.BloodGroup
import com.perru.lifeline.domain.model.BloodRequest
import com.perru.lifeline.domain.model.UrgencyLevel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestScreen(
    viewModel: HospitalViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSubmitted: () -> Unit
) {
    val hospital by viewModel.currentUser.collectAsState()
    val createState by viewModel.createState.collectAsState()
    val crisisMode by viewModel.crisisMode.collectAsState()
    val scrollState = rememberScrollState()

    var bloodGroup by remember { mutableStateOf(BloodGroup.O_POS) }
    var component by remember { mutableStateOf(BloodComponent.WHOLE_BLOOD) }
    var unitsNeeded by remember { mutableStateOf("1") }
    var urgency by remember { mutableStateOf(UrgencyLevel.MODERATE) }
    var contactPhone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(crisisMode) {
        if (crisisMode) urgency = UrgencyLevel.CRITICAL
    }

    val isUnitsValid = unitsNeeded.isNotBlank() && (unitsNeeded.toIntOrNull() ?: 0) > 0
    val isPhoneValid = contactPhone.length >= 8 // Basic check

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    LaunchedEffect(createState.submitted) {
        if (createState.submitted) onSubmitted()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_blood_request)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            Text(stringResource(R.string.blood_group_needed), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(BloodGroup.entries) { group ->
                    FilterChip(
                        selected = bloodGroup == group,
                        onClick = { bloodGroup = group },
                        label = { Text(group.label) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.component), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(BloodComponent.entries) { c ->
                    FilterChip(
                        selected = component == c,
                        onClick = { component = c },
                        label = { Text(c.name.replace('_', ' ')) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.urgency), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(UrgencyLevel.entries) { u ->
                    FilterChip(
                        selected = urgency == u,
                        onClick = { if (!crisisMode) urgency = u },
                        label = { Text(u.label) },
                        enabled = !crisisMode || u == UrgencyLevel.CRITICAL
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = unitsNeeded,
                onValueChange = { if (it.all(Char::isDigit)) unitsNeeded = it },
                label = { Text(stringResource(R.string.units_needed)) },
                isError = !isUnitsValid && unitsNeeded.isNotEmpty(),
                supportingText = {
                    if (!isUnitsValid && unitsNeeded.isNotEmpty()) {
                        Text("Please enter a valid number of units")
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = contactPhone,
                onValueChange = { contactPhone = it },
                label = { Text(stringResource(R.string.contact_phone)) },
                isError = !isPhoneValid && contactPhone.isNotEmpty(),
                supportingText = {
                    if (!isPhoneValid && contactPhone.isNotEmpty()) {
                        Text("Please enter a valid phone number")
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.notes_optional)) },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.verification_doc_optional), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = stringResource(R.string.cd_selected_verification),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { imagePicker.launch("image/*") }) {
                    Text(stringResource(R.string.change_image))
                }
            } else {
                OutlinedButton(onClick = { imagePicker.launch("image/*") }) {
                    Icon(Icons.Filled.AddPhotoAlternate, contentDescription = stringResource(R.string.cd_upload_icon), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.upload_doc_btn))
                }
            }

            createState.errorMessage?.let {
                Spacer(Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val hospitalUser = hospital ?: return@Button
                    viewModel.submitRequest(
                        hospital = hospitalUser,
                        request = BloodRequest(
                            bloodGroup = bloodGroup,
                            component = component,
                            unitsNeeded = unitsNeeded.toIntOrNull() ?: 1,
                            urgency = urgency,
                            contactPhone = contactPhone,
                            notes = notes
                        ),
                        verificationImageUri = imageUri
                    )
                },
                enabled = !createState.isSubmitting && !createState.isUploading && 
                    isUnitsValid && isPhoneValid,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.large
            ) {
                if (createState.isSubmitting || createState.isUploading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(if (createState.isUploading) stringResource(R.string.uploading_image) else stringResource(R.string.posting_request))
                    }
                } else {
                    Text(stringResource(R.string.post_sos_request))
                }
            }
        }
    }
}
