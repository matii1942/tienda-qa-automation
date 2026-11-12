package io.github.matii1942.tienda.steps;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.matii1942.tienda.api.StoreApi;
import io.github.matii1942.tienda.support.Customer;
import io.github.matii1942.tienda.support.TestContext;
import io.restassured.response.Response;

public class AuthenticationSteps {

    private final TestContext context;

    public AuthenticationSteps(TestContext context) {
        this.context = context;
    }

    @Given("I am logged in as a new customer")
    public void iAmLoggedInAsANewCustomer() {
        context.me = context.track(StoreApi.registerAndLogIn(Customer.random()));
    }

    @Given("a new customer has registered")
    public void aNewCustomerHasRegistered() {
        context.me = Customer.random();
        Response response = register(context.me);
        assertThat(response.statusCode()).as("registration: %s", response.asString()).isEqualTo(200);
    }

    @When("a new customer registers")
    public void aNewCustomerRegisters() {
        context.me = Customer.random();
        register(context.me);
    }

    @When("a new customer registers with the password {string}")
    public void aNewCustomerRegistersWithThePassword(String password) {
        context.me = Customer.random().withPassword(password);
        register(context.me);
    }

    @When("a new customer registers without a password")
    public void aNewCustomerRegistersWithoutAPassword() {
        context.me = Customer.random().withPassword(null);
        register(context.me);
    }

    @When("another customer registers with the same email")
    public void anotherCustomerRegistersWithTheSameEmail() {
        register(Customer.withSameEmailAs(context.me));
    }

    @When("the customer logs in with the correct password")
    public void theCustomerLogsInWithTheCorrectPassword() {
        logIn(context.me.email(), context.me.password());
    }

    @When("the customer logs in with the password {string}")
    public void theCustomerLogsInWithThePassword(String password) {
        logIn(context.me.email(), password);
    }

    @When("someone logs in as {string} with the password {string}")
    public void someoneLogsInAs(String email, String password) {
        context.remember(StoreApi.login(email, password));
    }

    @When("I call {string} {string} without a token")
    public void iCallWithoutAToken(String method, String path) {
        context.remember(StoreApi.call(method, path, null));
    }

    /** An empty cell in the examples table means "send no Authorization header at all". */
    @When("I call {string} {string} with the authorization header {string}")
    public void iCallWithTheAuthorizationHeader(String method, String path, String header) {
        context.remember(StoreApi.call(method, path, header.isBlank() ? null : header));
    }

    @Then("the response contains a session token")
    public void theResponseContainsASessionToken() {
        String token = StoreApi.json(context.lastResponse()).getString("token");
        assertThat(token).as("token").isNotBlank();
        assertThat(token.split("\\.")).as("a JWT has header, payload and signature").hasSize(3);
    }

    @Then("the response does not contain a session token")
    public void theResponseDoesNotContainASessionToken() {
        assertThat(StoreApi.json(context.lastResponse()).getString("token")).isNull();
    }

    /** Registers and, if the store accepted it, schedules the account for deletion. */
    private Response register(Customer customer) {
        Response response = context.remember(StoreApi.register(customer));

        if (response.statusCode() == 200) {
            customer.setId(StoreApi.json(response).getString("_id"));
            context.track(customer);

            // A token is needed to delete the account afterwards. Accounts created
            // without a valid password (a defect) cannot log in and are left behind.
            if (customer.password() != null) {
                Response login = StoreApi.login(customer.email(), customer.password());
                if (login.statusCode() == 200) {
                    customer.setToken(StoreApi.json(login).getString("token"));
                }
            }
        }
        return response;
    }

    private void logIn(String email, String password) {
        context.remember(StoreApi.login(email, password));
    }
}
