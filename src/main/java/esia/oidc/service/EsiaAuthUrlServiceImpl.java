package esia.oidc.service;

import esia.oidc.EsiaProperties;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

@Service
class EsiaAuthUrlServiceImpl implements EsiaAuthUrlService {
    private static final Logger logger = LoggerFactory.getLogger(EsiaAuthUrlServiceImpl.class);

    private final CryptoSigner cryptoSigner;
    private final EsiaProperties esiaProperties;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss Z")
            .withZone(ZoneId.systemDefault());

    private final String accessType = "offline"; // "online";
    private final String scope = "fullname"; // "fullname+email"
    private final String responseType = "code";

    public EsiaAuthUrlServiceImpl(CryptoSigner cryptoSigner, EsiaProperties esiaProperties) {
        this.cryptoSigner = cryptoSigner;
        this.esiaProperties = esiaProperties;
    }

    @Override
    public String generateAuthCodeUrl() {
        try {
            String clientId = esiaProperties.getClientId();
            String state = UUID.randomUUID().toString();
            String timestamp = dateTimeFormatter.format(Instant.now());
            String redirectUri = esiaProperties.getReturnUrl();
            String clientCertificateHash = esiaProperties.getClientCertificateHash();

            String clientSecret = generateClientSecret(ClientSecretParameters.builder()
                    .clientId(clientId).scope(scope).timestamp(timestamp).state(state).redirectUrl(redirectUri)
                    .build());

            UriComponentsBuilder authCodeUriBuilder = UriComponentsBuilder.fromHttpUrl(esiaProperties.getAuthCodeUrl())
                    .queryParam("client_id", clientId)
                    .queryParam("scope", scope)
                    .queryParam("timestamp", urlEncode(timestamp))
                    .queryParam("state", state)
                    .queryParam("redirect_uri", urlEncode(redirectUri))
                    .queryParam("client_certificate_hash", clientCertificateHash)
                    .queryParam("response_type", responseType)
                    .queryParam("access_type",accessType)
                    .queryParam("client_secret", clientSecret);

            String url = authCodeUriBuilder.toUriString();

            logger.debug("generated url: {}", url);

            return url;
        } catch (Exception e) {
            throw new EsiaAuthUrlServiceException("Unable to generate access token url", e);
        }
    }
    private String urlEncode(String string) {
        try {
            return URLEncoder.encode(string, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new EsiaAuthUrlServiceException("Could not encode string '" + string + '\'', e);
        }
    }


    private String generateClientSecret(ClientSecretParameters p) {
        // v1
        String[] toJoin = {p.scope, p.timestamp, p.clientId, p.state};
        // v2
        //String[] toJoin = {p.clientId, p.scope, p.timestamp, p.state, p.redirectUrl};

        String clientSecretUnsigned = String.join("", toJoin);
        logger.debug("clientSecret unsigned: {}", clientSecretUnsigned);

        byte[] signedClientSecretBytes = cryptoSigner.sign(clientSecretUnsigned);
        return Base64.getUrlEncoder().encodeToString(signedClientSecretBytes);
    }



    @Builder
    static class ClientSecretParameters {
        private final String clientId;
        private final String scope;
        private final String timestamp;
        private final String state;
        private final String redirectUrl;
        private final String authorizationCode;
    }
}
