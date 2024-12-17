package io.github.taodong.mail.dkim.support;

import io.github.taodong.mail.dkim.model.DkimSignHeader;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static io.github.taodong.mail.dkim.model.StandardMessageHeader.TO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
}