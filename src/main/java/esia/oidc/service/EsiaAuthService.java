package esia.oidc.service;

import esia.oidc.dto.AccessTokenDto;

import java.util.Optional;

public interface EsiaAuthService {
    String getAuthCode();

    AccessTokenDto getAccessToken(String code, String state);
}
