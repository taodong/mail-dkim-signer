package io.github.taodong.mail.dkim;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class CanonicalizationTest {

    @SuppressWarnings("unused")
    private static final List<Arguments> bodyTestCases = List.of(
            argumentSet("null body apply simple", Canonicalization.SIMPLE, null, "\r\n"),
            argumentSet("null body apply relaxed", Canonicalization.RELAXED, null, ""),
            argumentSet("empty body apply simple", Canonicalization.SIMPLE, "", "\r\n"),
            argumentSet("empty body apply relaxed", Canonicalization.RELAXED, "", ""),
            argumentSet("white space only body apply simple", Canonicalization.SIMPLE, " \t", " \t\r\n"),
            argumentSet("white space only body apply relaxed", Canonicalization.RELAXED, " \t", ""),
            argumentSet("body with white space apply simple", Canonicalization.SIMPLE, " \tXY \t", " \tXY \t\r\n"),
            argumentSet("body with white space apply relaxed", Canonicalization.RELAXED, " \tXY \t", " XY\r\n"),
            argumentSet("control characters in white spaces apply simple", Canonicalization.SIMPLE, " \t\f\u000b \t", " \t\f\u000b \t\r\n"),
            argumentSet("control characters in white spaces apply relaxed", Canonicalization.RELAXED, " \t\f\u000b \t", " \f\u000b\r\n"),
            argumentSet("space and CRLF apply simple", Canonicalization.SIMPLE, " \t\r\n \t", " \t\r\n \t\r\n"),
            argumentSet("space and CRLF apply relaxed", Canonicalization.RELAXED, " \t\r\n \t", ""),
            argumentSet("RLF apply simple", Canonicalization.SIMPLE, " C \r\nD \t E\r\n\r\n\r\n", " C \r\nD \t E\r\n"),
            argumentSet("RLF apply relaxed", Canonicalization.RELAXED, " C \r\nD \t E\r\n\r\n\r\n", " C\r\nD E\r\n")
    );

    @ParameterizedTest
    @FieldSource("bodyTestCases")
    void testBodyOperator(Canonicalization canonicalization, String input, String expected) {
        assertEquals(expected, canonicalization.getBodyOperator().apply(input));
    }

    @SuppressWarnings("unused")
    private static final List<Arguments> typeTestCases = List.of(
       arguments(Canonicalization.SIMPLE, "simple"),
       arguments(Canonicalization.RELAXED, "relaxed")
    );

    @ParameterizedTest
    @FieldSource("typeTestCases")
    void testName(Canonicalization canonicalization, String expected) {
        assertEquals(expected, canonicalization.getType());
    }

    @SuppressWarnings("unused")
    private static final List<Arguments> headerTestCases = List.of(
            arguments(Canonicalization.SIMPLE, "Subject", "test", "Subject: test"),
            arguments(Canonicalization.SIMPLE, "Subject", "test subject", "Subject: test subject"),
            arguments(Canonicalization.SIMPLE, "Subject", "test  subject", "Subject: test  subject"),
            arguments(Canonicalization.RELAXED, "Subject", "test", "subject:test"),
            arguments(Canonicalization.RELAXED, "Subject", "test Subject", "subject:test Subject"),
            arguments(Canonicalization.RELAXED, "Subject", "test  Subject", "subject:test Subject")
    );

    @ParameterizedTest
    @FieldSource("headerTestCases")
    void testHeaderOperator(Canonicalization canonicalization, String header, String value, String expected) {
        assertEquals(expected, canonicalization.getHeaderOperator().apply(header, value));
    }

    @SuppressWarnings("unused")
    private static final List<Arguments> fromTypeTestCases = List.of(
            arguments("simple", Canonicalization.SIMPLE),
            arguments("relaxed", Canonicalization.RELAXED),
            arguments("SIMPle", Canonicalization.SIMPLE),
            arguments("RELAXED", Canonicalization.RELAXED),
            arguments(null, Canonicalization.SIMPLE),
            arguments("unknown", Canonicalization.SIMPLE)
    );

    @ParameterizedTest
    @FieldSource("fromTypeTestCases")
    void testFromType(String type, Canonicalization expected) {
        assertEquals(expected, Canonicalization.fromType(type));
    }

}