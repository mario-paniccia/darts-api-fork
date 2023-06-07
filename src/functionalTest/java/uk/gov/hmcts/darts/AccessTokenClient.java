package uk.gov.hmcts.darts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Profile;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@TestComponent
@Profile("functionalTest")
public class AccessTokenClient {

    private final String tokenUri;
    private final String scope;
    private final String username;
    private final String password;
    private final String clientId;
    private final String clientSecret;

    public AccessTokenClient(@Value("${azure-ad-ropc.token-uri}") String tokenUri,
                             @Value("${azure-ad-ropc.scope}") String scope,
                             @NonNull @Value("${azure-ad-ropc.username}") String username,
                             @NonNull @Value("${azure-ad-ropc.password}") String password,
                             @NonNull @Value("${azure-ad-ropc.client-id}") String clientId,
                             @NonNull @Value("${azure-ad-ropc.client-secret}") String clientSecret) {
        this.tokenUri = tokenUri;
        this.scope = scope;
        this.username = username;
        this.password = password;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @SneakyThrows
    public String getAccessToken() {
        Map<String, String> params = Map.of("client_id", clientId,
                                            "client_secret", clientSecret,
                                            "scope", scope,
                                            "grant_type", "password",
                                            "username", username,
                                            "password", password
        );

        HttpRequest request = HttpRequest.newBuilder(URI.create(tokenUri))
            .POST(encode(params))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .build();

        String response = HttpClient.newHttpClient()
            .send(request, BodyHandlers.ofString())
            .body();

        TokenResponse tokenResponse = new ObjectMapper()
            .readValue(response, TokenResponse.class);

        return tokenResponse.accessToken();
    }

    @SuppressWarnings("PMD.LawOfDemeter")
    private BodyPublisher encode(Map<String, String> params) {
        String urlEncoded = params.entrySet()
            .stream()
            .map(entry -> new StringJoiner("=")
                .add(entry.getKey())
                .add(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .toString())
            .collect(Collectors.joining("&"));

        return HttpRequest.BodyPublishers.ofString(urlEncoded);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TokenResponse(@JsonProperty("access_token") String accessToken) {}

}
