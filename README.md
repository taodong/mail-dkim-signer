# Under Development!!!

# mail-dkim-signer
[![CI](https://github.com/taodong/mail-dkim-signer/actions/workflows/ci.yml/badge.svg)](https://github.com/taodong/mail-dkim-signer/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=taodong_mail-dkim-signer&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=taodong_mail-dkim-signer)
[![codecov](https://codecov.io/gh/taodong/mail-dkim-signer/graph/badge.svg?token=ME6HTXFS7A)](https://codecov.io/gh/taodong/mail-dkim-signer)

A simple Java email dkim signer

## Purpose
I created this tool because I failed to integrate [java-utils-mail-dkim](https://github.com/simple-java-mail/java-utils-mail-dkim) 
into my project. It seems there are some compatibility issues between angus-mail and jarkata-mail/spring-mail
which I'm too lazy to investigate.

In this project I want to achieve a single goal: generate a DKIM signature header value.

## Limitations
The following limitations are known:
- The signer is tested only for [jakarta.mail.internet.MimeMessage](https://jakartaee.github.io/mail-api/docs/api/jakarta.mail/jakarta/mail/internet/MimeMessage.html).
- Tags included in the signature are fixed as `v`, `a`, `b`, `bh`, `c`, `d`, `h`, `i`, `s`
- Private key has to be a `rsa-shha256` key

## Usage
`DkimSigningService` is the class to be used to generate the value of `DKIM-Signature` header. A sample usage is as follows:

```java
try {
    var message = new MimeMessage(session);
    var signingService = new DkimSigningService();
    var dkimSignature = signingService.sign(message, yourPrivateKey, yourSelector, yourDomain, yourIdentity,
        yourheaderList, headerCanonicalization, bodyCanonicalization);
    message.setHeader(DkimSignature.DKIM_SIGNATURE_HEADER, dkimSignature);
    Transport.send(message);
    
} catch (DkimSigningException e) {
    // handle exception
}
```

### DkimSigningService
`DkimSigningService` has only one method `sign` to generate a DKIM signature header value. 
```java
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
public String sign(@NotNull MimeMessage message, @NotNull RSAPrivateKey dkimPrivateKey,
                   @NotBlank String selector, @NotBlank String domain,
                   @NotBlank String identity, @NotEmpty List<DkimSignHeader> headers,
                   Canonicalization headerCanonicalization,
                   Canonicalization bodyCanonicalization) throws DkimSigningException {
    // implementation
}
```

### DkimMimeMessageHelper
You can use `DkimMimeMessageHelper` to prepare data needed for `DkimSigningService`. 

#### Header Management
`DkimMimeMessageHelper` has two overloaded methods `getDkimSignHeaders` to manage headers to sign.  
```java
/**
 * Get the headers to be signed. All the header name are case-sensitive.
 * The default headers used are: From:To:Subject:Date:Cc:Content-Type:Reply-To:Message-ID:List-Unsubscribe:List-Unsubscribe-Post:MIME-Version
 * @param customHeaders - any extra headers to be signed other than the default headers, if any header included having the same name as the default one, the default header will be replaced
 * @return the headers to be signed
 */
public List<DkimSignHeader> getDkimSignHeaders(List<DkimSignHeader> customHeaders) {
    // implementation
}

/**
 * Get the headers to be signed. All the header name are case-sensitive.
 * The default headers used are: From:To:Subject:Date:Cc:Content-Type:Reply-To:Message-ID:List-Unsubscribe:List-Unsubscribe-Post:MIME-Version
 * @param customHeaders - any extra headers to be signed other than the default headers, if any header included having the same name as the default one, the default header will be replaced
 * @param ignoredHeaders - any headers to be ignored
 * @return the headers to be signed
 */
public List<DkimSignHeader> getDkimSignHeaders(List<DkimSignHeader> customHeaders, Set<String> ignoredHeaders) {
    // implementation
}
```

#### Load Private Key
`DkimMimeMessageHelper` has a method `loadPrivateKey` to load private key from an input stream.
```java
/**
     * Get the private key from input stream
     * @param keyStream - the input stream of the private key
     * @return the RSA private key
     * @throws DkimSigningException when failed to read the private key
     */
    public RSAPrivateKey getKPCS8KeyFromInputStream(@NotNull InputStream keyStream) throws DkimSigningException {
        // implementation
    }
```
