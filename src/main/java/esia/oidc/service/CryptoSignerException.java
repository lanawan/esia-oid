package esia.oidc.service;

public class CryptoSignerException extends RuntimeException {
    public CryptoSignerException(String message, Throwable cause) {
        super(message, cause);
    }
    public CryptoSignerException(String message) {
        super(message);
    }
}
