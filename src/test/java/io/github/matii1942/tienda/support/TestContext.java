package io.github.matii1942.tienda.support;

import io.github.matii1942.tienda.api.StoreApi;
import io.restassured.response.Response;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.WebDriver;

/**
 * State shared by the step classes of one scenario.
 *
 * <p>PicoContainer creates a fresh instance per scenario and injects it into every step
 * class that asks for it in its constructor, so nothing leaks from one scenario to the next.
 */
public class TestContext {

    /** The last API answer, for the generic "the response status is ..." steps. */
    private Response lastResponse;

    /** The customer the scenario is acting as ("I"). */
    public Customer me;

    /** Named customers, for scenarios with more than one ("Alice", "Bob"). */
    public final Map<String, Customer> customers = new HashMap<>();

    /** The product the scenario buys, created through the API. */
    public TestProduct testProduct;

    /** Id of the last order placed successfully. */
    public String lastOrderId;

    /** Browser for @ui scenarios, opened and closed by {@link Hooks}. */
    public WebDriver driver;

    /** Everything to delete afterwards: id and the token allowed to delete it. */
    final List<String[]> productsToDelete = new ArrayList<>();
    final List<Customer> customersToDelete = new ArrayList<>();

    /** The account that creates test products. Registered on first use. */
    private Customer catalogueManager;

    public Response lastResponse() {
        if (lastResponse == null) {
            throw new IllegalStateException("No API call has been made in this scenario yet");
        }
        return lastResponse;
    }

    public Response remember(Response response) {
        this.lastResponse = response;
        return response;
    }

    public Customer catalogueManager() {
        if (catalogueManager == null) {
            catalogueManager = track(StoreApi.registerAndLogIn(Customer.named("Catalogue")));
        }
        return catalogueManager;
    }

    public Customer track(Customer customer) {
        customersToDelete.add(customer);
        return customer;
    }

    /** Remembers a product created by this scenario so the After hook can remove it. */
    public void trackProduct(String id, String token) {
        productsToDelete.add(new String[] {id, token});
    }

    /** Creates a product owned by the catalogue manager and schedules its deletion. */
    public TestProduct createTestProduct(String name, BigDecimal price, int stock, String status) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("price", price);
        body.put("quantity", stock);
        body.put("status", status);
        body.put("description", "Created by tienda-qa-automation");
        body.put("image", "./img/zapa1.webp");

        String token = catalogueManager().token();
        Response response = remember(StoreApi.createProduct(token, body));

        if (response.statusCode() != 200) {
            throw new AssertionError("Could not create test product '" + name + "': "
                    + response.statusCode() + " " + response.asString());
        }

        String id = StoreApi.json(response).getString("id");
        trackProduct(id, token);
        return new TestProduct(id, StoreApi.json(response).getString("name"), price, stock);
    }

    public Customer customer(String alias) {
        Customer customer = customers.get(alias);
        if (customer == null) {
            throw new IllegalArgumentException("No customer called '" + alias + "' in this scenario");
        }
        return customer;
    }
}
