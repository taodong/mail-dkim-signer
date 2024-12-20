package io.github.taodong.mail.dkim;

import lombok.Getter;

@Getter
public enum HeaderTag {
    VERSION("v"),
    ALGORITHM("a"),
    DOMAIN("d"),
    CANONICALIZATION("c"),
    USERNAME("i"),
    SELECTOR("s"),
    HEADERS("h"),
    BODY_HASH("bh"),
    SIGNATURE("b")
    ;

    private final String tagName;

    HeaderTag(String tagName) {
        this.tagName = tagName;
    }
}
