package io.github.taodong.mail.dkim;

import lombok.Getter;

@Getter
public enum StandardMessageHeader {
    FROM("From"),
    TO("To"),
    SUBJECT("Subject"),
    CONTENT_TYPE("Content-Type"),
    CC("Cc"),
    DATE("Date"),
    REPLY_TO("Reply-To"),
    MESSAGE_ID("Message-ID"),
    LIST_UNSUBSCRIBE("List-Unsubscribe"),
    LIST_UNSUBSCRIBE_POST("List-Unsubscribe-Post"),
    MIME_VERSION("MIME-Version"),
    ;

    private final String key;

    StandardMessageHeader(String key) {
        this.key = key;
    }
}
