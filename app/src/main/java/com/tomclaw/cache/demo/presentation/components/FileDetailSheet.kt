package com.tomclaw.cache.demo.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.tomclaw.cache.demo.domain.model.CachedFile
import com.tomclaw.cache.demo.ui.theme.CacheCritical
import com.tomclaw.cache.demo.ui.theme.CacheHealthy
import com.tomclaw.cache.demo.ui.theme.CacheWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileDetailSheet(
    file: CachedFile,
    onDismiss: () -> Unit,
    onAccess: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "File Details",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            DetailRow(label = "Key", value = file.key)
            DetailRow(label = "Filename", value = file.file.name)
            DetailRow(label = "Size", value = formatSize(file.size))
            DetailRow(
                label = "Last Accessed",
                value = formatDate(file.lastAccessed)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Priority",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            val priorityProgress = when (file.priority) {
                CachedFile.Priority.HIGH -> 1f
                CachedFile.Priority.MEDIUM -> 0.5f
                CachedFile.Priority.LOW -> 0.15f
            }
            val priorityColor = when (file.priority) {
                CachedFile.Priority.HIGH -> CacheHealthy
                CachedFile.Priority.MEDIUM -> CacheWarning
                CachedFile.Priority.LOW -> CacheCritical
            }
            val priorityText = when (file.priority) {
                CachedFile.Priority.HIGH -> "High"
                CachedFile.Priority.MEDIUM -> "Medium"
                CachedFile.Priority.LOW -> "Low - Will be evicted first"
            }

            LinearProgressIndicator(
                progress = { priorityProgress },
                modifier = Modifier.fillMaxWidth(),
                color = priorityColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = priorityText,
                style = MaterialTheme.typography.bodySmall,
                color = priorityColor,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onAccess,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TouchApp,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Access")
                }

                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CacheCritical
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = if (label == "Key") FontFamily.Monospace else FontFamily.Default
        )
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        else -> String.format("%.1f MB", bytes / 1024.0 / 1024.0)
    }
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMMM d, yyyy 'at' HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
