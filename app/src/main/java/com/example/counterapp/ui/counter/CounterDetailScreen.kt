package com.example.counterapp.ui.counter

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.counterapp.util.TimeFormatter
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterDetailScreen(
    navController: NavController,
    viewModel: CounterDetailViewModel = hiltViewModel()
) {
    val counter by viewModel.counter.collectAsState()
    val liveElapsedMs by viewModel.liveElapsedMs.collectAsState()
    val isEditDialogOpen by viewModel.isEditDialogOpen.collectAsState()
    val incrementTrigger by viewModel.incrementTrigger.collectAsState()
    var showRenameDialog by remember { mutableStateOf(false) }

    // Scale animation
    val scale = remember { Animatable(1f) }
    LaunchedEffect(incrementTrigger) {
        if (incrementTrigger > 0) {
            scale.animateTo(1.15f, animationSpec = spring(dampingRatio = 0.3f))
            scale.animateTo(1f, animationSpec = spring(dampingRatio = 0.5f))
        }
    }

    // Lifecycle management — start/flush session
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.startSession()
                Lifecycle.Event.ON_STOP -> viewModel.flushSession()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = counter?.name ?: ""
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showRenameDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Counter")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Large count display
            Text(
                text = NumberFormat.getInstance().format(counter?.count ?: 0),
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                },
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Live timer display
            Text(
                text = TimeFormatter.formatDuration(liveElapsedMs),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Increment buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IncrementButton("+1") { viewModel.increment(1) }
                IncrementButton("+5") { viewModel.increment(5) }
                IncrementButton("+10") { viewModel.increment(10) }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Edit button
            Button(onClick = { viewModel.openEditDialog() }) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Text("  Edit Count & Time", modifier = Modifier.padding(start = 4.dp))
            }
        }
    }

    // Edit dialog
    if (isEditDialogOpen) {
        counter?.let { c ->
            EditCounterDialog(
                currentCount = c.count,
                currentTimeMs = c.totalTimeMs,
                onSave = { count, timeMs -> viewModel.saveEdit(count, timeMs) },
                onDismiss = { viewModel.closeEditDialog() }
            )
        }
    }

    // Rename dialog
    if (showRenameDialog) {
        var newName by remember { mutableStateOf(counter?.name ?: "") }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Counter") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Counter Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.renameCounter(newName)
                            showRenameDialog = false
                        }
                    },
                    enabled = newName.isNotBlank()
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun IncrementButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(width = 96.dp, height = 72.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge
        )
    }
}
