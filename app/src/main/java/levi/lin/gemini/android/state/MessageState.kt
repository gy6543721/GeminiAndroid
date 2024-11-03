package levi.lin.gemini.android.state

import androidx.compose.runtime.toMutableStateList
import levi.lin.gemini.android.model.MessageItem

class MessageState(
    messageList: List<MessageItem> = emptyList()
) {
    private val _messageItems: MutableList<MessageItem> = messageList.toMutableStateList()
    val messageItems: List<MessageItem> = _messageItems

    fun addMessage(message: MessageItem) {
        _messageItems.add(element = message)
    }

    fun replaceLastMessage() {
        val lastMessage = _messageItems.lastOrNull()
        lastMessage?.let {
            val newMessage = lastMessage.copy(isPending = false)
            _messageItems.remove(it)
            _messageItems.add(newMessage)
        }
    }

    fun updateMessagePendingStatus(messageId: String, isPending: Boolean) {
        val index = _messageItems.indexOfFirst { messageItem ->
            messageItem.id == messageId
        }
        if (index != -1) {
            val message = _messageItems[index].copy(isPending = isPending)
            _messageItems[index] = message
        }
    }
}