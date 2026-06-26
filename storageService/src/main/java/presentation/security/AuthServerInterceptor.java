package presentation.security;

import io.grpc.*;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@GrpcGlobalServerInterceptor
@Component
public class AuthServerInterceptor implements ServerInterceptor {

    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    static final Context.Key<Jwt> JWT_CONTEXT = Context.key("jwt");

    public AuthServerInterceptor(JwtDecoder jwtDecoder,
                                  JwtAuthenticationConverter jwtAuthenticationConverter) {
        this.jwtDecoder = jwtDecoder;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }

    @Override
    public <Q, R> ServerCall.Listener<Q> interceptCall(
            ServerCall<Q, R> call, Metadata headers, ServerCallHandler<Q, R> next) {

        String authHeader = headers.get(
                Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER));

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            call.close(Status.UNAUTHENTICATED.withDescription("Missing token"), new Metadata());
            return new ServerCall.Listener<>() {};
        }

        try {
            Jwt jwt = jwtDecoder.decode(authHeader.substring(7));
            JwtAuthenticationToken authentication =
                    (JwtAuthenticationToken) jwtAuthenticationConverter.convert(jwt);

            Context ctx = Context.current().withValue(JWT_CONTEXT, jwt);
            ServerCall.Listener<Q> delegate = Contexts.interceptCall(ctx, call, headers, next);

            return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(delegate) {
                private void runWithAuth(Runnable r) {
                    SecurityContext secCtx = SecurityContextHolder.createEmptyContext();
                    secCtx.setAuthentication(authentication);
                    SecurityContextHolder.setContext(secCtx);
                    try {
                        r.run();
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                }

                @Override
                public void onMessage(Q message) {
                    runWithAuth(() -> super.onMessage(message));
                }

                @Override
                public void onHalfClose() {
                    runWithAuth(super::onHalfClose);
                }

                @Override
                public void onComplete() {
                    runWithAuth(super::onComplete);
                }

                @Override
                public void onCancel() {
                    SecurityContextHolder.clearContext();
                    super.onCancel();
                }
            };
        } catch (Exception e) {
            call.close(Status.UNAUTHENTICATED.withDescription("Invalid token"), new Metadata());
            return new ServerCall.Listener<>() {};
        }
    }
}
