package esia.oidc.service;

import esia.oidc.EsiaProperties;
import esia.oidc.dto.AccessTokenDto;
import esia.oidc.utils.ParameterStringBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.*;
import java.net.HttpURLConnection;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URL;
import java.util.Optional;
import java.util.UUID;

@Service
class EsiaAuthServiceImpl implements EsiaAuthService {
    private static final Logger logger = LoggerFactory.getLogger(EsiaAuthServiceImpl.class);
    private final EsiaProperties esiaProperties;
    private final RestTemplate restTemplate;

    EsiaAuthServiceImpl(RestTemplate restTemplate, EsiaProperties esiaProperties) {
        this.esiaProperties = esiaProperties;
        this.restTemplate = restTemplate;
    }

    @Override
    public String getAuthCode() {
        try {
            String clientId = esiaProperties.getClientId();
            String clientSecret = esiaProperties.getClientSecret();

            String redirectUrlEncoded = esiaProperties.getRedirectUri()
                    .replace(":", "%3A")
                    .replace("/", "%2F");

            UriComponentsBuilder accessTokenRequestBuilder = UriComponentsBuilder.fromHttpUrl(esiaProperties.getAuthCodeUrl())
                    .queryParam("client_id", clientId)
                    .queryParam("client_secret", clientSecret)
                    .queryParam("response_type", "code")
                    .queryParam("state", generateState());

            String url = accessTokenRequestBuilder.toUriString();
            url += "&redirect_uri=" + redirectUrlEncoded;

            logger.debug("generated url: {}", url);

            return url;
        } catch (Exception e) {
            throw new EsiaAuthException("Unable to generate access code url", e);
        }
    }

    private String generateState() {
        return UUID.randomUUID().toString();
    }

    @Override
    public AccessTokenDto getAccessToken(String code, String state) {
        JSONObject postBody = new JSONObject();
        try {
            postBody.put("grant_type", "authorization_code");
            postBody.put("code", code);
            postBody.put("redirect_uri", esiaProperties.getRedirectUri() + "?code=" + code + "&state=" + state);
        } catch (JSONException e) {
            throw new EsiaAuthException("Unable to generate access code body", e);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(esiaProperties.getClientId(), esiaProperties.getClientSecret());

            HttpEntity<String> request = new HttpEntity<>(postBody.toString(), headers);

            logger.debug("fetching esia access token, post body parameters: {}", postBody);

            AccessTokenDto accessTokenDto = restTemplate.postForObject(esiaProperties.getTokenUrl(), request, AccessTokenDto.class);

            logger.debug("accessTokenDto: {}", accessTokenDto);

            return accessTokenDto;
        } catch (HttpClientErrorException e) {
            throw new EsiaAuthException("Unable to get access token for authorization code '" + code + '\'' + e);
        }
    }

}

