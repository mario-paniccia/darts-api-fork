package uk.gov.hmcts.darts.authentication.component.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.darts.authentication.component.TokenValidator;
import uk.gov.hmcts.darts.authentication.config.AuthenticationConfiguration;
import uk.gov.hmcts.darts.authentication.model.JwtValidationResult;

import java.text.ParseException;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenValidatorImpl implements TokenValidator {

    private final JWKSource<SecurityContext> jwkSource;
    private final AuthenticationConfiguration authenticationConfiguration;

    @Override
    public JwtValidationResult validate(String accessToken) {
        log.debug("Validating JWT: {}", accessToken);

        var keySelector = new JWSVerificationKeySelector<>(
            JWSAlgorithm.RS256,
            jwkSource
        );

        var jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWSKeySelector(keySelector);

        JWTClaimsSet jwtClaimsSet = new Builder()
            .issuer(authenticationConfiguration.getExternalADissuerUri())
            .build();
        var claimsVerifier = new DefaultJWTClaimsVerifier<>(
            authenticationConfiguration.getExternalADclientId(),
            jwtClaimsSet,
            null
        );
        jwtProcessor.setJWTClaimsSetVerifier(claimsVerifier);

        try {
            jwtProcessor.process(accessToken, null);
        } catch (ParseException | JOSEException | BadJOSEException e) {
            log.debug("Validation failed", e);
            return new JwtValidationResult(false, e.getMessage());
        }

        log.debug("Validation successful");
        return new JwtValidationResult(true, null);
    }

}