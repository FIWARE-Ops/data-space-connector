package org.fiware.dataspace.it.components;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import org.apache.http.HttpStatus;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.fiware.dataspace.it.components.model.OpenIdConfiguration;
import org.fiware.dataspace.it.components.model.TokenResponse;
import org.fiware.dataspace.it.components.model.VerifiablePresentation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.common.crypto.CryptoIntegration;
import org.keycloak.crypto.ECDSASignatureSignerContext;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.representations.JsonWebToken;

import java.security.Security;
import java.time.Clock;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.fiware.dataspace.it.components.TestUtils.OBJECT_MAPPER;
import static org.fiware.dataspace.it.components.TestUtils.getConsumerDid;
import static org.fiware.dataspace.it.components.TestUtils.getConsumerKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
public class TestTest {

    private static final Map<String, String> DEFAULT_ODRL_CONTEXT = Map.of(
            "dc", "http://purl.org/dc/elements/1.1/",
            "dct", "http://purl.org/dc/terms/",
            "owl", "http://www.w3.org/2002/07/owl#",
            "odrl", "http://www.w3.org/ns/odrl/2/",
            "rdfs", "http://www.w3.org/2000/01/rdf-schema#",
            "skos", "http://www.w3.org/2004/02/skos/core#",
            "xsd", "http://www.w3.org/2001/XMLSchema#"
    );
    private static final String entityPath = "/ngsi-ld/v1/entities";
    private static final String oidWellKnownPath = "/.well-known/openid-configuration";

    private static final String consumerKeycloak = "http://keycloak-consumer.127.0.0.1.nip.io:8080";

    private static final String providerDataPlane = "http://api.127.0.0.1.nip.io:8080";
    private static final String providerKeycloak = "http://keycloak-provider.127.0.0.1.nip.io:8080";
    private static final String providerPap = "http://provider-pap.127.0.0.1.nip.io:8080";

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();

    @BeforeEach
    public void setup() {
        CryptoIntegration.init(this.getClass().getClassLoader());
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void test() throws Exception {

        KeycloakHelper providerKeycloak = new KeycloakHelper("test-realm", consumerKeycloak);
        String userToken = providerKeycloak.getUserToken("test-user", "test");
        Wallet wallet = new Wallet();
        String credential = wallet.getCredentialFromIssuer(userToken, consumerKeycloak, "service-account");
        assertFalse(credential.isEmpty(), "A credential should have been issued.");

        Request entityRequest = new Request.Builder().get()
                .url(providerDataPlane + entityPath)
                .build();

        Response entityResponse = HTTP_CLIENT.newCall(entityRequest).execute();
        assertEquals(HttpStatus.SC_UNAUTHORIZED, entityResponse.code(), "A request without a valid jwt should be rejected.");

        Request wellKnownRequest = new Request.Builder().get()
                .url(providerDataPlane + oidWellKnownPath)
                .build();
        Response wellKnownResponse = HTTP_CLIENT.newCall(wellKnownRequest).execute();

        assertEquals(HttpStatus.SC_OK, wellKnownResponse.code(), "The oidc config should have been returned.");
        OpenIdConfiguration openIdConfiguration = OBJECT_MAPPER.readValue(wellKnownResponse.body().string(), OpenIdConfiguration.class);
        assertTrue(openIdConfiguration.getGrantTypesSupported().contains("vp_token"), "The endpoint should support vp_tokens");
        assertTrue(openIdConfiguration.getResponseModeSupported().contains("direct_post"), "The endpoint should support direct_post");
        assertNotNull(openIdConfiguration.getTokenEndpoint(), "The token endpoint needs to be returned.");

        KeyWrapper consumerKey = getConsumerKey();
        String did = getConsumerDid();
        String vpToken = Base64.getUrlEncoder().withoutPadding().encodeToString(createVPToken(did, consumerKey, credential).getBytes());

        RequestBody requestBody = new FormEncodingBuilder()
                .add("grant_type", "vp_token")
                .add("vp_token", vpToken)
                .add("scope", "default")
                .build();
        Request tokenRequest = new Request.Builder()
                .post(requestBody)
                .addHeader("client_id", "data-plane")
                .url(openIdConfiguration.getTokenEndpoint())
                .build();

        Response tokenResponse = HTTP_CLIENT.newCall(tokenRequest).execute();
        assertEquals(HttpStatus.SC_OK, tokenResponse.code(), "A token should have been responded.");

        TokenResponse accessTokenResponse = OBJECT_MAPPER.readValue(tokenResponse.body().string(), TokenResponse.class);
        assertNotNull(accessTokenResponse.getAccessToken(), "The access token should have been returned.");

        Request authenticatedEntityRequest = new Request.Builder().get()
                .url(providerDataPlane + entityPath)
                .addHeader("Authorization", "Bearer " + accessTokenResponse.getAccessToken())
                .build();

        Response authenticatedEntityResponse = HTTP_CLIENT.newCall(authenticatedEntityRequest).execute();
        assertEquals(HttpStatus.SC_FORBIDDEN, authenticatedEntityResponse.code(), "Without a matching policy, the request should be forbidden.");
    }

    private String createVPToken(String did, KeyWrapper key, String credential) {
        VerifiablePresentation verifiablePresentation = new VerifiablePresentation();
        verifiablePresentation.setVerifiableCredential(List.of(credential));
        verifiablePresentation.setHolder(did);
        key.setKid(did);
        key.setAlgorithm("ES256");
        key.setUse(KeyUse.SIG);

        ECDSASignatureSignerContext signerContext = new ECDSASignatureSignerContext(key);

        JsonWebToken jwt = new JsonWebToken()
                .issuer(did)
                .subject(did)
                .iat(Clock.systemUTC().millis());
        jwt.setOtherClaims("vp", verifiablePresentation);
        return new JWSBuilder()
                .type("JWT")
                .jsonContent(jwt)
                .sign(signerContext);
    }

}
