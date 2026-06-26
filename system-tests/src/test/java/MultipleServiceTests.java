import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.RestAssured;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Slf4j
@Testcontainers
class MultipleServiceTests {

    static Network network = Network.newNetwork();

    @Container
    static PostgreSQLContainer<?> postgresOrder =
            new PostgreSQLContainer<>("postgres:16")
                    .withNetwork(network).withNetworkAliases("database")
                    .withDatabaseName("app_db").withUsername("app_user").withPassword("app_password");

    @Container
    static PostgreSQLContainer<?> postgresStorage =
            new PostgreSQLContainer<>("postgres:16")
                    .withNetwork(network).withNetworkAliases("database_storage")
                    .withDatabaseName("app_db_storage").withUsername("app_user_storage").withPassword("app_password_storage");

    @Container
    static PostgreSQLContainer<?> postgresKeycloak =
            new PostgreSQLContainer<>("postgres:16")
                    .withNetwork(network).withNetworkAliases("database_keycloak")
                    .withDatabaseName("app_db_keycloak").withUsername("app_user_keycloak").withPassword("app_password_keycloak");

    @Container
    static GenericContainer<?> kafka =
            new GenericContainer<>("apache/kafka-native:3.8.0")
                    .withNetwork(network).withNetworkAliases("kafka")
                    .withExposedPorts(9092)
                    .withEnv("KAFKA_NODE_ID", "1")
                    .withEnv("KAFKA_PROCESS_ROLES", "broker,controller")
                    .withEnv("KAFKA_CONTROLLER_QUORUM_VOTERS", "1@kafka:29093")
                    .withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT")
                    .withEnv("KAFKA_LISTENERS", "PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:29093")
                    .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://kafka:9092")
                    .withEnv("KAFKA_INTER_BROKER_LISTENER_NAME", "PLAINTEXT")
                    .withEnv("KAFKA_CONTROLLER_LISTENER_NAMES", "CONTROLLER")
                    .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
                    .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
                    .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
                    .withEnv("KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS", "0")
                    .withEnv("CLUSTER_ID", "MkU3OEVBNTcwNTJENDM2Qk")
                    .waitingFor(Wait.forListeningPort());

    @Container
    static KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:26.0")
            .withNetwork(network).withNetworkAliases("keycloak")
            .withRealmImportFile("keycloak/realm-export.json")
            .withEnv("KC_HOSTNAME", "keycloak").withEnv("KC_HOSTNAME_PORT", "8080")
            .withEnv("KC_HOSTNAME_STRICT", "false").withEnv("KC_HOSTNAME_STRICT_BACKCHANNEL", "false")
            .withEnv("KC_DB", "postgres")
            .withEnv("KC_DB_URL", "jdbc:postgresql://database_keycloak:5432/app_db_keycloak")
            .withEnv("KC_DB_USERNAME", "app_user_keycloak").withEnv("KC_DB_PASSWORD", "app_password_keycloak")
            .withEnv("KC_HOSTNAME_URL", "http://keycloak:8080").withEnv("KC_HTTP_RELATIVE_PATH", "/")
            .dependsOn(postgresKeycloak);

    static final Path projectRoot = Paths.get("/home/epii/IdeaProjects/epii253_template");
    static String orderServiceSecret;
    static String storageServiceSecret;

    static {
        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "build",
                    "-f", projectRoot.resolve("docker/orderservice/Dockerfile").toString(),
                    "-t", "order-service-tests:latest", projectRoot.toString());
            pb.redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT);

            if (pb.start().waitFor() != 0)
                throw new RuntimeException("order-service Docker build failed");

            ProcessBuilder pb2 = new ProcessBuilder("docker", "build",
                    "-f", projectRoot.resolve("docker/storageservice/Dockerfile").toString(),
                    "-t", "storage-service-tests:latest", projectRoot.toString());
            pb2.redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT);

            if (pb2.start().waitFor() != 0)
                throw new RuntimeException("storage-service Docker build failed");

            JsonNode cfg = new ObjectMapper(new YAMLFactory()).readTree(
                    projectRoot.resolve("system-tests/src/test/resources/application.yml").toFile());

            orderServiceSecret = cfg.path("services").path("secrets").path("order").asText();
            storageServiceSecret = cfg.path("services").path("secrets").path("storage").asText();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Container
    static GenericContainer<?> storageService =
            new GenericContainer<>("storage-service-tests:latest")
                    .withNetwork(network).withNetworkAliases("storage-service").withExposedPorts(8080)
                    .withLogConsumer(new org.testcontainers.containers.output.Slf4jLogConsumer(log).withPrefix("STORAGE"))
                    .withEnv("DB_HOST", "database_storage").withEnv("DB_PORT", "5432")
                    .withEnv("DB_NAME", "app_db_storage").withEnv("DB_USER", "app_user_storage").withEnv("DB_PASSWORD", "app_password_storage")
                    .withEnv("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")
                    .withEnv("KEYCLOAK_URL", "http://keycloak:8080")
                    .withEnv("SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI",
                            "http://keycloak:8080/realms/lab_realm/protocol/openid-connect/certs")
                    .withEnv("SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_KEYCLOAK_TOKEN_URI",
                            "http://keycloak:8080/realms/lab_realm/protocol/openid-connect/token")
                    .waitingFor(Wait.forLogMessage(".*Started.*in \\d+.*\\n", 1))
                    .dependsOn(postgresStorage, keycloak);

    @Container
    static GenericContainer<?> orderService =
            new GenericContainer<>("order-service-tests:latest")
                    .withNetwork(network).withNetworkAliases("order-service").withExposedPorts(8080)
                    .withLogConsumer(new org.testcontainers.containers.output.Slf4jLogConsumer(log).withPrefix("ORDER"))
                    .withEnv("DB_HOST", "database").withEnv("DB_PORT", "5432")
                    .withEnv("DB_NAME", "app_db").withEnv("DB_USER", "app_user").withEnv("DB_PASSWORD", "app_password")
                    .withEnv("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")
                    .withEnv("SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI",
                            "http://keycloak:8080/realms/lab_realm/protocol/openid-connect/certs")
                    .withEnv("SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_KEYCLOAK_TOKEN_URI",
                            "http://keycloak:8080/realms/lab_realm/protocol/openid-connect/token")
                    .waitingFor(Wait.forLogMessage(".*Started.*in \\d+.*\\n", 1))
                    .dependsOn(postgresOrder, keycloak, storageService);

    private final String orderServiceClient = "Order_API";
    private final String storageServiceClient = "Storage_API";
    private final ObjectMapper objectMapper = new ObjectMapper();

    private JdbcTemplate orderJdbc;
    private JdbcTemplate storageJdbc;

    private String orderServiceUrl() {
        return "http://localhost:" + orderService.getMappedPort(8080);
    }

    private String storageServiceUrl() {
        return "http://localhost:" + storageService.getMappedPort(8080);
    }

    private static String obtainAccessToken(String username, String password, String clientId, String clientSecret) {
        return RestAssured.given()
                .baseUri(keycloak.getAuthServerUrl())
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("client_id", clientId)
                .formParam("client_secret", clientSecret)
                .formParam("username", username)
                .formParam("password", password)
                .post("/realms/lab_realm/protocol/openid-connect/token")
                .then().extract().path("access_token");
    }

    private void enableDirectAccess(String clientId) {
        var realm = keycloak.getKeycloakAdminClient().realm("lab_realm");
        var clientRep = realm.clients().findByClientId(clientId).getFirst();
        var rep = realm.clients().get(clientRep.getId()).toRepresentation();
        rep.setDirectAccessGrantsEnabled(true);
        realm.clients().get(clientRep.getId()).update(rep);
    }

    @BeforeEach
    void resetDatabases() throws Exception {
        orderJdbc = new JdbcTemplate(new DriverManagerDataSource(
                postgresOrder.getJdbcUrl(), postgresOrder.getUsername(), postgresOrder.getPassword()));
        storageJdbc = new JdbcTemplate(new DriverManagerDataSource(
                postgresStorage.getJdbcUrl(), postgresStorage.getUsername(), postgresStorage.getPassword()));

        resetAndMigrate(postgresOrder.getJdbcUrl(), postgresOrder.getUsername(), postgresOrder.getPassword(),
                projectRoot.resolve("orderService/src/main/resources"), "db/changelog/db.changelog-master.yaml");
        resetAndMigrate(postgresStorage.getJdbcUrl(), postgresStorage.getUsername(), postgresStorage.getPassword(),
                projectRoot.resolve("storageService/src/main/resources"), "db/changelog/db.changelog-master.yaml");
    }

    private void resetAndMigrate(String url, String user, String pass, Path root, String changelog) throws Exception {
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            Database db = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));
            ResourceAccessor accessor = new FileSystemResourceAccessor(root.toFile());
            Liquibase liq = new Liquibase(changelog, accessor, db);
            liq.dropAll();
            liq.update(new Contexts(), new LabelExpression());
        }
    }

    @AfterAll
    static void stopAll() {
        storageService.stop();
        orderService.stop();
        kafka.stop();
        keycloak.stop();
        postgresOrder.stop();
        postgresStorage.stop();
        postgresKeycloak.stop();
    }

    //  Tests 

    @Test
    void getAvailableCars_noParam_callsGrpcGetAllCarsAndReturnsJson() throws Exception {
        // Arrange
        enableDirectAccess(orderServiceClient);
        String token = obtainAccessToken("service", "service", orderServiceClient, orderServiceSecret);
        assertNotNull(token);

        // Act
        String body = RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + token)
                .get("/order/avaliables")
                .then().statusCode(200)
                .extract().asString();

        // Assert
        JsonNode json = objectMapper.readTree(body);
        assertTrue(json.isArray(), "Response should be a JSON array");
    }

    @Test
    void getAvailableCars_withCarId_callsGrpcGetByIdAndReturnsSingleCar() throws Exception {
        // Arrange
        enableDirectAccess(orderServiceClient);
        enableDirectAccess(storageServiceClient);

        String orderToken = obtainAccessToken("service", "service", orderServiceClient, orderServiceSecret);
        String storageToken = obtainAccessToken("storage", "storage", storageServiceClient, storageServiceSecret);

        String carsBody = RestAssured.given()
                .baseUri(storageServiceUrl())
                .header("Authorization", "Bearer " + storageToken)
                .get("/cars/all")
                .then().statusCode(200)
                .extract().asString();

        JsonNode cars = objectMapper.readTree(carsBody);
        assumeTrue(cars.has("models") && !cars.get("models").isEmpty(), "Requires seeded cars");
        String carId = cars.get("models").get(0).get("id").asText();

        // Act
        String body = RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + orderToken)
                .get("/order/avaliables/" + carId)
                .then().statusCode(200)
                .extract().asString();

        // Assert
        JsonNode result = objectMapper.readTree(body);
        assertTrue(result.isArray());
        assertEquals(1, result.size());
        assertEquals(carId, result.get(0).get("id").asText());
    }

    @Test
    void getAvailableCars_withoutToken_returns401() {
        // Act & Assert
        RestAssured.given()
                .baseUri(orderServiceUrl())
                .get("/order/avaliables")
                .then().statusCode(401);
    }

    @Test
    void createModelOrder_propagatesTokenViaGrpcToStorage_returns201() throws Exception {
        // Arrange
        enableDirectAccess(orderServiceClient);
        String token = obtainAccessToken("service", "service", orderServiceClient, orderServiceSecret);
        assertNotNull(token);

        // Act
        String body = RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
                        {
                          "modelName": "bmw320i",
                          "color": "Alpine White"
                        }
                        """)
                .post("/order/model")
                .then().statusCode(201)
                .extract().asString();

        // Assert
        JsonNode json = objectMapper.readTree(body);
        assertNotNull(json.get("orderId"));

        UUID orderId = UUID.fromString(json.get("orderId").asText());
        Integer count = orderJdbc.queryForObject(
                "SELECT COUNT(*) FROM car_orders WHERE id = ?", Integer.class, orderId);
        assertEquals(1, count);
    }

    @Test
    void moveOrderForward_fullOrderLifecycle_reachesManagerAgreedState() throws Exception {
        // Arrange
        enableDirectAccess(orderServiceClient);
        String token = obtainAccessToken("service", "service", orderServiceClient, orderServiceSecret);

        String createBody = RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
                        {"modelName": "bmw320i", "color": "Alpine White"}
                        """)
                .post("/order/model")
                .then().statusCode(201).extract().asString();

        String orderId = objectMapper.readTree(createBody).get("orderId").asText();

        // Act
        String moveBody = RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
                        {"orderId": "%s"}
                        """.formatted(orderId))
                .put("/order/moveOn")
                .then().statusCode(200).extract().asString();

        // Assert
        assertEquals("ManagerAgreed", objectMapper.readTree(moveBody).get("status").asText());
    }

    @Test
    void moveOrderForward_completesFullPaymentLifecycle() throws Exception {
        // Arrange
        enableDirectAccess(orderServiceClient);
        String token = obtainAccessToken("service", "service", orderServiceClient, orderServiceSecret);

        String createBody = RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
                        {"modelName": "bmw320i", "color": "Alpine White"}
                        """)
                .post("/order/model")
                .then().statusCode(201).extract().asString();
        String orderId = objectMapper.readTree(createBody).get("orderId").asText();

        // Act & Assert - advance through Registered → ManagerAgreed → WaitPayment → Paid
        String body1 = RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"orderId\": \"%s\"}".formatted(orderId))
                .put("/order/moveOn")
                .then().statusCode(200).extract().asString();
        assertEquals("ManagerAgreed", objectMapper.readTree(body1).get("status").asText());

        String body2 = RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"orderId\": \"%s\"}".formatted(orderId))
                .put("/order/moveOn")
                .then().statusCode(200).extract().asString();
        assertEquals("WaitPayment", objectMapper.readTree(body2).get("status").asText());

        String body3 = RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"orderId\": \"%s\"}".formatted(orderId))
                .put("/order/moveOn")
                .then().statusCode(200).extract().asString();
        assertEquals("Paid", objectMapper.readTree(body3).get("status").asText());

        await().pollDelay(5, TimeUnit.SECONDS).until(() -> true);
    }

    @Test
    void cancelOrder_afterCreation_orderIsCancelled() throws Exception {
        // Arrange
        enableDirectAccess(orderServiceClient);
        String token = obtainAccessToken("service", "service", orderServiceClient, orderServiceSecret);

        String createBody = RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
                        {"modelName": "bmw320i", "color": "Alpine White"}
                        """)
                .post("/order/model")
                .then().statusCode(201).extract().asString();

        String orderId = objectMapper.readTree(createBody).get("orderId").asText();

        // Act & Assert
        RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
                        {"orderId": "%s"}
                        """.formatted(orderId))
                .patch("/order/cansel")
                .then().statusCode(200);
    }

    @Test
    void getAllOrders_afterCreatingOne_returnsNonEmptyList() throws Exception {
        // Arrange
        enableDirectAccess(orderServiceClient);
        String token = obtainAccessToken("service", "service", orderServiceClient, orderServiceSecret);

        RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
                        {"modelName": "bmw320i", "color": "Alpine White"}
                        """)
                .post("/order/model")
                .then().statusCode(201);

        // Act
        String body = RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + token)
                .get("/order/all")
                .then().statusCode(200).extract().asString();

        // Assert
        JsonNode json = objectMapper.readTree(body);
        assertNotNull(json.get("infoList"));
        assertFalse(json.get("infoList").isEmpty());
    }

    @Test
    void getCarParts_fromStorageService_returnsSeededParts() throws Exception {
        // Arrange
        enableDirectAccess(storageServiceClient);
        String token = obtainAccessToken("storage", "storage", storageServiceClient, storageServiceSecret);

        // Act
        String body = RestAssured.given()
                .baseUri(storageServiceUrl())
                .header("Authorization", "Bearer " + token)
                .get("/parts/all")
                .then().statusCode(200).extract().asString();

        // Assert
        JsonNode json = objectMapper.readTree(body);
        assertNotNull(json.get("data"));
        assertFalse(json.get("data").isEmpty());
    }

    @Test
    void getAllCarsFromStorage_returnsSeededCars() throws Exception {
        // Arrange
        enableDirectAccess(storageServiceClient);
        String token = obtainAccessToken("storage", "storage", storageServiceClient, storageServiceSecret);

        // Act
        String body = RestAssured.given()
                .baseUri(storageServiceUrl())
                .header("Authorization", "Bearer " + token)
                .get("/cars/all")
                .then().statusCode(200).extract().asString();

        // Assert
        JsonNode json = objectMapper.readTree(body);
        assertNotNull(json.get("models"));
    }

    @Test
    void addRudderPart_thenVerifyInPartsList() throws Exception {
        // Arrange
        enableDirectAccess(storageServiceClient);
        String token = obtainAccessToken("storage", "storage", storageServiceClient, storageServiceSecret);

        // Act
        String createBody = RestAssured.given()
                .baseUri(storageServiceUrl())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
                        {"name": "Sport Rudder", "diffPrice": 500.0, "compatibleModels": ["bmw320i"]}
                        """)
                .post("/parts/add_rudder")
                .then().statusCode(201).extract().asString();

        // Assert
        String partId = objectMapper.readTree(createBody).get("partId").asText();
        assertNotNull(partId);

        String listBody = RestAssured.given()
                .baseUri(storageServiceUrl())
                .header("Authorization", "Bearer " + token)
                .get("/parts/all")
                .then().statusCode(200).extract().asString();

        JsonNode parts = objectMapper.readTree(listBody).get("data");
        boolean found = false;
        for (JsonNode part : parts) {
            if (partId.equals(part.get("id").asText())) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Newly created rudder part should appear in /parts/all");
    }

    @Test
    void addEnginePart_returns201WithPartId() throws Exception {
        // Arrange
        enableDirectAccess(storageServiceClient);
        String token = obtainAccessToken("storage", "storage", storageServiceClient, storageServiceSecret);

        // Act
        String body = RestAssured.given()
                .baseUri(storageServiceUrl())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
                        {"name": "V8 Engine", "diffPrice": 12000.0, "compatibleModels": ["bmw320i"],
                         "power": 450.0, "volume": 4.0, "engineType": "Petrol"}
                        """)
                .post("/parts/add_engine")
                .then().statusCode(201).extract().asString();

        // Assert
        JsonNode json = objectMapper.readTree(body);
        assertNotNull(json.get("partId"));
        assertDoesNotThrow(() -> UUID.fromString(json.get("partId").asText()));
    }

    @Test
    void addNewCarModel_usingSeededParts_returns201() throws Exception {
        // Arrange
        enableDirectAccess(storageServiceClient);
        String token = obtainAccessToken("storage", "storage", storageServiceClient, storageServiceSecret);

        String partsBody = RestAssured.given()
                .baseUri(storageServiceUrl())
                .header("Authorization", "Bearer " + token)
                .get("/parts/all")
                .then().statusCode(200).extract().asString();

        JsonNode partsData = objectMapper.readTree(partsBody).get("data");
        String rudderId = null, wheelId = null, transmissionId = null, interiorId = null, engineId = null;
        for (JsonNode part : partsData) {
            String type = part.get("partType").asText();
            switch (type) {
                case "Rudder" -> { if (rudderId == null) rudderId = part.get("id").asText(); }
                case "Wheel" -> { if (wheelId == null) wheelId = part.get("id").asText(); }
                case "Transmission" -> { if (transmissionId == null) transmissionId = part.get("id").asText(); }
                case "Interior" -> { if (interiorId == null) interiorId = part.get("id").asText(); }
                case "Engine" -> { if (engineId == null) engineId = part.get("id").asText(); }
                default -> {}
            }
        }
        assumeTrue(rudderId != null && wheelId != null && transmissionId != null
                        && interiorId != null && engineId != null,
                "Requires seeded parts of each type");

        // Act
        String carBody = RestAssured.given()
                .baseUri(storageServiceUrl())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
                        {
                          "requiredParts": ["Rudder","Wheel","Transmission","Interior","Engine"],
                          "modelName": "TestCar",
                          "carCase": "Sedan",
                          "brandName": "TestBrand",
                          "color": "Red",
                          "initialPrice": 30000.0,
                          "wheeldrive": "Front",
                          "gearBoxType": "Automatic",
                          "rudderId": "%s",
                          "wheelId": "%s",
                          "transmissionId": "%s",
                          "interiorId": "%s",
                          "engineId": "%s"
                        }
                        """.formatted(rudderId, wheelId, transmissionId, interiorId, engineId))
                .post("/cars/new")
                .then().statusCode(201).extract().asString();

        // Assert
        JsonNode carJson = objectMapper.readTree(carBody);
        assertNotNull(carJson.get("carInfo"));
        assertNotNull(carJson.get("carInfo").get("id"));
        assertEquals("TestCar", carJson.get("carInfo").get("modelName").asText());
    }

    @Test
    void getCarsByBrandName_returnsSeededBmwCars() throws Exception {
        // Arrange
        enableDirectAccess(storageServiceClient);
        String token = obtainAccessToken("storage", "storage", storageServiceClient, storageServiceSecret);

        // Act
        String body = RestAssured.given()
                .baseUri(storageServiceUrl())
                .header("Authorization", "Bearer " + token)
                .queryParam("brandName", "BMW")
                .get("/cars/find_by")
                .then().statusCode(200).extract().asString();

        // Assert
        JsonNode json = objectMapper.readTree(body);
        assertNotNull(json.get("models"));
        assertFalse(json.get("models").isEmpty(), "Should return BMW cars from seeded data");
        for (JsonNode car : json.get("models")) {
            assertEquals("BMW", car.get("brandName").asText());
        }
    }

    @Test
    void getAllTestDriveTickets_afterSignUp_returnsNonEmpty() throws Exception {
        // Arrange
        enableDirectAccess(orderServiceClient);
        String managerToken = obtainAccessToken("service", "service", orderServiceClient, orderServiceSecret);

        enableDirectAccess(storageServiceClient);
        String storageToken = obtainAccessToken("storage", "storage", storageServiceClient, storageServiceSecret);

        String carsBody = RestAssured.given()
                .baseUri(storageServiceUrl())
                .header("Authorization", "Bearer " + storageToken)
                .get("/cars/all")
                .then().statusCode(200).extract().asString();

        JsonNode cars = objectMapper.readTree(carsBody);
        assumeTrue(cars.has("models") && !cars.get("models").isEmpty(), "Requires seeded cars");
        String carId = cars.get("models").get(0).get("id").asText();

        RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + managerToken)
                .contentType("application/json")
                .body("{\"carId\": \"%s\", \"userId\": \"%s\"}".formatted(carId, UUID.randomUUID()))
                .post("/test_drive/add_car")
                .then().statusCode(201);

        RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + managerToken)
                .contentType("application/json")
                .body("{\"userId\": \"%s\", \"modelId\": \"%s\"}".formatted(UUID.randomUUID(), carId))
                .post("/test_drive/sign_up")
                .then().statusCode(201);

        // Act
        String ticketsBody = RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + managerToken)
                .queryParam("id", UUID.randomUUID())
                .get("/test_drive/all_tickets")
                .then().statusCode(200).extract().asString();

        // Assert
        JsonNode tickets = objectMapper.readTree(ticketsBody);
        assertNotNull(tickets.get("ticketsInfo"));
        assertFalse(tickets.get("ticketsInfo").isEmpty());
    }

    @Test
    void createCustomOrder_withMinimalBody_returns201() throws Exception {
        // Arrange
        enableDirectAccess(orderServiceClient);
        String token = obtainAccessToken("service", "service", orderServiceClient, orderServiceSecret);

        // Act
        String body = RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
                        {"modelName": "bmw320i", "color": "Alpine White"}
                        """)
                .post("/order/custom")
                .then().statusCode(201).extract().asString();

        // Assert
        JsonNode json = objectMapper.readTree(body);
        assertNotNull(json.get("orderId"));
        assertDoesNotThrow(() -> UUID.fromString(json.get("orderId").asText()));
    }

    @Test
    void cancelOrder_calledTwice_secondCallReturnsError() throws Exception {
        // Arrange
        enableDirectAccess(orderServiceClient);
        String token = obtainAccessToken("service", "service", orderServiceClient, orderServiceSecret);

        String createBody = RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"modelName\": \"bmw320i\", \"color\": \"Alpine White\"}")
                .post("/order/model")
                .then().statusCode(201).extract().asString();

        String orderId = objectMapper.readTree(createBody).get("orderId").asText();

        RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"orderId\": \"%s\"}".formatted(orderId))
                .patch("/order/cansel")
                .then().statusCode(200);

        // Act & Assert
        int secondStatus = RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"orderId\": \"%s\"}".formatted(orderId))
                .patch("/order/cansel")
                .then().extract().statusCode();

        assertNotEquals(200, secondStatus, "Cancelling an already-cancelled order should return an error");
    }

    @Test
    void cancelOrder_withoutToken_returns401() {
        // Act & Assert
        RestAssured.given()
                .baseUri(orderServiceUrl())
                .contentType("application/json")
                .body("{\"orderId\": \"%s\"}".formatted(UUID.randomUUID()))
                .patch("/order/cansel")
                .then().statusCode(401);
    }

    @Test
    void addTestDriveCar_asManager_returnsTestDriveCarInfo() throws Exception {
        // Arrange
        enableDirectAccess(orderServiceClient);
        String managerToken = obtainAccessToken("service", "service", orderServiceClient, orderServiceSecret);

        enableDirectAccess(storageServiceClient);
        String storageToken = obtainAccessToken("storage", "storage", storageServiceClient, storageServiceSecret);

        String carsBody = RestAssured.given()
                .baseUri(storageServiceUrl())
                .header("Authorization", "Bearer " + storageToken)
                .get("/cars/all")
                .then().statusCode(200).extract().asString();

        JsonNode cars = objectMapper.readTree(carsBody);
        assumeTrue(cars.has("models") && !cars.get("models").isEmpty(), "Requires seeded cars");
        String carId = cars.get("models").get(0).get("id").asText();

        // Act
        String addBody = RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + managerToken)
                .contentType("application/json")
                .body("{\"carId\": \"%s\", \"userId\": \"%s\"}".formatted(carId, UUID.randomUUID()))
                .post("/test_drive/add_car")
                .then().statusCode(201).extract().asString();

        // Assert
        JsonNode addJson = objectMapper.readTree(addBody);
        assertNotNull(addJson.get("testDriveCarInfo"));
        assertNotNull(addJson.get("testDriveCarInfo").get("id"));
        assertNotNull(addJson.get("testDriveCarInfo").get("modelId"));
        assertEquals(carId, addJson.get("testDriveCarInfo").get("modelId").asText());
    }

    @Test
    void moveOrderForward_afterCancellation_returnsError() throws Exception {
        // Arrange
        enableDirectAccess(orderServiceClient);
        String token = obtainAccessToken("service", "service", orderServiceClient, orderServiceSecret);

        String createBody = RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"modelName\": \"bmw320i\", \"color\": \"Alpine White\"}")
                .post("/order/model")
                .then().statusCode(201).extract().asString();

        String orderId = objectMapper.readTree(createBody).get("orderId").asText();

        RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"orderId\": \"%s\"}".formatted(orderId))
                .patch("/order/cansel")
                .then().statusCode(200);

        // Act & Assert
        int moveStatus = RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"orderId\": \"%s\"}".formatted(orderId))
                .put("/order/moveOn")
                .then().extract().statusCode();

        assertNotEquals(200, moveStatus, "Moving forward a cancelled order should return an error");
    }

    @Test
    void addTestDriveCar_andSignUp_returnsTicket() throws Exception {
        // Arrange
        enableDirectAccess(orderServiceClient);
        String managerToken = obtainAccessToken("service", "service", orderServiceClient, orderServiceSecret);

        enableDirectAccess(storageServiceClient);
        String storageToken = obtainAccessToken("storage", "storage", storageServiceClient, storageServiceSecret);
        String carsBody = RestAssured.given()
                .baseUri(storageServiceUrl())
                .header("Authorization", "Bearer " + storageToken)
                .get("/cars/all")
                .then().statusCode(200).extract().asString();

        JsonNode cars = objectMapper.readTree(carsBody);
        assumeTrue(cars.has("models") && !cars.get("models").isEmpty(), "Requires seeded cars");
        String carId = cars.get("models").get(0).get("id").asText();

        UUID userId = UUID.randomUUID();

        RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + managerToken)
                .contentType("application/json")
                .body("""
                        {"carId": "%s", "userId": "%s"}
                        """.formatted(carId, userId))
                .post("/test_drive/add_car")
                .then().statusCode(201);

        // Act
        String ticketBody = RestAssured.given()
                .baseUri(orderServiceUrl())
                .header("Authorization", "Bearer " + managerToken)
                .contentType("application/json")
                .body("""
                        {"userId": "%s", "modelId": "%s"}
                        """.formatted(userId, carId))
                .post("/test_drive/sign_up")
                .then().statusCode(201).extract().asString();

        // Assert
        JsonNode ticket = objectMapper.readTree(ticketBody);
        assertNotNull(ticket.get("ticketInfo"));
        assertNotNull(ticket.get("ticketInfo").get("id"));
    }
}
