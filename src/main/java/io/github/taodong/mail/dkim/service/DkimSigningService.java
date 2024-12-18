package io.github.taodong.mail.dkim.service;

import io.github.taodong.mail.dkim.model.Canonicalization;
import io.github.taodong.mail.dkim.model.DkimSignHeader;
import io.github.taodong.mail.dkim.model.DkimSignature;
import io.github.taodong.mail.dkim.model.DkimSigningException;
import io.github.taodong.mail.dkim.model.HeaderTags;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;
import java.util.List;

public class DkimSigningService {

    public String sign(@NotNull MimeMessage message, @NotNull RSAPrivateKey dkimPrivateKey,
                       @NotBlank String selector, @NotBlank String domain,
                       @NotBlank String identity, @NotEmpty List<DkimSignHeader> headers,
                       Canonicalization headerCanonicalization,
                       Canonicalization bodyCanonicalization) throws DkimSigningException {
        domain = normalizeString(domain);
        identity = normalizeString(identity);
        validateParameters(domain, identity);
        var signature = new DkimSignature();
        signature.addTagValue(HeaderTags.DOMAIN, domain);
        signature.addTagValue(HeaderTags.SELECTOR, selector);
        signature.addTagValue(HeaderTags.USERNAME, identity);
        signature.addTagValue(HeaderTags.CANONICALIZATION, generateCanonicalizationValue(headerCanonicalization, bodyCanonicalization));
        signature.addTagValue(HeaderTags.BODY_HASH, hashBody(message, bodyCanonicalization));
        signHeaders(signature, message, headers, headerCanonicalization, dkimPrivateKey);
        return signature.getValue();
    }

    private void signHeaders(DkimSignature signature, MimeMessage message, List<DkimSignHeader> headers,
                             Canonicalization canonicalization, RSAPrivateKey privateKey) throws DkimSigningException {
        if (canonicalization == null) {
            canonicalization = Canonicalization.SIMPLE;
        }

        var foundHeaders = new StringBuilder();
        var headerValues = new StringBuilder();

        for (var header : headers) {
            var headerName = header.name();
            try {
                var headerValuesArray = message.getHeader(headerName);
                if (headerValuesArray == null) {
                    if (header.required()) {
                        throw new DkimSigningException("Required header " + headerName + " is missing.");
                    }
                    continue;
                }

                for (var headerValue : headerValuesArray) {
                    foundHeaders.append(headerName).append(":");
                    headerValues.append(headerValue).append("\r\n");
                }
            } catch (MessagingException e) {
                throw new DkimSigningException("Failed to get header " + headerName, e);
            }
        }

    }

    private String hashBody(MimeMessage message, Canonicalization canonicalization) throws DkimSigningException {
        if (canonicalization == null) {
            canonicalization = Canonicalization.SIMPLE;
        }

        try (var input = message.getInputStream()){
            var body = MimeUtility.getBytes(input);
            var bodyString = new String(body, StandardCharsets.UTF_8);
            var canonicalBody = canonicalization.getBodyOperator().apply(bodyString);
            var hashed = MessageDigest.getInstance("SHA-256").digest(canonicalBody.getBytes(StandardCharsets.UTF_8));
            return base64Encode(hashed);
        } catch (IOException | MessagingException | NoSuchAlgorithmException e) {
            throw new DkimSigningException("Failed to hash message body.", e);
        }

    }

    private static String base64Encode(byte[] bytes) {
        String encoded = Base64.getEncoder().encodeToString(bytes);

        // remove unnecessary line feeds after 76 characters
        encoded = encoded.replace("\n", "");
        encoded = encoded.replace("\r", "");

        return encoded;
    }

    private String generateCanonicalizationValue(Canonicalization headerCanonicalization, Canonicalization bodyCanonicalization) {

        return (headerCanonicalization == null ? Canonicalization.SIMPLE.getType() : headerCanonicalization.getType()) + "/" +
                (bodyCanonicalization == null ? Canonicalization.SIMPLE.getType() : bodyCanonicalization.getType());
    }

    private void validateParameters(String domain, String identity) throws DkimSigningException {
        if (!StringUtils.endsWithAny(identity, "@" + domain, "." + domain)) {
            throw new DkimSigningException("The identity " + identity + " is not end with domain " + domain);
        }
    }

    private String normalizeString(String input) {
        return StringUtils.lowerCase(StringUtils.trim(input));
    }
}
