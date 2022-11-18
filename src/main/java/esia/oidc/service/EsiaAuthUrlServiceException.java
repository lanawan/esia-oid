package esia.oidc.service;

public class EsiaAuthUrlServiceException extends RuntimeException {
    public EsiaAuthUrlServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    public EsiaAuthUrlServiceException(String message) {
        super(message);
    }
}
