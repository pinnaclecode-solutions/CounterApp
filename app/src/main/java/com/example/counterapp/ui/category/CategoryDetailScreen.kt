package com.example.counterapp.ui.category

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.counterapp.data.local.Counter
import com.example.counterapp.navigation.Routes
import com.example.counterapp.ui.components.ConfirmDeleteDialog
import com.example.counterapp.ui.components.StatItem
import com.example.counterapp.ui.theme.ActiveGreen
import com.example.counterapp.util.TimeFormatter
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    navController: NavController,
    viewModel: CategoryDetailViewModel = hiltViewModel()
) {
    val category by viewModel.category.collectAsState()
    val counters by viewModel.counters.collectAsState()
    val stats by viewModel.stats.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var counterToDelete by remember { mutableStateOf<Counter?>(null) }
    var showRenameCategory by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = category?.name ?: "",
                        modifier = Modifier.clickable { showRenameCategory = true }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Counter")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Stats Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Statistics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(
                                "Total Count",
                                NumberFormat.getInstance().format(stats.totalCount)
                            )
                            StatItem(
                                "Total Time",
                                TimeFormatter.formatDuration(stats.totalTimeMs)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val avgCount = if (stats.counterCount > 0)
                                String.format("%.1f", stats.totalCount.toDouble() / stats.counterCount)
                            else "0.0"
                            StatItem("Avg Count", avgCount)
                            StatItem(
                                "Most Active",
                                stats.mostActiveCounterName ?: "—"
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Counter Cards
            items(counters, key = { it.id }) { counter ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart) {
                            counterToDelete = counter
                            false
                        } else false
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                "Delete",
                                modifier = Modifier.padding(end = 16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    },
                    enableDismissFromStartToEnd = false
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(
                                    Routes.CounterDetail.createRoute(counter.id)
                                )
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 4.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (counter.isActive) {
                                Canvas(modifier = Modifier.size(10.dp)) {
                                    drawCircle(color = ActiveGreen)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = counter.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Count: ${NumberFormat.getInstance().format(counter.count)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Text(
                                text = TimeFormatter.formatDuration(counter.totalTimeMs),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            IconButton(onClick = { counterToDelete = counter }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Counter",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Counter Dialog
    if (showAddDialog) {
        val defaultName = remember {
            java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
                .format(java.util.Date())
        }
        var name by remember { mutableStateOf(defaultName) }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Counter") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Counter Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.addCounter(name) { id ->
                                navController.navigate(
                                    com.example.counterapp.navigation.Routes.CounterDetail.createRoute(id)
                                )
                            }
                            showAddDialog = false
                        }
                    },
                    enabled = name.isNotBlank()
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Delete Confirmation Dialog
    counterToDelete?.let { counter ->
        ConfirmDeleteDialog(
            title = "Delete Counter",
            message = "Delete \"${counter.name}\"?",
            onConfirm = {
                viewModel.deleteCounter(counter)
                counterToDelete = null
            },
            onDismiss = { counterToDelete = null }
        )
    }

    // Rename Category Dialog
    if (showRenameCategory) {
        var newName by remember { mutableStateOf(category?.name ?: "") }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showRenameCategory = false },
            title = { Text("Rename Category") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Category Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.updateCategoryName(newName)
                            showRenameCategory = false
                        }
                    },
                    enabled = newName.isNotBlank()
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameCategory = false }) { Text("Cancel") }
            }
        )
    }
}
