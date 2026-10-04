package com.cocinacero.auth.service;

import com.cocinacero.auth.model.Usuario;
import com.cocinacero.comun.ApiError;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final JwtEncoder encoder;
  private final JwtDecoder decoder;

  public JwtService(@Value("${app.jwt-secret}") String secret) {
    if (secret.getBytes(StandardCharsets.UTF_8).length < 32)
      throw new IllegalStateException("Configura app.jwt-secret con al menos 32 bytes.");
    var key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
    var d = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    d.setJwtValidator(JwtValidators.createDefaultWithIssuer("cocina-cero-deber3"));
    decoder = d;
  }

  public String emitir(Usuario u) {
    Instant now = Instant.now();
    return encoder
        .encode(
            JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder()
                    .issuer("cocina-cero-deber3")
                    .audience(List.of("cocina-cero-deber3-api"))
                    .subject(u.id.toString())
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(3600))
                    .claim("ver", u.tokenVersion)
                    .build()))
        .getTokenValue();
  }

  public Jwt leer(String token) {
    try {
      Jwt j = decoder.decode(token);
      if (!j.getAudience().contains("cocina-cero-deber3-api")) throw new IllegalArgumentException();
      return j;
    } catch (Exception e) {
      throw new ApiError(401, "Token inválido o vencido.");
    }
  }
}
