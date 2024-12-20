package io.github.taodong.mail.dkim;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

public class DkimSignature {
    private static final String TAG_DELIMITER = "; ";
    private static final String TAG_VALUE_DELIMITER = "=";

    public static final String DKIM_SIGNATURE_HEADER = "DKIM-Signature";

    private final EnumMap<HeaderTag, String> headerTags = new EnumMap<>(HeaderTag.class);

    public DkimSignature() {
        this.headerTags.put(HeaderTag.VERSION, "1");
        this.headerTags.put(HeaderTag.ALGORITHM, "rsa-sha256");
    }

    public void addTagValue(HeaderTag tag, String value) {
        headerTags.put(tag, value);
    }

    public String getTagValue(HeaderTag tag) {
        return headerTags.get(tag);
    }

    public String getValue() throws DkimSigningException {
        validateContent4Signing();
        return formStringValue(getTagsWithExclusion(null));
    }

    String getBeforeHashValue() throws DkimSigningException {
        var tags = getTagsWithExclusion(Set.of(HeaderTag.SIGNATURE));
        return formStringValue(tags);
    }

    private List<HeaderTag> getTagsWithExclusion(Set<HeaderTag> excludedTags) {
        if (CollectionUtils.isEmpty(excludedTags)) {
            return Arrays.asList(HeaderTag.values());
        }

        return Arrays.stream(HeaderTag.values())
                .filter(tag -> !excludedTags.contains(tag))
                .toList();
    }

    private String formStringValue(List<HeaderTag> tags) throws DkimSigningException {
        return tags.stream()
                .map(tag -> {
                    var value = headerTags.get(tag);
                    return tag.getTagName() + TAG_VALUE_DELIMITER + value;
                })
                .reduce((s1, s2) -> s1 + TAG_DELIMITER + s2)
                .orElseThrow(() -> new DkimSigningException("Failed to generate DKIM signature. Please report a bug."));
    }

    private void validateContent4Signing() throws DkimSigningException {
        var missing = Arrays.stream(HeaderTag.values())
                .filter(tag -> !headerTags.containsKey(tag))
                .map(HeaderTag::getTagName)
                .reduce((s1, s2) -> s1 + ", " + s2)
                .orElse(null);

        if (missing != null) {
            throw new DkimSigningException("Missing value for tag(s): " + missing);
        }
    }
}
