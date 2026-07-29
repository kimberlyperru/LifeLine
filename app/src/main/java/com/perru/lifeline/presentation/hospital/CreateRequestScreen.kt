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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
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
    val scrollState = rememberScrollState()

    var bloodGroup by remember { mutableStateOf(BloodGroup.O_POS) }
    var component by remember { mutableStateOf(BloodComponent.WHOLE_BLOOD) }
    var unitsNeeded by remember { mutableStateOf("1") }
    var urgency by remember { mutableStateOf(UrgencyLevel.MODERATE) }
    var contactPhone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    LaunchedEffect(createState.submitted) {
        if (createState.submitted) onSubmitted()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New blood request") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            Text("Blood group needed", style = MaterialTheme.typography.labelLarge)
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
            Text("Component", style = MaterialTheme.typography.labelLarge)
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
            Text("Urgency", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(UrgencyLevel.entries) { u ->
                    FilterChip(
                        selected = urgency == u,
                        onClick = { urgency = u },
                        label = { Text(u.label) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = unitsNeeded,
                onValueChange = { if (it.all(Char::isDigit)) unitsNeeded = it },
                label = { Text("Units needed") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = contactPhone,
                onValueChange = { contactPhone = it },
                label = { Text("Contact phone") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(Modifier.height(16.dp))
            Text("Verification document (optional)", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Selected verification image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { imagePicker.launch("image/*") }) {
                    Text("Change image")
                }
            } else {
                OutlinedButton(onClick = { imagePicker.launch("image/*") }) {
                    Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Upload hospital badge or requisition")
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
                enabled = !createState.isSubmitting && !createState.isUploading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.large
            ) {
                if (createState.isSubmitting || createState.isUploading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(if (createState.isUploading) "Uploading image…" else "Posting request…")
                    }
                } else {
                    Text("Post SOS Request")
                }
            }
        }
    }
}
