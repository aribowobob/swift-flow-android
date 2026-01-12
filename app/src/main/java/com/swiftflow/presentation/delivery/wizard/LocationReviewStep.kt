package com.swiftflow.presentation.delivery.wizard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.swiftflow.domain.model.CreateDeliveryWizardState

@Composable
fun LocationReviewStep(
    state: CreateDeliveryWizardState,
    onUpdateLocationName: (String) -> Unit,
    onUpdateStreet: (String) -> Unit,
    onUpdateDistrict: (String) -> Unit,
    onUpdateCity: (String) -> Unit,
    onUpdateRegion: (String) -> Unit,
    onUpdateLatitude: (String) -> Unit,
    onUpdateLongitude: (String) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = "Review Location",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Review and update the delivery location information. All fields are required.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Coordinates (read-only display)
        if (state.latitude.isNotBlank() && state.longitude.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                OutlinedTextField(
                    value = state.latitude,
                    onValueChange = onUpdateLatitude,
                    label = { Text("Latitude") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = state.longitude,
                    onValueChange = onUpdateLongitude,
                    label = { Text("Longitude") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }

        // Location Name
        OutlinedTextField(
            value = state.locationName,
            onValueChange = onUpdateLocationName,
            label = { Text("Location Name *") },
            placeholder = { Text("e.g., Toko Makmur Jaya") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = state.locationName.isBlank() && state.error != null
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Street
        OutlinedTextField(
            value = state.street,
            onValueChange = onUpdateStreet,
            label = { Text("Street *") },
            placeholder = { Text("e.g., Jl. Pemuda No. 123") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = state.street.isBlank() && state.error != null
        )

        Spacer(modifier = Modifier.height(12.dp))

        // District
        OutlinedTextField(
            value = state.district,
            onValueChange = onUpdateDistrict,
            label = { Text("District *") },
            placeholder = { Text("e.g., Kec. Semarang Tengah") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = state.district.isBlank() && state.error != null
        )

        Spacer(modifier = Modifier.height(12.dp))

        // City
        OutlinedTextField(
            value = state.city,
            onValueChange = onUpdateCity,
            label = { Text("City *") },
            placeholder = { Text("e.g., Kota Semarang") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = state.city.isBlank() && state.error != null
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Region
        OutlinedTextField(
            value = state.region,
            onValueChange = onUpdateRegion,
            label = { Text("Region *") },
            placeholder = { Text("e.g., Jawa Tengah") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = state.region.isBlank() && state.error != null
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Next button
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Next")
            }
        }
    }
}
