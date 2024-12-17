package io.github.taodong.mail.dkim.service;

import io.github.taodong.mail.dkim.support.DkimMimeMessageHelper;
import jakarta.mail.internet.MimeMessage;

import java.security.interfaces.RSAPrivateKey;

public class DkimSigningService {

    private final DkimMimeMessageHelper mimeMessageHelper = new DkimMimeMessageHelper();

    public String sign(MimeMessage message, RSAPrivateKey dkimPrivateKey) {
        return "TODO...";
    }
}
