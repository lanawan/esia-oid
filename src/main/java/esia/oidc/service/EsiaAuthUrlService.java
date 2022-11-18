package esia.oidc.service;

public interface EsiaAuthUrlService {
    String generateAuthCodeUrlV1();
    String generateAuthCodeUrlV2();
}
