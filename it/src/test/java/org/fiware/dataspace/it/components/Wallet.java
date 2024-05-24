package org.fiware.dataspace.it.components;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.it.E;
import jakarta.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.fiware.dataspace.it.components.model.Credential;
import org.fiware.dataspace.it.components.model.CredentialOffer;
import org.fiware.dataspace.it.components.model.CredentialRequest;
import org.fiware.dataspace.it.components.model.Grant;
import org.fiware.dataspace.it.components.model.IssuerConfiguration;
import org.fiware.dataspace.it.components.model.OfferUri;
import org.fiware.dataspace.it.components.model.OpenIdConfiguration;
import org.fiware.dataspace.it.components.model.SupportedConfiguration;
import org.fiware.dataspace.it.components.model.TokenResponse;
import org.keycloak.common.VerificationException;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.fiware.dataspace.it.components.TestUtils.OBJECT_MAPPER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
public class Wallet {

    private static final String OPENID_CREDENTIAL_ISSUER_PATH = "/realms/test-realm/.well-known/openid-credential-issuer";
    private static final String CREDENTIAL_OFFER_URI_PATH = "/realms/test-realm/protocol/oid4vc/credential-offer-uri";
    private static final String OID_WELL_KNOWN_PATH = "/.well-known/openid-configuration";
    private static final String PRE_AUTHORIZED_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:pre-authorized_code";

    private static final String SAME_DEVICE_ENDPOINT = "/api/v1/samedevice";

    private static final HttpClient HTTP_CLIENT = HttpClient
            .newBuilder()
            // we don´t follow the redirect directly, since we are not a real wallet
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    public String getCredentialFromIssuer(String userToken, String issuerHost, String credentialId) throws Exception {
        IssuerConfiguration issuerConfiguration = getIssuerConfiguration(issuerHost);
        OfferUri offerUri = getCredentialOfferUri(userToken, issuerHost, credentialId);
        CredentialOffer credentialOffer = getCredentialOffer(userToken, offerUri);
        return getCredential(issuerConfiguration, credentialOffer);
    }

    public IssuerConfiguration getIssuerConfiguration(String issuerHost) throws Exception {
        HttpRequest configRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(issuerHost + OPENID_CREDENTIAL_ISSUER_PATH))
                .build();
        HttpResponse<String> configResponse = HTTP_CLIENT.send(configRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatus.SC_OK, configResponse.statusCode(), "An issuer config should have been returned.");
        return OBJECT_MAPPER.readValue(configResponse.body(), IssuerConfiguration.class);
    }

    public OfferUri getCredentialOfferUri(String keycloakJwt, String issuerHost, String credentialConfigId) throws Exception {
        URI requestUri = new URIBuilder(issuerHost + CREDENTIAL_OFFER_URI_PATH)
                .addParameter("credential_configuration_id", credentialConfigId)
                .build();
        HttpRequest uriRequest = HttpRequest.newBuilder()
                .GET()
                .uri(requestUri)
                .header("Authorization", "Bearer " + keycloakJwt)
                .build();
        HttpResponse<String> uriResponse = HTTP_CLIENT.send(uriRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatus.SC_OK, uriResponse.statusCode(), "An uri should have been returned.");
        return OBJECT_MAPPER.readValue(uriResponse.body(), OfferUri.class);
    }

    public CredentialOffer getCredentialOffer(String keycloakJwt, OfferUri offerUri) throws Exception {

        HttpRequest uriRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(offerUri.getIssuer() + offerUri.getNonce()))
                .header("Authorization", "Bearer " + keycloakJwt)
                .build();
        HttpResponse<String> offerResponse = HTTP_CLIENT.send(uriRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatus.SC_OK, offerResponse.statusCode(), "An offer should have been returned.");
        return OBJECT_MAPPER.readValue(offerResponse.body(), CredentialOffer.class);
    }

    public String getTokenForOffer(IssuerConfiguration issuerConfiguration, CredentialOffer credentialOffer) throws Exception {
        String authorizationServer = issuerConfiguration.getAuthorizationServers().get(0);
        OpenIdConfiguration openIdConfiguration = getOpenIdConfiguration(authorizationServer);
        assertTrue(openIdConfiguration.getGrantTypesSupported().contains(PRE_AUTHORIZED_GRANT_TYPE), "The grant type should actually be supported by the authorization server.");

        Grant preAuthorizedGrant = credentialOffer.getGrants().get(PRE_AUTHORIZED_GRANT_TYPE);
        return getAccessToken(openIdConfiguration.getTokenEndpoint(), preAuthorizedGrant.getPreAuthorizedCode());
    }

    public String getCredential(IssuerConfiguration issuerConfiguration, CredentialOffer credentialOffer) throws Exception {
        String accessToken = getTokenForOffer(issuerConfiguration, credentialOffer);

        String credentialResponse = credentialOffer.getCredentialConfigurationIds()
                .stream()
                .map(offeredCredentialId -> issuerConfiguration.getCredentialConfigurationsSupported().get(offeredCredentialId))
                .map(supportedCredential -> {
                    try {
                        return requestOffer(accessToken, issuerConfiguration.getCredentialEndpoint(), supportedCredential);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst()
                .get();
        return OBJECT_MAPPER.readValue(credentialResponse, Credential.class).getCredential();
    }

    private String requestOffer(String token, String credentialEndpoint, SupportedConfiguration offeredCredential) throws Exception {
        CredentialRequest credentialRequest = new CredentialRequest();
        credentialRequest.setCredentialIdentifier(offeredCredential.getId());
        credentialRequest.setFormat(offeredCredential.getFormat());

        HttpRequest credentialHttpRequest = HttpRequest.newBuilder()
                .uri(URI.create(credentialEndpoint))
                .POST(HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(credentialRequest)))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .build();
        HttpResponse<String> credentialResponse = HTTP_CLIENT.send(credentialHttpRequest,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpStatus.SC_OK, credentialResponse.statusCode(), "A credential should have been returned.");
        return credentialResponse.body();
    }

    public String getAccessToken(String tokenEndpoint, String preAuthorizedCode) throws Exception {

        Map<String, String> tokenRequestFormData = Map.of("grant_type",
                PRE_AUTHORIZED_GRANT_TYPE, "code",
                preAuthorizedCode);
        HttpRequest tokenRequest = HttpRequest.newBuilder()
                .uri(URI.create(tokenEndpoint))
                .POST(HttpRequest.BodyPublishers.ofString(TestUtils.getFormDataAsString(tokenRequestFormData)))
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED)
                .build();

        HttpResponse<String> tokenResponse = HTTP_CLIENT.send(tokenRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpStatus.SC_OK, tokenResponse.statusCode(), "A valid token should have been returned.");

        return OBJECT_MAPPER.readValue(tokenResponse.body(), TokenResponse.class).getAccessToken();
    }

    public OpenIdConfiguration getOpenIdConfiguration(String authorizationServer) throws Exception {
        HttpRequest uriRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(authorizationServer + OID_WELL_KNOWN_PATH))
                .build();
        HttpResponse<String> openIdConfigResponse = HTTP_CLIENT.send(uriRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatus.SC_OK, openIdConfigResponse.statusCode(), "An openId config should have been returned.");
        return OBJECT_MAPPER.readValue(openIdConfigResponse.body(), OpenIdConfiguration.class);
    }


}
