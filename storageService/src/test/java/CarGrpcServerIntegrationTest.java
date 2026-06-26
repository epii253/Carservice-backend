import io.grpc.*;
import io.grpc.stub.MetadataUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.svyatniy.Main;
import ru.svyatniy.common.domain.proto.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.grpc.Status.Code.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(
        classes = Main.class,
        properties = {
                "grpc.server.port=19091",
                "grpc.server.security.enabled=false",
                "spring.security.oauth2.client.provider.keycloak.token-uri=http://localhost:9999/token",
                "spring.security.oauth2.client.registration.storage-client.provider=keycloak",
                "spring.security.oauth2.client.registration.storage-client.client-id=test",
                "spring.security.oauth2.client.registration.storage-client.client-secret=test",
                "spring.security.oauth2.client.registration.storage-client.authorization-grant-type=client_credentials",
                "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:9999/certs",
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "keycloak.enabled=true",
                "keycloak.url=http://localhost:9999",
                "keycloak.role-path=/admin/realms/lab_realm/roles/"
        }
)
@EmbeddedKafka(partitions = 1, topics = {"build_orders", "request_orders"})
@Testcontainers
class CarGrpcServerIntegrationTest {

    private static final int GRPC_PORT = 19091;
    private static final String VALID_TOKEN = "test-token";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("app_db_storage")
            .withUsername("app_user_storage")
            .withPassword("app_password_storage");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @MockitoBean
    JwtDecoder jwtDecoder;

    private ManagedChannel channel;
    private CarsServiceGrpc.CarsServiceBlockingStub unauthenticatedStub;
    private CarsServiceGrpc.CarsServiceBlockingStub authenticatedStub;

    @BeforeEach
    void setUp() {
        Jwt jwt = Jwt.withTokenValue(VALID_TOKEN)
                .header("alg", "none")
                .claim("sub", "test-service")
                .claim("realm_access", Map.of("roles", List.of("SYSTEM_ADMIN")))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        when(jwtDecoder.decode(VALID_TOKEN)).thenReturn(jwt);
        when(jwtDecoder.decode(not(eq(VALID_TOKEN)))).thenThrow(new JwtException("Invalid token"));

        channel = ManagedChannelBuilder.forAddress("localhost", GRPC_PORT)
                .usePlaintext()
                .build();

        unauthenticatedStub = CarsServiceGrpc.newBlockingStub(channel);

        Metadata authHeaders = new Metadata();
        authHeaders.put(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER), "Bearer " + VALID_TOKEN);
        authenticatedStub = unauthenticatedStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(authHeaders));
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    // --- GetAllCars ---

    @Test
    void getAllCars_withValidToken_returnsCarsList() {
        // Act
        GetAllCarsResponse response = authenticatedStub.getAllCars(GetAllCarsRequest.newBuilder().build());

        // Assert
        assertNotNull(response);
        assertNotNull(response.getCarsList());
    }

    @Test
    void getAllCars_withNoToken_returnsUnauthenticated() {
        // Act & Assert
        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                unauthenticatedStub.getAllCars(GetAllCarsRequest.newBuilder().build())
        );
        assertEquals(UNAUTHENTICATED, ex.getStatus().getCode());
    }

    @Test
    void getAllCars_withInvalidToken_returnsUnauthenticated() {
        // Arrange
        Metadata badHeaders = new Metadata();
        badHeaders.put(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER), "Bearer bad-token");

        // Act & Assert
        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                unauthenticatedStub
                        .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(badHeaders))
                        .getAllCars(GetAllCarsRequest.newBuilder().build())
        );
        assertEquals(UNAUTHENTICATED, ex.getStatus().getCode());
    }

    // --- GetCarById ---

    @Test
    void getCarById_existingCar_returnsCar() {
        // Arrange
        GetAllCarsResponse allCars = authenticatedStub.getAllCars(GetAllCarsRequest.newBuilder().build());
        assumeFalse(allCars.getCarsList().isEmpty(), "Requires seeded cars");
        String carId = allCars.getCars(0).getId();

        // Act
        GetCarByIdResponse response = authenticatedStub.getCarById(
                GetCarByIdRequest.newBuilder().setCarId(carId).build()
        );

        // Assert
        assertNotNull(response.getCar());
        assertEquals(carId, response.getCar().getId());
    }

    @Test
    void getCarById_unknownId_returnsNotFound() {
        // Act & Assert
        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                authenticatedStub.getCarById(
                        GetCarByIdRequest.newBuilder().setCarId(UUID.randomUUID().toString()).build()
                )
        );
        assertEquals(NOT_FOUND, ex.getStatus().getCode());
    }

    // --- CheckAvalForTestDriven ---

    @Test
    void checkAvalForTestDriven_existingModel_returnsResult() {
        // Arrange
        GetAllCarsResponse allCars = authenticatedStub.getAllCars(GetAllCarsRequest.newBuilder().build());
        assumeFalse(allCars.getCarsList().isEmpty(), "Requires seeded cars");
        String modelId = allCars.getCars(0).getId();

        // Act
        CheckAvalForTestDrivenResponse response = authenticatedStub.checkAvalForTestDriven(
                CheckAvalForTestDrivenRequest.newBuilder().setId(modelId).build()
        );

        // Assert
        assertNotNull(response);
        assertEquals(modelId, response.getId());
    }

    @Test
    void checkAvalForTestDriven_unknownModel_returnsNotAvailable() {
        // Arrange
        String unknownId = UUID.randomUUID().toString();

        // Act
        CheckAvalForTestDrivenResponse response = authenticatedStub.checkAvalForTestDriven(
                CheckAvalForTestDrivenRequest.newBuilder().setId(unknownId).build()
        );

        // Assert
        assertNotNull(response);
        assertFalse(response.getIsAvailable());
    }

    // --- CheckCarConfig ---

    @Test
    void checkCarConfig_unknownPartIds_returnsNotFound() {
        // Act & Assert
        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                authenticatedStub.checkCarConfig(CheckCarConfigRequest.newBuilder()
                        .setModelName("bmw320i")
                        .setColor("Alpine White")
                        .setRudderId(UUID.randomUUID().toString())
                        .setWheelId(UUID.randomUUID().toString())
                        .setTransmissionId(UUID.randomUUID().toString())
                        .setInteriorId(UUID.randomUUID().toString())
                        .setEngineId(UUID.randomUUID().toString())
                        .build()
                )
        );
        assertEquals(NOT_FOUND, ex.getStatus().getCode());
    }
}
