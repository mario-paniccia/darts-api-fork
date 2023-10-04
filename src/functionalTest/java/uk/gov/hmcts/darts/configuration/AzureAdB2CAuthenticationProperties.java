package uk.gov.hmcts.darts.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("azure-ad-b2c-auth")
@Getter
@Setter
public class AzureAdB2CAuthenticationProperties {

    private String tokenUri;
    private String scope;
    private String username;
    private String password;
    private String clientId;
    private String clientSecret;

}