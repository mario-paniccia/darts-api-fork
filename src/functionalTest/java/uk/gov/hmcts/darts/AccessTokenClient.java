package uk.gov.hmcts.darts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

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

@RequiredArgsConstructor
public class AccessTokenClient {

    private final String tokenUri;
    private final String scope;
    private final String username;
    private final String password;
    private final String clientId;
    private final String clientSecret;


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
