package presentation.security;

import io.grpc.*;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@GrpcGlobalClientInterceptor
public class AuthClientInterceptor implements ClientInterceptor {

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final String clientId;
    private static final String REGISTRATION_ID = "order-client";
    private static final Metadata.Key<String> AUTH_KEY =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    public AuthClientInterceptor(
            OAuth2AuthorizedClientManager authorizedClientManager,
            @Value("${spring.security.oauth2.client.registration.order-client.client-id}") String clientId) {
        this.authorizedClientManager = authorizedClientManager;
        this.clientId = clientId;
    }

    @Override
    public <Q, R> ClientCall<Q, R> interceptCall(
            MethodDescriptor<Q, R> method, CallOptions callOptions, Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<>(
                next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<R> responseListener, Metadata headers) {
                String token = resolveToken();
                if (token != null) {
                    headers.put(AUTH_KEY, "Bearer " + token);
                }
                super.start(responseListener, headers);
            }
        };
    }

    private String resolveToken() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getTokenValue();
        }

        OAuth2AuthorizedClient client = authorizedClientManager.authorize(
                OAuth2AuthorizeRequest.withClientRegistrationId(REGISTRATION_ID)
                        .principal(clientId)
                        .build());
        return client != null ? client.getAccessToken().getTokenValue() : null;
    }
}