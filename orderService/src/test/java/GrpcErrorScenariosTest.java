import application.services.grpc.CarGrpcClientFacade;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.svyatniy.Main;
import ru.svyatniy.common.domain.proto.*;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = Main.class,
        properties = {
                "grpc.client.cars-service.address=static://localhost:19093",
                "grpc.client.cars-service.negotiation-type=plaintext",
                "spring.security.oauth2.client.provider.keycloak.token-uri=http://localhost:9999/token",
                "spring.security.oauth2.client.registration.order-client.provider=keycloak",
                "spring.security.oauth2.client.registration.order-client.client-id=test",
                "spring.security.oauth2.client.registration.order-client.client-secret=test",
                "spring.security.oauth2.client.registration.order-client.authorization-grant-type=client_credentials",
                "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:9999/certs",
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "keycloak.enabled=true",
                "services.storage-service.url=http://localhost:9999"
        }
)
@EmbeddedKafka(partitions = 1, topics = {"build_orders", "request_orders"})
@Testcontainers
class GrpcErrorScenariosTest {

    private static final int ERROR_SERVER_PORT = 19093;

    private static Server errorCarsServer;

    // Holds the status the fake server will return: changed per-test
    private static volatile Status serverStatus = Status.OK;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("app_db")
            .withUsername("app_user")
            .withPassword("app_password");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @MockitoBean
    JwtDecoder jwtDecoder;

    @MockitoBean
    OAuth2AuthorizedClientManager authorizedClientManager;

    @Autowired
    CarGrpcClientFacade carGrpcClient;

    static {
        try {
            errorCarsServer = ServerBuilder.forPort(ERROR_SERVER_PORT)
                    .addService(new ConfigurableErrorService())
                    .build()
                    .start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to start error gRPC server", e);
        }
    }

    @AfterAll
    static void stopErrorServer() {
        if (errorCarsServer != null) {
            errorCarsServer.shutdown();
        }
    }

    @BeforeEach
    void setUpSecurityContext() {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("sub", "test-user")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // --- Error scenario tests ---

    @Test
    void getAllCars_serverReturnsNotFound_throwsStatusRuntimeException() {
        // Arrange
        serverStatus = Status.NOT_FOUND;

        // Act & Assert
        assertThrows(StatusRuntimeException.class, () -> carGrpcClient.getAllCars());
    }

    @Test
    void getAllCars_serverReturnsUnavailable_throwsStatusRuntimeException() {
        // Arrange
        serverStatus = Status.UNAVAILABLE;

        // Act & Assert
        assertThrows(StatusRuntimeException.class, () -> carGrpcClient.getAllCars());
    }

    @Test
    void checkCarConfig_serverReturnsAborted_throwsStatusRuntimeException() {
        // Arrange
        serverStatus = Status.ABORTED;

        // Act & Assert
        assertThrows(StatusRuntimeException.class, () ->
                carGrpcClient.checkCarConfig(
                        new domain.valueObjects.ModelName("model"),
                        new domain.valueObjects.Color("Red"),
                        UUID.randomUUID(), UUID.randomUUID(),
                        UUID.randomUUID(), UUID.randomUUID(),
                        UUID.randomUUID()
                )
        );
    }

    @Test
    void getCarById_serverReturnsDeadlineExceeded_throwsStatusRuntimeException() {
        // Arrange
        serverStatus = Status.DEADLINE_EXCEEDED;

        // Act & Assert
        assertThrows(StatusRuntimeException.class, () ->
                carGrpcClient.getCarById(UUID.randomUUID())
        );
    }

    @Test
    void anyCall_serverReturnsUnauthenticated_throwsStatusRuntimeException() {
        // Arrange
        serverStatus = Status.UNAUTHENTICATED;

        // Act & Assert
        assertThrows(StatusRuntimeException.class, () -> carGrpcClient.getAllCars());
    }

    @Test
    void getAllCars_serverReturnsEmptyList_returnsEmptyResponse() throws Exception {
        // Arrange
        serverStatus = Status.OK;

        // Act
        var cars = carGrpcClient.getAllCars();

        // Assert
        assertNotNull(cars);
        assertTrue(cars.isEmpty());
    }

    // --- Fake server that returns a configurable error status ---

    static class ConfigurableErrorService extends CarsServiceGrpc.CarsServiceImplBase {

        private void replyOrError(StreamObserver<?> observer, com.google.protobuf.Message emptyOk) {
            if (serverStatus.getCode() == Status.Code.OK) {
                //noinspection unchecked
                ((StreamObserver<com.google.protobuf.Message>) observer).onNext(emptyOk);
                observer.onCompleted();
            } else {
                observer.onError(serverStatus.asException());
            }
        }

        @Override
        public void getAllCars(GetAllCarsRequest request, StreamObserver<GetAllCarsResponse> observer) {
            replyOrError(observer, GetAllCarsResponse.getDefaultInstance());
        }

        @Override
        public void getCarById(GetCarByIdRequest request, StreamObserver<GetCarByIdResponse> observer) {
            replyOrError(observer, GetCarByIdResponse.getDefaultInstance());
        }

        @Override
        public void checkCarConfig(CheckCarConfigRequest request, StreamObserver<CheckCarConfigResponse> observer) {
            replyOrError(observer, CheckCarConfigResponse.getDefaultInstance());
        }

        @Override
        public void checkAvalForTestDriven(CheckAvalForTestDrivenRequest request, StreamObserver<CheckAvalForTestDrivenResponse> observer) {
            replyOrError(observer, CheckAvalForTestDrivenResponse.getDefaultInstance());
        }
    }
}
