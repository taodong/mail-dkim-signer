package io.github.taodong.mail.dkim.support;

import io.github.taodong.mail.dkim.model.DkimSignHeader;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static io.github.taodong.mail.dkim.model.StandardMessageHeader.CC;
import static io.github.taodong.mail.dkim.model.StandardMessageHeader.CONTENT_TYPE;
import static io.github.taodong.mail.dkim.model.StandardMessageHeader.DATE;
import static io.github.taodong.mail.dkim.model.StandardMessageHeader.FROM;
import static io.github.taodong.mail.dkim.model.StandardMessageHeader.LIST_UNSUBSCRIBE;
import static io.github.taodong.mail.dkim.model.StandardMessageHeader.LIST_UNSUBSCRIBE_POST;
import static io.github.taodong.mail.dkim.model.StandardMessageHeader.MESSAGE_ID;
import static io.github.taodong.mail.dkim.model.StandardMessageHeader.MIME_VERSION;
import static io.github.taodong.mail.dkim.model.StandardMessageHeader.REPLY_TO;
import static io.github.taodong.mail.dkim.model.StandardMessageHeader.SUBJECT;
import static io.github.taodong.mail.dkim.model.StandardMessageHeader.TO;

public class DkimMimeMessageHelper {
    static final List<DkimSignHeader> DEFAULT_SIGN_HEADERS = List.of(
            new DkimSignHeader(FROM.getKey(), true),
            new DkimSignHeader(TO.getKey(), false),
            new DkimSignHeader(SUBJECT.getKey(), false),
            new DkimSignHeader(DATE.getKey(), false),
            new DkimSignHeader(CC.getKey(), false),
            new DkimSignHeader(CONTENT_TYPE.getKey(), false),
            new DkimSignHeader(REPLY_TO.getKey(), false),
            new DkimSignHeader(MESSAGE_ID.getKey(), false),
            new DkimSignHeader(LIST_UNSUBSCRIBE.getKey(), false),
            new DkimSignHeader(LIST_UNSUBSCRIBE_POST.getKey(), false),
            new DkimSignHeader(MIME_VERSION.getKey(), false)
    );


    public List<DkimSignHeader> getDkimSignHeaders(List<DkimSignHeader> customHeaders) {
        List<DkimSignHeader> headers = new ArrayList<>(DEFAULT_SIGN_HEADERS);

        if (CollectionUtils.isNotEmpty(customHeaders)) {
            headers.addAll(
                    customHeaders.stream()
                            .filter(h -> !StringUtils.equals(h.name(), FROM.getKey()))
                            .toList());
            headers = reverseDistinct(headers);
        }

        return headers;
    }

    private List<DkimSignHeader> reverseDistinct(List<DkimSignHeader> fatList) {
        var elementMap = new HashMap<DkimSignHeader, Integer>();
        var distinctList = new ArrayList<DkimSignHeader>();
        var lastIndex = 0;

        for (var element : fatList) {
            if (elementMap.containsKey(element)) {
                var index = elementMap.get(element);
                distinctList.set(index, element);
            } else {
                elementMap.put(element, lastIndex);
                distinctList.add(element);
                lastIndex++;
            }
        }
        return distinctList;
    }
}
