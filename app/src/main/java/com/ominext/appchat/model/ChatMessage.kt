package com.ominext.appchat.model

import java.util.*

class ChatMessage(
    val content: String,
    val image: String,
    val time: Long,
    val fromUser :String,
    val toRoom :String
) {
    constructor() : this("", "",System.currentTimeMillis() / 1000,"","")
    override fun toString(): String {
        return "ChatMessage(content='$content', type='$image', time=$time, fromUser='$fromUser', toRoom='$toRoom')"
    }
}