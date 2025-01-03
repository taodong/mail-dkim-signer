package io.github.taodong.mail.dkim;

import jakarta.validation.constraints.NotNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.taodong.mail.dkim.StandardMessageHeader.CC;
import static io.github.taodong.mail.dkim.StandardMessageHeader.CONTENT_TYPE;
import static io.github.taodong.mail.dkim.StandardMessageHeader.DATE;
import static io.github.taodong.mail.dkim.StandardMessageHeader.FROM;
import static io.github.taodong.mail.dkim.StandardMessageHeader.LIST_UNSUBSCRIBE;
import static io.github.taodong.mail.dkim.StandardMessageHeader.LIST_UNSUBSCRIBE_POST;
import static io.github.taodong.mail.dkim.StandardMessageHeader.MESSAGE_ID;
import static io.github.taodong.mail.dkim.StandardMessageHeader.MIME_VERSION;
import static io.github.taodong.mail.dkim.StandardMessageHeader.REPLY_TO;
import static io.github.taodong.mail.dkim.StandardMessageHeader.SUBJECT;
import static io.github.taodong.mail.dkim.StandardMessageHeader.TO;

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


    /**
     * Get the headers to be signed. All the header name are case-sensitive.
     * The default headers used are: From:To:Subject:Date:Cc:Content-Type:Reply-To:Message-ID:List-Unsubscribe:List-Unsubscribe-Post:MIME-Version
     * @param customHeaders - any extra headers to be signed other than the default headers, if any header included having the same name as the default one, the default header will be replaced
     * @return the headers to be signed
     */
    public List<DkimSignHeader> getDkimSignHeaders(List<DkimSignHeader> customHeaders) {

        return getDkimSignHeaders(customHeaders, null);
    }

    /**
     * Get the headers to be signed. All the header name are case-sensitive.
     * The default headers used are: From:To:Subject:Date:Cc:Content-Type:Reply-To:Message-ID:List-Unsubscribe:List-Unsubscribe-Post:MIME-Version
     * @param customHeaders - any extra headers to be signed other than the default headers, if any header included having the same name as the default one, the default header will be replaced
     * @param ignoredHeaders - any headers to be ignored
     * @return the headers to be signed
     */
    public List<DkimSignHeader> getDkimSignHeaders(List<DkimSignHeader> customHeaders, Set<String> ignoredHeaders) {
        var headers = combineCustomerHeaders(customHeaders);
        removeIgnoredHeaders(headers, ignoredHeaders);

        return headers;
    }

    /**
     * Get the private key from input stream
     * @param keyStream - the input stream of the private key
     * @return the RSA private key
     * @throws DkimSigningException when failed to read the private key
     */
    public RSAPrivateKey getKPCS8KeyFromInputStream(@NotNull InputStream keyStream) throws DkimSigningException {
        try (final var reader = new BufferedReader(new InputStreamReader(keyStream, StandardCharsets.US_ASCII))) {
            var rawKey = reader.lines().filter(line -> !line.startsWith("-----")).reduce(String::concat).orElseThrow();
            var rsaKeyFactory = KeyFactory.getInstance("RSA");
            var privateKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(rawKey));
            return (RSAPrivateKey) rsaKeyFactory.generatePrivate(privateKeySpec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new DkimSigningException("Failed to read private key from input stream", e);
        }
    }

    private List<DkimSignHeader> combineCustomerHeaders(List<DkimSignHeader> customHeaders) {
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

    private void removeIgnoredHeaders(List<DkimSignHeader> headers, Set<String> ignoredHeaders) {
        if (CollectionUtils.isNotEmpty(ignoredHeaders)) {
            var mutableIgnoredHeaders = new HashSet<>(ignoredHeaders);
            mutableIgnoredHeaders.removeIf(h -> StringUtils.equals(h, FROM.getKey()));
            headers.removeIf(h -> mutableIgnoredHeaders.contains(h.name()));
        }
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
