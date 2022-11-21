package esia.oidc.service;

import esia.oidc.EsiaProperties;
import esia.oidc.utils.ParameterStringBuilder;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.*;
import java.net.HttpURLConnection;

import org.springframework.web.util.UriComponentsBuilder;

import java.net.URL;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class EsiaAuthServiceImpl implements EsiaAuthService {
    private static final Logger logger = LoggerFactory.getLogger(EsiaAuthServiceImpl.class);
    private final EsiaProperties esiaProperties;

    @Override
    public String generateAuthCodeUrl() {
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

    public static String readResponse(InputStream inputStream) {
        final int bufferSize = 8 * 1024;
        byte[] buffer = new byte[bufferSize];
        final StringBuilder builder = new StringBuilder();
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, bufferSize)) {
            while (bufferedInputStream.read(buffer) != -1) {
                builder.append(new String(buffer));
            }
        } catch (IOException ex) {
            throw new EsiaAuthException("Requesting for token : unable to read resonse", ex);
        }
        return builder.toString();
    }

    private void writeRequest(String jsonInputString, HttpURLConnection con) {
        try(OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        } catch (Exception e) {
            throw new EsiaAuthException("Requesting for token : unable to write inputStream", e);
        }
    }
    @Override
    public String getAccessToken(String code, String state) {
        JSONObject json = new JSONObject();
        try {
            URL obj = new URL(esiaProperties.getTokenUrl());
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setRequestMethod("POST");
            con.setDoOutput(true);

            String redirectUrlEncoded = esiaProperties.getRedirectUri()
                    .replace(":", "%3A")
                    .replace("/", "%2F");


            json.put("grant_type", "authorization_code");
            json.put("code",  code );
            json.put("redirect_uri", esiaProperties.getRedirectUri() + "?code=" + code + "&state=" + state);

            writeRequest(json.toString(), con);

            int status = con.getResponseCode();

            InputStream inputStream;

            if (status == HttpURLConnection.HTTP_OK) {
                inputStream = con.getInputStream();
            } else {
                inputStream = con.getErrorStream();
            }

            return readResponse(inputStream);

        } catch (Exception e) {
            throw new EsiaAuthException("Requesting for token : ", e);
        }
    }
}

