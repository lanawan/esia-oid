package esia.oidc.service;

import esia.oidc.dto.AccessTokenDto;
import esia.oidc.dto.UserDto;

import java.util.Optional;

public interface EsiaAuthService {
    String getAuthCode();

    AccessTokenDto getAccessToken(String code, String state);

    UserDto getUserInfo(String token);
}
