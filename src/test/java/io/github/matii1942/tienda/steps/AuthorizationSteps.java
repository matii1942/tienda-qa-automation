package io.github.matii1942.tienda.steps;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.matii1942.tienda.api.StoreApi;
import io.github.matii1942.tienda.support.Customer;
import io.github.matii1942.tienda.support.TestContext;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;

/** Scenarios with two named customers acting on each other's data. */
public class AuthorizationSteps {

    private final TestContext context;

    public AuthorizationSteps(TestContext context) {
        this.context = context;
    }

    @Given("the customers {string} and {string} are registered and logged in")
    public void theCustomersAreRegisteredAndLoggedIn(String first, String second) {
        for (String alias : new String[] {first, second}) {
            context.customers.put(alias, context.track(StoreApi.registerAndLogIn(Customer.named(alias))));
        }
        // "I" in shared steps is the first customer.
        context.me = context.customer(first);
    }

    @When("{string} deletes the account of {string}")
    public void deletesTheAccountOf(String actor, String target) {
        context.remember(StoreApi.deleteUser(
                context.customer(actor).token(), context.customer(target).id()));
    }

    @When("{string} changes the name of {string} to {string}")
    public void changesTheNameOf(String actor, String target, String newName) {
        context.remember(StoreApi.updateUser(
                context.customer(actor).token(), context.customer(target).id(), Map.of("nombre", newName)));
    }

    @When("{string} requests the list of all users")
    public void requestsTheListOfAllUsers(String actor) {
        context.remember(StoreApi.allUsers(context.customer(actor).token()));
    }

    @When("{string} changes her/his own password to {string}")
    public void changesTheirOwnPasswordTo(String actor, String newPassword) {
        Customer customer = context.customer(actor);
        context.remember(StoreApi.updateUser(customer.token(), customer.id(), Map.of("password", newPassword)));
    }

    @When("{string} creates a product")
    public void createsAProduct(String actor) {
        Customer customer = context.customer(actor);

        Map<String, Object> product = new HashMap<>();
        product.put("name", "qa product by customer " + Customer.uniqueSuffix());
        product.put("price", 1);
        product.put("quantity", 1);
        product.put("status", "DISPONIBLE");

        Response response = context.remember(StoreApi.createProduct(customer.token(), product));

        // If the store lets it through (the defect), remove it afterwards.
        if (response.statusCode() == 200) {
            context.trackProduct(StoreApi.json(response).getString("id"), customer.token());
        }
    }

    @When("{string} deletes the product with id {string}")
    public void deletesTheProductWithId(String actor, String id) {
        context.remember(StoreApi.deleteProduct(context.customer(actor).token(), id));
    }

    @Then("{string} can still log in")
    public void canStillLogIn(String alias) {
        Customer customer = context.customer(alias);
        assertLogsIn(customer, customer.password());
    }

    @Then("{string} can log in with the password {string}")
    public void canLogInWithThePassword(String alias, String password) {
        assertLogsIn(context.customer(alias), password);
    }

    private static void assertLogsIn(Customer customer, String password) {
        Response login = StoreApi.login(customer.email(), password);
        assertThat(login.statusCode())
                .as("%s logging in: %s", customer, login.asString())
                .isEqualTo(200);
    }
}
