package levi.lin.gemini.android.ui.view.component

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import levi.lin.gemini.android.model.MessageItem
import levi.lin.gemini.android.model.MessageType

@Composable
fun MessageList(
    messages: List<MessageItem>,
    listState: LazyListState,
    innerPadding: PaddingValues
) {
    Column(modifier = Modifier.padding(innerPadding)) {
        LazyColumn(
            state = listState
        ) {
            items(messages) { message ->
                MessageBubble(messageItem = message)
            }
        }
    }
}

@Composable
fun MessageBubble(
    messageItem: MessageItem
) {
    val isGeminiMessage = messageItem.type == MessageType.Gemini

    val backgroundColor = when (messageItem.type) {
        MessageType.User -> MaterialTheme.colorScheme.primaryContainer
        MessageType.Gemini -> MaterialTheme.colorScheme.secondaryContainer
        MessageType.Error -> MaterialTheme.colorScheme.errorContainer
    }

    val bubbleShape = if (isGeminiMessage) {
        RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
    }

    val horizontalAlignment = if (isGeminiMessage) {
        Alignment.Start
    } else {
        Alignment.End
    }

    Column(
        horizontalAlignment = horizontalAlignment,
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = messageItem.type.name,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row {
            if (messageItem.isPending) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(alignment = Alignment.CenterVertically)
                        .padding(all = 8.dp)
                )
            }
            BoxWithConstraints {
                Card(
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    shape = bubbleShape,
                    modifier = Modifier.widthIn(min = 0.dp, max = maxWidth * 0.9f)
                ) {
                    SelectionContainer {
                        Text(
                            text = messageItem.text,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}