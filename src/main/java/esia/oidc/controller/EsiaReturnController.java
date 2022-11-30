package esia.oidc.controller;

import esia.oidc.dto.AccessTokenDto;
import esia.oidc.dto.UserDto;
import esia.oidc.service.EsiaAuthException;
import esia.oidc.service.EsiaAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class EsiaReturnController {

    private final EsiaAuthService esiaAuthService;

    @GetMapping(path = "/esia_return") //
    public UserDto handleReturn(
            @RequestParam(name = "code", required = false) String authorizationCode
            , @RequestParam(name = "state", required = false) String state
            , @RequestParam(name = "error", required = false) String error
            , @RequestParam(name = "error_description", required = false) String errorDescription
    ) {
        if (!StringUtils.hasText(authorizationCode)) {
            throw new EsiaAuthException("Authorization code not received. Error: '" + error
                    + "'. Description: '" + errorDescription + "'.");
        }
        AccessTokenDto accessTokenDto = esiaAuthService.getAccessToken(authorizationCode, state);
        if (accessTokenDto != null && accessTokenDto.getAccessToken() != null) {
            return esiaAuthService.getUserInfo(accessTokenDto.getAccessToken());
        }
        // TODO: change it according to UI
        return null;
    }
}
