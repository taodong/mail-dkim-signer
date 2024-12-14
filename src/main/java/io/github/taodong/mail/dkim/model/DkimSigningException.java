package io.github.taodong.mail.dkim.model;

public class DkimSigningException extends Exception {
    public DkimSigningException(String message) {
        super(message);
    }

    public DkimSigningException(String message, Throwable cause) {
        super(message, cause);
    }
}
