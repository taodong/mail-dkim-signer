package io.github.taodong.mail.dkim.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DkimSignature {
    private static final String TAG_DELIMITER = "; ";
    private static final String TAG_VALUE_DELIMITER = "=";

    private final Map<HeaderTags, String> headerTags = new HashMap<>();

    public DkimSignature() {
        this.headerTags.put(HeaderTags.VERSION, "1");
        this.headerTags.put(HeaderTags.ALGORITHM, "rsa-sha256");
    }

    public void addTagValue(HeaderTags tag, String value) {
        headerTags.put(tag, value);
    }

    public String getValue() throws DkimSigningException {
        validateContent4Signing();
        return Arrays.stream(HeaderTags.values())
                .map(tag -> {
                    var value = headerTags.get(tag);
                    return tag.getTagName() + TAG_VALUE_DELIMITER + value;
                })
                .reduce((s1, s2) -> s1 + TAG_DELIMITER + s2)
                .orElseThrow(() -> new DkimSigningException("Failed to generate DKIM signature. Please report a bug."));
    }

    private void validateContent4Signing() throws DkimSigningException {
        var missing = Arrays.stream(HeaderTags.values())
                .filter(tag -> !headerTags.containsKey(tag))
                .map(HeaderTags::getTagName)
                .reduce((s1, s2) -> s1 + ", " + s2)
                .orElse(null);

        if (missing != null) {
            throw new DkimSigningException("Missing value for tag(s): " + missing);
        }

    }
}
