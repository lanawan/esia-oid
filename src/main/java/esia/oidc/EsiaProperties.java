package esia.oidc;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "esia")
@ConstructorBinding
@Data
public class EsiaProperties {
    private final String clientId;
    private final String authCodeUrl;
    private final String redirectUri;
    private final String tokenUrl;
    private final String clientSecret;

}
