package levi.lin.gemini.android.model

import java.util.UUID

data class MessageItem(
    val id: String = UUID.randomUUID().toString(),
    var text: String = "",
    val type: MessageType = MessageType.User,
    var isPending: Boolean = false
)