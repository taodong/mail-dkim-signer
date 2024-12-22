package io.github.taodong.mail.dkim;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

class DkimSigningServiceTest {

    private static final Date TEST_DATE = createTestDate();

    private final DkimSigningService dkimSigningService = new DkimSigningService();
    private final DkimMimeMessageHelper dkimMimeMessageHelper = new DkimMimeMessageHelper();
    private final RSAPrivateKey testKey = getTestKey();
    private final PublicKey testPublicKey = getTestPublicKey();

    @SuppressWarnings("unused")
    private static final List<Arguments> signTestCases = List.of(
        argumentSet("Empty message",
                createTestMessage("tao.dong@duotail.com", "test@gmail.com", "Empty Body", ""),
                "s1", "duotail.com", "tao.dong@duotail.com", null, null,
                "v=1; a=rsa-sha256; d=duotail.com; c=simple/simple; i=tao.dong@duotail.com; s=s1; h=From:To:Subject:Date; bh=frcCV1k9oG9oKj3dpUqdJg1PxRT2RSN/XKdLCPjaYaY=; b=")
    );


    @ParameterizedTest
    @FieldSource("signTestCases")
    void sign(MimeMessage message, String selector, String domain, String identity,
              Canonicalization headerCanonicalization, Canonicalization bodyCanonicalization,
              String expected) throws DkimSigningException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {

        var result = dkimSigningService.sign(message, testKey, selector, domain, identity,
                dkimMimeMessageHelper.getDkimSignHeaders(null),
                headerCanonicalization, bodyCanonicalization);

        System.out.println(result);

        assertTrue(result.startsWith(expected));
        assertTrue(validateSignature(message, result, headerCanonicalization == null ? Canonicalization.SIMPLE : headerCanonicalization));
    }

    private boolean validateSignature(MimeMessage message, String dkimToken, Canonicalization headerCanonicalization)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        LinkedHashMap<String, String> contentToSign = getSignedHeaders(message, dkimToken);
        contentToSign.put(DkimSignature.DKIM_SIGNATURE_HEADER, stripSignatureFromDkimToken(dkimToken));
        var signature = extractSignatureValue(dkimToken);
        var signatureBytes = Base64.getDecoder().decode(signature);
        var signedContent = contentToSign.entrySet().stream()
                .map(entry -> headerCanonicalization.getHeaderOperator().apply(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("\r\n"));

        var signatureVerifier = Signature.getInstance("SHA256withRSA");
        signatureVerifier.initVerify(testPublicKey);
        signatureVerifier.update(signedContent.getBytes(StandardCharsets.UTF_8));
        return signatureVerifier.verify(signatureBytes);
    }

    private String stripSignatureFromDkimToken(String dkimToken) {
        return StringUtils.substringBeforeLast(dkimToken, "b=") + "b=";
    }

    private LinkedHashMap<String, String> getSignedHeaders(MimeMessage message, String dkimToken) {
        var headers = Arrays.stream(dkimToken.split(";"))
                .map(String::trim)
                .filter(tag -> StringUtils.startsWith(tag, "h="))
                .findFirst()
                .map(tag -> StringUtils.substringAfter(tag, "h="))
                .orElseThrow();

        LinkedHashMap<String, String> signedHeaders = new LinkedHashMap<>();
        var headerNames = headers.split(":");
        for (var header : headerNames) {
            try {
                signedHeaders.put(header, message.getHeader(header, ","));
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }
        return signedHeaders;
    }

    private String extractSignatureValue(String dkimToken) {
        return Arrays.stream(dkimToken.split(";"))
                .map(String::trim)
                .filter(tag -> StringUtils.startsWith(tag, "b="))
                .findFirst()
                .map(tag -> StringUtils.substringAfter(tag, "b="))
                .orElseThrow();
    }

    private static MimeMessage createTestMessage(@Email String from, @Email String to,
                                          @NotNull String subject, String body) {

        var message = new MimeMessage(Session.getInstance(new Properties()));
        try {
            message.setFrom(from);
            message.setRecipients(MimeMessage.RecipientType.TO, to);
            message.setSubject(subject);
            message.setSentDate(TEST_DATE);
            message.setHeader("Content-Type", "text/plain; charset=UTF-8");

            message.setText(body, StandardCharsets.UTF_8.name());

            return message;
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }

    private static Date createTestDate() {
        var calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(2024, Calendar.DECEMBER, 10, 0, 0, 0);
        return calendar.getTime();
    }

    private RSAPrivateKey getTestKey() {
        var classLoader = getClass().getClassLoader();
        try (var input = classLoader.getResourceAsStream("keys/test_key.pem")) {
            return dkimMimeMessageHelper.getKPCS8KeyFromInputStream(input);
        } catch (IOException | DkimSigningException e) {
            throw new RuntimeException(e);
        }
    }

    private PublicKey getTestPublicKey() {
        var classLoader = getClass().getClassLoader();
        try (var input = classLoader.getResourceAsStream("keys/test_key.pub")) {
            assert input != null;
            try (final var reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.US_ASCII))) {
                var rawKey = reader.lines().filter(line -> !line.startsWith("-----"))
                        .reduce(String::concat).orElseThrow();
                byte[] publicKeyBytes = Base64.getDecoder().decode(rawKey);
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                return keyFactory.generatePublic(keySpec);
            }
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
}