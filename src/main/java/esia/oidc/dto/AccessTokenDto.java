package esia.oidc.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AccessTokenDto {
    private String accessToken;
    private String tokenType;
    private String expiresIn;
    private String scope;
    private String createdAt;
}
