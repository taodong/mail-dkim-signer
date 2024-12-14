package io.github.taodong.mail.dkim.model;

import lombok.Getter;

@Getter
public enum HeaderTags {
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

    HeaderTags(String tagName) {
        this.tagName = tagName;
    }
}
