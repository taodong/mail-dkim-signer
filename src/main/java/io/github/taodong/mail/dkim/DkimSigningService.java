package io.github.taodong.mail.dkim;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class DkimSigningService {

    /**
     * Generate unfolded DKIM signature header value. This value should be the last header value introduced into message before sending.
     * @param message - message to sign
     * @param dkimPrivateKey - private key to sign the message
     * @param selector - selector to sign
     * @param domain - domain to sign
     * @param identity - identity to sign
     * @param headers - headers to sign, you can use {@link DkimMimeMessageHelper#getDkimSignHeaders(List)} to manage the headers
     * @param headerCanonicalization - header canonicalization method, when null, use {@link Canonicalization#SIMPLE}
     * @param bodyCanonicalization - body canonicalization method, when null, use {@link Canonicalization#SIMPLE}
     * @return the DKIM signature header value unfolded
     * @throws DkimSigningException when failed to sign the message
     */
    @SuppressWarnings("java:S107") // Suppress "Methods should not have too many parameters" for this method
    public String sign(@NotNull MimeMessage message, @NotNull RSAPrivateKey dkimPrivateKey,
                       @NotBlank String selector, @NotBlank String domain,
                       @NotBlank String identity, @NotEmpty List<DkimSignHeader> headers,
                       Canonicalization headerCanonicalization,
                       Canonicalization bodyCanonicalization) throws DkimSigningException {
        domain = normalizeString(domain);
        identity = normalizeString(identity);
        validateParameters(domain, identity);
        var signature = new DkimSignature();
        signature.addTagValue(HeaderTag.DOMAIN, domain);
        signature.addTagValue(HeaderTag.SELECTOR, selector);
        signature.addTagValue(HeaderTag.USERNAME, identity); // `i` tag should be a `dkim-quoted-printable` string, my use cases have no special characters, leave it as it is
        signature.addTagValue(HeaderTag.CANONICALIZATION, generateCanonicalizationValue(headerCanonicalization, bodyCanonicalization));
        signature.addTagValue(HeaderTag.BODY_HASH, hashBody(message, bodyCanonicalization));
        signHeaders(signature, message, headers, headerCanonicalization, dkimPrivateKey);
        return signature.getValue();
    }

    private void signHeaders(DkimSignature signature, MimeMessage message, List<DkimSignHeader> headers,
                             Canonicalization canonicalization, RSAPrivateKey privateKey) throws DkimSigningException {
        if (canonicalization == null) {
            canonicalization = Canonicalization.SIMPLE;
        }

        List<String> headerNames = new ArrayList<>();
        List<String> canonicalHeaders = new ArrayList<>();

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
                    headerNames.add(headerName);
                    canonicalHeaders.add(canonicalization.getHeaderOperator().apply(headerName, headerValue));
                }
            } catch (MessagingException e) {
                throw new DkimSigningException("Failed to get header " + headerName, e);
            }
        }

        var headerTagValue = String.join(":", headerNames);
        var headerToSign = String.join("\r\n", canonicalHeaders) + "\r\n";

        signature.addTagValue(HeaderTag.HEADERS, headerTagValue);

        var beforeHashValue = signature.getBeforeHashValue();
        var serializedSignature = beforeHashValue + "; " + HeaderTag.SIGNATURE.getTagName() + "=";
        var canonicalSignature = canonicalization.getHeaderOperator().apply(DkimSignature.DKIM_SIGNATURE_HEADER, serializedSignature);
        headerToSign += canonicalSignature;

        signature.addTagValue(HeaderTag.SIGNATURE, createSignatureValue(headerToSign, privateKey));
    }

    private String createSignatureValue(String headerToSign, RSAPrivateKey privateKey)
            throws DkimSigningException {
        try {
            Signature rsaSignature = Signature.getInstance("SHA256withRSA");
            rsaSignature.initSign(privateKey);
            rsaSignature.update(headerToSign.getBytes(StandardCharsets.UTF_8));
            return base64Encode(rsaSignature.sign());
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new DkimSigningException("Failed to create signature.", e);
        }
    }

    private String hashBody(MimeMessage message, Canonicalization canonicalization) throws DkimSigningException {
        if (canonicalization == null) {
            canonicalization = Canonicalization.SIMPLE;
        }

        try (var input = message.getInputStream()) {
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
