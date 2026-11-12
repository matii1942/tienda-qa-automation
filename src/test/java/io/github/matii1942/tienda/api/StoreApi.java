package io.github.matii1942.tienda.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.matii1942.tienda.config.Config;
import io.github.matii1942.tienda.support.Customer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.path.json.config.JsonPathConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.math.BigDecimal;
import java.util.Map;

/**
 * A thin client for the store's REST API.
 *
 * <p>Methods return the raw {@link Response}: the step definitions decide what to assert,
 * because the same call is a happy path in one scenario and a defect in another.
 */
public final class StoreApi {

    /** Money is compared as BigDecimal: 3899.97 must be exactly 3899.97, not 3899.9700000000003. */
    private static final JsonPathConfig EXACT_NUMBERS =
            JsonPathConfig.jsonPathConfig().numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL);

    private StoreApi() {
    }

    /** Parses a response body keeping decimal numbers exact. */
    public static JsonPath json(Response response) {
        return response.jsonPath(EXACT_NUMBERS);
    }

    public static BigDecimal decimal(Response response, String path) {
        return new BigDecimal(json(response).getString(path));
    }

    public static RequestSpecification request() {
        return RestAssured.given()
                .baseUri(Config.baseUrl())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

    public static RequestSpecification authenticated(String token) {
        return request().header("Authorization", "Bearer " + token);
    }

    // ---------------------------------------------------------------- public

    public static Response health() {
        return request().get("/api/health");
    }

    public static Response products() {
        return request().get("/api/products");
    }

    public static Response productByName(String name) {
        return request().pathParam("name", name).get("/api/products/{name}");
    }

    public static Response categories() {
        return request().get("/api/categories");
    }

    public static Response register(Customer customer) {
        return request().body(customer.registrationBody()).post("/api/users/create");
    }

    public static Response login(String email, String password) {
        return request().body(Map.of("email", email, "password", password)).post("/api/users/login");
    }

    // ------------------------------------------------------- authenticated

    public static Response createProduct(String token, Map<String, Object> product) {
        return authenticated(token).body(product).post("/api/products");
    }

    public static Response updateProduct(String token, String id, Map<String, Object> changes) {
        return authenticated(token).pathParam("id", id).body(changes).put("/api/products/{id}");
    }

    public static Response deleteProduct(String token, String id) {
        return authenticated(token).pathParam("id", id).delete("/api/products/{id}");
    }

    public static Response placeOrder(String token, Object body) {
        return authenticated(token).body(body).post("/api/orders");
    }

    public static Response myOrders(String token) {
        return authenticated(token).get("/api/orders");
    }

    public static Response allUsers(String token) {
        return authenticated(token).get("/api/users/getAll");
    }

    public static Response updateUser(String token, String id, Map<String, Object> changes) {
        return authenticated(token).pathParam("id", id).body(changes).put("/api/users/update/{id}");
    }

    public static Response deleteUser(String token, String id) {
        return authenticated(token).pathParam("id", id).delete("/api/users/destroyed/{id}");
    }

    /** Any method and path, with an optional raw Authorization header. */
    public static Response call(String method, String path, String authorizationHeader) {
        RequestSpecification spec = request();

        if (authorizationHeader != null) {
            spec = spec.header("Authorization", authorizationHeader);
        }

        if (!method.equalsIgnoreCase("GET") && !method.equalsIgnoreCase("DELETE")) {
            spec = spec.body("{}");
        }

        return spec.request(method.toUpperCase(), path);
    }

    // -------------------------------------------------------------- fixtures

    /** Registers and logs in, failing the scenario with the API's answer if either step fails. */
    public static Customer registerAndLogIn(Customer customer) {
        Response registration = register(customer);
        assertThat(registration.statusCode())
                .as("registering %s: %s", customer, registration.asString())
                .isEqualTo(200);
        customer.setId(json(registration).getString("_id"));

        Response login = login(customer.email(), customer.password());
        assertThat(login.statusCode())
                .as("logging in as %s: %s", customer, login.asString())
                .isEqualTo(200);
        customer.setToken(json(login).getString("token"));

        return customer;
    }
}
