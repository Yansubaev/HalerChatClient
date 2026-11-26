package com.ians.halerchat.feature.rooms

import com.ians.halerchat.feature.rooms.resources.*
import org.jetbrains.compose.resources.DrawableResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ians.halerchat.core.model.Room


@Composable
internal fun RoomsContent(
    rooms: List<Room>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        items(
            items = rooms,
            key = { it.id }
        ) { room ->
            RoomListItem(room)
        }
    }

}

@Composable
internal fun RoomsError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RoomsMessage(
        title = stringResource(Res.string.rooms_error_title),
        body = stringResource(Res.string.rooms_error_body),
        iconRes = Res.drawable.ic_cloud_off,
        iconColor = MaterialTheme.colorScheme.onErrorContainer,
        iconBackgroundColor = MaterialTheme.colorScheme.errorContainer,
        modifier = modifier,
    ) {
        Button(onClick = onRetry) {
            Icon(
                painter = painterResource(Res.drawable.ic_refresh),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = stringResource(Res.string.rooms_retry))
        }
    }
}

@Composable
internal fun RoomsEmpty(
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RoomsMessage(
        title = stringResource(Res.string.rooms_empty_title),
        body = stringResource(Res.string.rooms_empty_body),
        iconRes = Res.drawable.ic_forum,
        iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
        iconBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier,
    ) {
        Button(onClick = onCreateRoom) {
            Icon(
                painter = painterResource(Res.drawable.ic_add),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = stringResource(Res.string.rooms_create))
        }
        FilledTonalButton(
            onClick = onJoinRoom
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_link),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = stringResource(Res.string.rooms_join_with_link))
        }
    }

}

@Composable
internal fun RoomsLoading(modifier: Modifier = Modifier) {
    Column(modifier) {
        LinearProgressIndicator(Modifier.fillMaxWidth())
        repeat(6) { RoomSkeletonItem() }
    }
}

// Тот же ListItem, что у RoomListItem: высота и отступы строк совпадают сами.
// Полоски 16/12dp внутри строки текста 24/20sp (bodyLarge/bodyMedium) → по 4dp сверху и снизу.
@Composable
private fun RoomSkeletonItem() {
    ListItem(
        leadingContent = { SkeletonBlock(Modifier.size(56.dp), CircleShape) },
        headlineContent = {
            SkeletonBlock(Modifier.padding(vertical = 4.dp).fillMaxWidth(0.55f).height(16.dp))
        },
        supportingContent = {
            SkeletonBlock(Modifier.padding(vertical = 4.dp).fillMaxWidth(0.85f).height(12.dp))
        },
        trailingContent = { SkeletonBlock(Modifier.size(width = 36.dp, height = 12.dp)) },
    )
}

@Composable
private fun SkeletonBlock(modifier: Modifier, shape: Shape = MaterialTheme.shapes.small) {
    Box(modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh, shape))
}

@Composable
private fun RoomListItem(room: Room) {
    ListItem(
        leadingContent = {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    )
                    .size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = room.name.getInitials(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        },
        headlineContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f, fill = false),
                    text = room.name,
                    style = MaterialTheme.typography.bodyLarge,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )

                if (room.visibility == "private") {
                    Spacer(Modifier.size(4.dp))
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(Res.drawable.ic_lock),
                        contentDescription = stringResource(Res.string.rooms_cd_private),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        supportingContent = {
            Text(
                text = room.description ?: pluralStringResource(
                    Res.plurals.rooms_member_count,
                    room.memberCount,
                    room.memberCount
                ),
            )
        }
    )
}

@Composable
private fun RoomsMessage(
    title: String,
    body: String,
    iconRes: DrawableResource,
    iconColor: Color,
    iconBackgroundColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable FlowRowScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(color = iconBackgroundColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = iconColor
            )
        }

        Spacer(Modifier.size(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.size(8.dp))

        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp)
        )

        Spacer(Modifier.size(16.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

private fun String.getInitials(): String {
    val words = this.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }

    return when {
        words.isEmpty() -> ""
        words.size == 1 -> words[0].take(2).uppercase()
        else -> words.take(2).joinToString("") { it.first().toString() }.uppercase()
    }
}
