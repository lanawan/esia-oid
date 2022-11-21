package esia.oidc.service;

public interface EsiaAuthService {
    String generateAuthCodeUrl();

    String getAccessToken(String code, String state);
}
