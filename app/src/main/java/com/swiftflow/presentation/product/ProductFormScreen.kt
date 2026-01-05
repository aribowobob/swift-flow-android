package com.swiftflow.presentation.product

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swiftflow.domain.model.Product
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    productId: Int? = null,
    onNavigateBack: () -> Unit,
    viewModel: ProductViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    var sku by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var isFormInitialized by remember { mutableStateOf(false) }

    // Coroutine scope for snackbar
    val scope = rememberCoroutineScope()

    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }

    val isEditMode = productId != null
    val title = if (isEditMode) "Edit Product" else "Create Product"

    // Load product data if in edit mode
    LaunchedEffect(productId, state.products) {
        if (productId != null && !isFormInitialized && state.products.isNotEmpty()) {
            val product = state.products.find { it.id == productId }
            product?.let {
                sku = it.sku
                name = it.name
                unit = it.unit
                isFormInitialized = true
            }
        }
    }

    // Handle successful operation - show snackbar then navigate back
    LaunchedEffect(state.operationSuccess, state.successMessage) {
        if (state.operationSuccess && state.successMessage != null) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = state.successMessage ?: "",
                    duration = SnackbarDuration.Short
                )
                delay(1500) // Wait for snackbar to be visible
                viewModel.clearSuccessMessage()
                viewModel.clearOperationSuccess()
                onNavigateBack()
            }
        }
    }

    // Show error message
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Temporary test button
                    Button(onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "lorem ipsum",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }) {
                        Text("Test")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            OutlinedTextField(
                value = sku,
                onValueChange = { sku = it },
                label = { Text("SKU") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isEditMode // SKU cannot be changed in edit mode
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = unit,
                onValueChange = { unit = it },
                label = { Text("Unit (e.g., Carton, Box, Pcs)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (isEditMode && productId != null) {
                        // Update product
                        viewModel.updateProduct(productId, name, unit)
                    } else {
                        // Create product
                        viewModel.createProduct(sku, name, unit)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = sku.isNotBlank() && name.isNotBlank() && unit.isNotBlank() && !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isEditMode) "Update Product" else "Create Product")
            }
        }

            // Snackbar at the top
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                snackbar = { snackbarData ->
                    Snackbar(
                        snackbarData = snackbarData,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            )
        }
    }
}
