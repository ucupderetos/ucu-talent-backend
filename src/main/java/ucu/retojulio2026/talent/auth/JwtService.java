package ucu.retojulio2026.talent.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import ucu.retojulio2026.talent.user.User;

@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final long expirationMinutes;

    public JwtService(JwtEncoder encoder, @Value("${jwt.expiration-minutes}") long expirationMinutes) {
        this.encoder = encoder;
        this.expirationMinutes = expirationMinutes;
    }

    public String issue(User user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("talent-api")
                .issuedAt(now)
                .expiresAt(now.plus(expirationMinutes, ChronoUnit.MINUTES))
                .subject(user.getUserId())
                .claim("role", user.getRole().name())
                .build();

        // Sin esto, NimbusJwtEncoder asume por default un algoritmo asimetrico (RS256) para
        // elegir la JWK con la que firmar - incompatible con nuestra llave simetrica (HMAC).
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        return encoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }
}
