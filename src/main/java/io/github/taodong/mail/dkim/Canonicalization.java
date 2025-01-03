package io.github.taodong.mail.dkim;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

/**
 * Enum for DKIM canonicalization algorithms
 * Logic is copied from java-utils-mail-dkim project
 *
 * @see <a href="https://github.com/simple-java-mail/java-utils-mail-dkim">java-utils-mail-dkim</a>
 */
@Getter
public enum Canonicalization {
    SIMPLE("simple",
            body -> {
                    // if body is empty, CRLF is returned
                    if (StringUtils.isEmpty(body)) {
                        return "\r\n";
                    }

                    // if there is no trailing CRLF on the message body, CRLF is added
                    if (!body.endsWith("\r\n")) {
                        return body + "\r\n";
                    }

                    // while there are multiple trailing CRLF on the message body, one is removed
                    while (body.endsWith("\r\n\r\n")) {
                        body = body.substring(0, body.length() - 2);
                    }

                    return body;
                },
            (header, value) -> header + ": " + value
    ),
    RELAXED("relaxed",
            body ->  {

                // if there body is blank, an empty body is returned
                if (StringUtils.isEmpty(body)) {
                    return "";
                }

                // if there is no trailing CRLF on the message body, CRLF is added
                if (!body.endsWith("\r\n")) {
                    body += "\r\n";
                }

                // ignore all whitespace at the end of lines
                body = body.replaceAll("[ \\t]+\r\n", "\r\n");

                // reduce all sequences of whitespace within a line to a single SP character
                body = body.replaceAll("[ \\t]+", " ");

                // while there are multiple trailing CRLF on the message body, one is removed
                while (body.endsWith("\r\n\r\n")) {
                    body = body.substring(0, body.length() - 2);
                }

                // at last, ensure CRLF is empty
                if ("\r\n".equals(body)) {
                    body = "";
                }

                return body;

            },
            (header, value) -> header.trim().toLowerCase() + ":" + value.replaceAll("\\s+", " ").trim()
    );

    private final String type;
    private final UnaryOperator<String> bodyOperator;
    private final BinaryOperator<String> headerOperator;

    Canonicalization(String type, UnaryOperator<String> bodyOperator, BinaryOperator<String> headerOperator) {
        this.type = type;
        this.bodyOperator = bodyOperator;
        this.headerOperator = headerOperator;
    }

    /**
     * Get the canonicalization algorithm by type
     * @param type - the type of canonicalization algorithm
     * @return the canonicalization algorithm, simple if not matched
     */
    public static Canonicalization fromType(String type) {
        for (Canonicalization c : Canonicalization.values()) {
            if (StringUtils.equalsIgnoreCase(c.getType(), type)) {
                return c;
            }
        }
        return Canonicalization.SIMPLE;
    }
}
