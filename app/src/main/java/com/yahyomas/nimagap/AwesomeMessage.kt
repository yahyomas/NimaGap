package com.yahyomas.nimagap

class AwesomeMessage {
    var text: String? = null
    var name: String? = null
    var sender: String? = null
    var recipient: String? = null
    var imageUrl: String? = null
    var isMine = false

    constructor() {}
    constructor(
        text: String?, name: String?, sender: String?,
        recipient: String?, imageUrl: String?, isMine: Boolean
    ) {
        this.text = text
        this.name = name
        this.sender = sender
        this.recipient = recipient
        this.imageUrl = imageUrl
        this.isMine = isMine
    }

}
