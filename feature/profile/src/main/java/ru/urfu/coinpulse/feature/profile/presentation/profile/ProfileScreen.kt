package ru.urfu.coinpulse.feature.profile.presentation.profile

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File
import ru.urfu.coinpulse.feature.profile.domain.model.DownloadedDocument
import ru.urfu.coinpulse.feature.profile.domain.model.UserProfile
import ru.urfu.coinpulse.core.designsystem.components.EmptyState
import ru.urfu.coinpulse.core.designsystem.theme.Divider
import ru.urfu.coinpulse.core.designsystem.theme.Primary
import ru.urfu.coinpulse.core.designsystem.theme.Surface
import ru.urfu.coinpulse.core.designsystem.theme.TextPrimary
import ru.urfu.coinpulse.core.designsystem.theme.TextSecondary
import ru.urfu.coinpulse.core.designsystem.theme.TextTertiary

@Composable
fun ProfileRoute(
    onEdit: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.OpenDocument -> openDocument(context, event.document)
            }
        }
    }

    ProfileScreen(
        state = state,
        onEdit = onEdit,
        onResumeClick = viewModel::onResumeClick,
    )
}

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onEdit: () -> Unit,
    onResumeClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
                Text(
                    text = "Stored locally on this device",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextTertiary,
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit profile",
                    tint = Primary,
                )
            }
        }

        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Primary)
            }

            state.profile.isEmpty -> EmptyState(
                title = "Your profile is empty",
                message = "Add your name, a position, an avatar and a resume link. Everything is stored on this device.",
                icon = Icons.Filled.Person,
                actionLabel = "Create profile",
                onAction = onEdit,
                modifier = Modifier.weight(1f),
            )

            else -> ProfileContent(
                profile = state.profile,
                isDownloadingResume = state.isDownloadingResume,
                error = state.error,
                onResumeClick = onResumeClick,
            )
        }
    }
}

@Composable
private fun ProfileContent(
    profile: UserProfile,
    isDownloadingResume: Boolean,
    error: String?,
    onResumeClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = Surface),
            border = BorderStroke(1.dp, Divider.copy(alpha = 0.55f)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProfileAvatar(
                    avatarUri = profile.avatarUri,
                    fullName = profile.fullName,
                    size = 96.dp,
                )
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.fullName.ifBlank { "Your name" },
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    if (profile.position.isNotBlank()) {
                        Text(
                            text = profile.position,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                    }
                    Text(
                        text = "Local profile",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextTertiary,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = Surface),
            border = BorderStroke(1.dp, Divider.copy(alpha = 0.55f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
            ) {
                Text(
                    text = "Resume",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(6.dp))
                if (profile.resumeUrl.isBlank()) {
                    Text(
                        text = "Add a resume or portfolio URL in Edit profile.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                } else {
                    Text(
                        text = profile.resumeUrl,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onResumeClick,
                        enabled = !isDownloadingResume,
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        if (isDownloadingResume) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Downloading…")
                        } else {
                            Text("Resume", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "The file is downloaded to app storage and opened in the system viewer.",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextTertiary,
                    )
                }

                if (error != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = Surface),
            border = BorderStroke(1.dp, Divider.copy(alpha = 0.55f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
            ) {
                Text(
                    text = "Profile details",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(10.dp))
                ProfileInfoRow("Full name", profile.fullName.ifBlank { "Not set" })
                ProfileInfoRow("Position", profile.position.ifBlank { "Not set" })
                ProfileInfoRow("Avatar", if (profile.avatarUri.isBlank()) "Not set" else "Local photo")
                ProfileInfoRow("Resume", if (profile.resumeUrl.isBlank()) "Not set" else "Saved URL")
                ProfileInfoRow("Class reminder", profile.classTime.ifBlank { "Not set" })
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun openDocument(context: Context, document: DownloadedDocument) {
    runCatching {
        val file = File(document.filePath)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, document.mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "Open ${document.displayName}").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }.onFailure {
        Toast.makeText(
            context,
            "No app can open this document. The file is saved locally.",
            Toast.LENGTH_LONG,
        ).show()
    }
}
