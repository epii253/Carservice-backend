import application.contracts.dataobjects.cars.CarConfigResult;
import application.contracts.dataobjects.cars.CarInfo;
import application.contracts.dataobjects.cars.TestDriveAvailability;
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
                "grpc.client.cars-service.address=static://localhost:19092",
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
class CarGrpcClientIntegrationTest {

    private static final int FAKE_SERVER_PORT = 19092;
    private static final String FAKE_CAR_ID = UUID.randomUUID().toString();
    private static final String FAKE_MODEL_ID = UUID.randomUUID().toString();

    private static Server fakeCarsServer;

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

    // Replaced with a mock so Spring doesn't try to reach real Keycloak
    @MockitoBean
    JwtDecoder jwtDecoder;

    @MockitoBean
    OAuth2AuthorizedClientManager authorizedClientManager;

    @Autowired
    CarGrpcClientFacade carGrpcClient;

    static {
        try {
            fakeCarsServer = ServerBuilder.forPort(FAKE_SERVER_PORT)
                    .addService(new FakeCarsService())
                    .build()
                    .start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to start fake gRPC server", e);
        }
    }

    @AfterAll
    static void stopFakeServer() {
        if (fakeCarsServer != null) {
            fakeCarsServer.shutdown();
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

    @Test
    void getAllCars_callsFakeServer_returnsCars() throws Exception {
        // Act
        List<CarInfo> cars = carGrpcClient.getAllCars();

        // Assert
        assertNotNull(cars);
        assertEquals(1, cars.size());
        assertEquals(FAKE_CAR_ID, cars.get(0).id());
        assertEquals("test-model", cars.get(0).modelName());
    }

    @Test
    void getCarById_callsFakeServer_returnsCar() throws Exception {
        // Arrange
        UUID carId = UUID.fromString(FAKE_CAR_ID);

        // Act
        CarInfo car = carGrpcClient.getCarById(carId);

        // Assert
        assertNotNull(car);
        assertEquals(FAKE_CAR_ID, car.id());
    }

    @Test
    void checkCarConfig_callsFakeServer_returnsResponse() throws Exception {
        // Act
        CarConfigResult result = carGrpcClient.checkCarConfig(
                new domain.valueObjects.ModelName("test-model"),
                new domain.valueObjects.Color("Red"),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        // Assert
        assertNotNull(result);
        assertNotNull(result.modelId());
    }

    @Test
    void checkAvalForTestDriven_callsFakeServer_returnsAvailability() throws Exception {
        // Arrange
        UUID modelId = UUID.fromString(FAKE_MODEL_ID);

        // Act
        TestDriveAvailability availability = carGrpcClient.checkAvalForTestDriven(modelId);

        // Assert
        assertNotNull(availability);
        assertEquals(modelId, availability.modelId());
        assertTrue(availability.isAvailable());
    }

    // --- Fake gRPC server ---

    static class FakeCarsService extends CarsServiceGrpc.CarsServiceImplBase {

        @Override
        public void getAllCars(GetAllCarsRequest request, StreamObserver<GetAllCarsResponse> observer) {
            observer.onNext(GetAllCarsResponse.newBuilder()
                    .addCars(ru.svyatniy.common.domain.proto.CarInfo.newBuilder()
                            .setId(FAKE_CAR_ID)
                            .setModelName("test-model")
                            .setBrandName("test-brand")
                            .setColor("Red")
                            .setPrice(50000)
                            .setGearBoxType("AUTO")
                            .setCarCase("SEDAN")
                            .setWheelDrive("AWD")
                            .build())
                    .build());
            observer.onCompleted();
        }

        @Override
        public void getCarById(GetCarByIdRequest request, StreamObserver<GetCarByIdResponse> observer) {
            observer.onNext(GetCarByIdResponse.newBuilder()
                    .setCar(ru.svyatniy.common.domain.proto.CarInfo.newBuilder()
                            .setId(request.getCarId())
                            .setModelName("test-model")
                            .setBrandName("test-brand")
                            .setColor("Red")
                            .setPrice(50000)
                            .build())
                    .build());
            observer.onCompleted();
        }

        @Override
        public void checkCarConfig(CheckCarConfigRequest request, StreamObserver<CheckCarConfigResponse> observer) {
            observer.onNext(CheckCarConfigResponse.newBuilder()
                    .setModelId(UUID.randomUUID().toString())
                    .build());
            observer.onCompleted();
        }

        @Override
        public void checkAvalForTestDriven(CheckAvalForTestDrivenRequest request, StreamObserver<CheckAvalForTestDrivenResponse> observer) {
            observer.onNext(CheckAvalForTestDrivenResponse.newBuilder()
                    .setId(request.getId())
                    .setIsAvailable(true)
                    .build());
            observer.onCompleted();
        }
    }
}
