package io.github.taodong.mail.dkim;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static io.github.taodong.mail.dkim.StandardMessageHeader.CC;
import static io.github.taodong.mail.dkim.StandardMessageHeader.FROM;
import static io.github.taodong.mail.dkim.StandardMessageHeader.LIST_UNSUBSCRIBE;
import static io.github.taodong.mail.dkim.StandardMessageHeader.LIST_UNSUBSCRIBE_POST;
import static io.github.taodong.mail.dkim.StandardMessageHeader.MIME_VERSION;
import static io.github.taodong.mail.dkim.StandardMessageHeader.TO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class DkimMimeMessageHelperTest {

    private final DkimMimeMessageHelper dkimMimeMessageHelper = new DkimMimeMessageHelper();

    @SuppressWarnings("unused")
    private static final List<Arguments> headerTestCases = List.of(
            arguments(null, (Supplier<List<DkimSignHeader>>)() -> DkimMimeMessageHelper.DEFAULT_SIGN_HEADERS),
            arguments(List.of(new DkimSignHeader("Custom-Header1"), new DkimSignHeader("Customer-Header2", true)),
                    (Supplier<List<DkimSignHeader>>)() -> {
                        var result = new ArrayList<>(DkimMimeMessageHelper.DEFAULT_SIGN_HEADERS);
                        result.addAll(List.of(new DkimSignHeader("Custom-Header1"), new DkimSignHeader("Customer-Header2", true)));
                        return result;
                    })
    );

    @ParameterizedTest
    @FieldSource("headerTestCases")
    void getDkimSignHeaders(List<DkimSignHeader> customerHeaders, Supplier<List<DkimSignHeader>> expectedSupplier) {
        assertTrue(CollectionUtils.isEqualCollection(expectedSupplier.get(), dkimMimeMessageHelper.getDkimSignHeaders(customerHeaders)));
    }

    @Test
    void getDkimSignHeaders_replaceDefault() {
        var ignoredHeader = new DkimSignHeader("From", false);
        var updatedHeader = new DkimSignHeader(TO.getKey(), true);
        var customHeaders = List.of(ignoredHeader, updatedHeader,
                new DkimSignHeader("Custom-Header1"),
                new DkimSignHeader("Customer-Header2", true));
        var result = dkimMimeMessageHelper.getDkimSignHeaders(customHeaders);
        assertEquals(13, result.size());
        assertEquals(ignoredHeader.name(), result.get(0).name());
        assertTrue(result.get(0).required());
        assertEquals(updatedHeader.name(), result.get(1).name());
        assertTrue(result.get(1).required());
        assertFalse(result.get(11).required());
        assertTrue(result.get(12).required());
    }

    @Test
    void getDkimSignHeaders_removeDefaultHeaders() {
        var ignoredHeader = new DkimSignHeader("From", false);
        var updatedHeader = new DkimSignHeader(TO.getKey(), true);
        var customHeaders = List.of(ignoredHeader, updatedHeader,
                new DkimSignHeader("Custom-Header1"),
                new DkimSignHeader("Customer-Header2", true));
        var ignoredHeaders = Set.of(FROM.getKey(), CC.getKey(), LIST_UNSUBSCRIBE.getKey(), LIST_UNSUBSCRIBE_POST.getKey(), MIME_VERSION.getKey());
        var result = dkimMimeMessageHelper.getDkimSignHeaders(customHeaders, ignoredHeaders);
        assertEquals(9, result.size());
        assertEquals(ignoredHeader.name(), result.get(0).name());
        assertTrue(result.get(0).required());
        assertEquals(updatedHeader.name(), result.get(1).name());
        assertTrue(result.get(1).required());
        assertFalse(result.get(7).required());
        assertTrue(result.get(8).required());
    }

    @Test
    void getKPCS8KeyFromInputStream() throws IOException, DkimSigningException {
        var classLoader = getClass().getClassLoader();
        try (var input = classLoader.getResourceAsStream("keys/test_key.pem")) {
            var key = dkimMimeMessageHelper.getKPCS8KeyFromInputStream(input);
            assertEquals("RSA", key.getAlgorithm());
        }
    }

    @Test
    void getKPCS8KeyFromInputStream_InvalidKey() throws IOException {
        var classLoader = getClass().getClassLoader();
        try (var input = classLoader.getResourceAsStream("keys/test_key.pub")) {
            assertThrows(DkimSigningException.class, () -> dkimMimeMessageHelper.getKPCS8KeyFromInputStream(input));
        }
    }
}