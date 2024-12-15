# Under Development!!!

# mail-dkim-signer
[![CI](https://github.com/taodong/mail-dkim-signer/actions/workflows/ci.yml/badge.svg)](https://github.com/taodong/mail-dkim-signer/actions/workflows/ci.yml)
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
