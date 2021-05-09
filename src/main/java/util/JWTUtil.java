package util;


import com.google.common.collect.Maps;
import exception.CommonException;
import lombok.SneakyThrows;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

import static util.CommonUtils.msg;
import static util.Constants.AuthConstants.*;


/**
 * @author gvaddepally on 27/05/20
 */
public class JWTUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JWTUtil.class);

    public static Map<String, String> generateTokens(String issuer, String audience, Map<String, String> claimsMap) {
        Map<String, String> tokensNap = Maps.newHashMap();
        JwtClaims jwtClaims = createAuthClaims(issuer, audience, claimsMap);
        JwtClaims refreshToken = createRefreshClaims(jwtClaims);
        String signedAuthJWT = signJWT(jwtClaims);
        String encryptedAuthJWT = encryptJWT(signedAuthJWT);
        tokensNap.put(AUTH_TOKEN, encryptedAuthJWT);
        String signedRefreshJWT = signJWT(refreshToken);
        String encryptedRefreshJWT = encryptJWT(signedRefreshJWT);
        tokensNap.put(REFRESH_TOKEN, encryptedRefreshJWT);
        return tokensNap;
    }

    private static JwtClaims createAuthClaims(String issuer, String audience, Map<String, String> claimsMap) {
        JwtClaims claims = new JwtClaims();
        claims.setIssuer(issuer);
        claims.setAudience(audience);
        claims.setExpirationTimeMinutesInTheFuture(AUTH_TOKEN_VALIDITY);
        claims.setGeneratedJwtId();
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(0);
        claims.setSubject(AUTH);
        for (String key : claimsMap.keySet()) {
            claims.setClaim(key, claimsMap.get(key));
        }
        return claims;
    }

    @SneakyThrows
    private static JwtClaims createRefreshClaims(JwtClaims authClaims) {
        JwtClaims claims = new JwtClaims();
        claims.setIssuer(authClaims.getIssuer());
        claims.setAudience(authClaims.getAudience());
        claims.setGeneratedJwtId();
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(0);
        claims.setSubject(AUTH_REFRESH);
        for (String claim : authClaims.getClaimsMap().keySet()) {
            claims.setClaim(claim, authClaims.getClaimValue(claim));
        }
        claims.setClaim(AUTH_TOKEN_CLAIM_ID, authClaims.getJwtId());
        return claims;
    }

    private static String signJWT(JwtClaims jwtClaims) {
        try {
            JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(jwtClaims.toJson());
            jws.setKey(EncryptionUtil.getSecretKey());
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
            String jwt = jws.getCompactSerialization();
            return jwt;
        } catch (JoseException ex) {
            String message = msg("Error while signing the jwt due to {}", ex.getMessage());
            LOGGER.error(message, ex);
            throw new CommonException(message, ex);
        }
    }

    private static String encryptJWT(String signedJwt) {
        try {
            JsonWebEncryption jwe = new JsonWebEncryption();
            jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.DIRECT);
            jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
            jwe.setKey(EncryptionUtil.getSecretKey());
            jwe.setContentTypeHeaderValue("JWT");
            jwe.setPayload(signedJwt);
            String jwt = jwe.getCompactSerialization();
            return jwt;
        } catch (Exception ex) {
            String message = msg("Error while encrypting the jwt due to {}", ex.getMessage());
            LOGGER.error(message, ex);
            throw new CommonException(message, ex);
        }
    }

    public static JwtClaims validateJWT(String jwt, String issuer, String audience) {
        JwtConsumer jwtConsumer = getJwtConsumer(false, issuer, audience);
        try {
            JwtClaims jwtClaims = jwtConsumer.processToClaims(jwt);
            LOGGER.info("JWT validation succeeded! ", jwtClaims);
            return jwtClaims;
        } catch (InvalidJwtException e) {
            LOGGER.error("Invalid JWT! ", e.getErrorDetails());
            String message = msg("Auth token validation failed due to {}", e.getErrorDetails());
            throw new CommonException(message, e);
        }
    }

    public static boolean validateRefreshToken(String authToken, String refreshToken, String issuer, String audience) {
        JwtConsumer jwtConsumer = getJwtConsumer(false, issuer, audience);
        try {
            jwtConsumer.processToClaims(authToken);
            return true;
        } catch (InvalidJwtException e) {
            if (e.hasExpired()) {
                LOGGER.error("JWT is expired refreshing the auth token ");
                JwtClaims jwtClaims = e.getJwtContext().getJwtClaims();
                try {
                    jwtConsumer = getJwtConsumer(true, issuer, audience);
                    JwtClaims refreshJwtClaims = jwtConsumer.processToClaims(refreshToken);
                    if (jwtClaims.getJwtId().equalsIgnoreCase(refreshJwtClaims.getClaimValueAsString(AUTH_TOKEN_CLAIM_ID))) {
                        return true;
                    }
                } catch (InvalidJwtException | MalformedClaimException invalidJwtException) {
                    String message = msg("refresh token validation failed due to {}", e.getErrorDetails());
                    throw new CommonException(message, e);
                }
            }
        }
        return false;
    }

    public static String updateAndGetAuthToken(String authToken, String issuer, String audience) {
        JwtConsumer jwtConsumer = getJwtConsumer(false, issuer, audience);
        try {
            JwtClaims jwtClaims = jwtConsumer.processToClaims(authToken);
            return updateAuthToken(Optional.empty(), Optional.of(jwtClaims));
        } catch (InvalidJwtException e) {
            if (e.hasExpired()) {
                return updateAuthToken(Optional.of(e), Optional.empty());
            } else {
                LOGGER.error("Error while updating auth token ", e.getMessage());
                String message = msg("auth token update failed due to {}", e.getMessage());
                throw new CommonException(message, e);
            }
        }
    }

    private static String updateAuthToken(Optional<InvalidJwtException> e, Optional<JwtClaims> jwtClaims) {
        LOGGER.error("auth token expired so updating auth token ");
        JwtClaims authJwtClaims = jwtClaims.orElseGet(() -> e.get().getJwtContext().getJwtClaims());
        authJwtClaims.setExpirationTimeMinutesInTheFuture(AUTH_TOKEN_VALIDITY);
        String signedAuthJWT = signJWT(authJwtClaims);
        String encryptedAuthJWT = encryptJWT(signedAuthJWT);
        return encryptedAuthJWT;
    }

    private static JwtConsumer getJwtConsumer(boolean isRefresh, String issuer, String audience) {
        AlgorithmConstraints jwsAlgConstraints = new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                AlgorithmIdentifiers.HMAC_SHA256);

        AlgorithmConstraints jweAlgConstraints = new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                KeyManagementAlgorithmIdentifiers.DIRECT);

        AlgorithmConstraints jweEncConstraints = new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);

        JwtConsumerBuilder jwtConsumerBuilder = new JwtConsumerBuilder()
                .setRequireSubject()
                .setExpectedSubject(AUTH)
                .setExpectedIssuer(issuer)
                .setExpectedAudience(audience)
                .setDecryptionKey(EncryptionUtil.getSecretKey())
                .setVerificationKey(EncryptionUtil.getSecretKey())
                .setJwsAlgorithmConstraints(jwsAlgConstraints)
                .setJweAlgorithmConstraints(jweAlgConstraints)
                .setJweContentEncryptionAlgorithmConstraints(jweEncConstraints);

        if (isRefresh) {
            jwtConsumerBuilder.setExpectedSubject(AUTH_REFRESH);
        } else {
            jwtConsumerBuilder.setRequireExpirationTime()
                    .setMaxFutureValidityInMinutes(30);
        }
        return jwtConsumerBuilder.build();
    }

}
