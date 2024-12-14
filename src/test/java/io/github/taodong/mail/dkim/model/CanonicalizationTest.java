package io.github.taodong.mail.dkim.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

class CanonicalizationTest {

    @SuppressWarnings("unused")
    private static final List<Arguments> bodyTestCases = List.of(
            argumentSet("empty body", Canonicalization.SIMPLE, null, "\r\n")
    );

    @ParameterizedTest
    @FieldSource("bodyTestCases")
    void testBodyOperator(Canonicalization canonicalization, String input, String expected) {
        assertEquals(expected, canonicalization.getBodyOperator().apply(input));
    }

}