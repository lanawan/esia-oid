package esia.oidc.service;

public class EsiaAuthException extends RuntimeException {
    public EsiaAuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public EsiaAuthException(String message) {
        super(message);
    }
}
