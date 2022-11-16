package esia.oidc.service;

public interface CryptoSigner {
    byte[] sign(String textToSign);
}
