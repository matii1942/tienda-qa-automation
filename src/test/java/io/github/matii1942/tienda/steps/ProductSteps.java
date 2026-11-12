package io.github.matii1942.tienda.steps;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.matii1942.tienda.api.StoreApi;
import io.github.matii1942.tienda.support.Customer;
import io.github.matii1942.tienda.support.TestContext;
import io.restassured.response.Response;
import java.math.BigDecimal;
import java.util.Map;

/** Test products: created through the API with a known price and stock, removed afterwards. */
public class ProductSteps {

    private final TestContext context;

    public ProductSteps(TestContext context) {
        this.context = context;
    }

    @Given("a test product priced {bigdecimal} with {int} unit(s) in stock")
    public void aTestProductPricedWithUnitsInStock(BigDecimal price, int stock) {
        String name = "qa product " + Customer.uniqueSuffix();
        context.testProduct = context.createTestProduct(name, price, stock, "DISPONIBLE");
    }

    @Given("a product whose name contains the HTML {string}")
    public void aProductWhoseNameContainsTheHtml(String html) {
        String name = html + " qa " + Customer.uniqueSuffix();
        context.testProduct = context.createTestProduct(name, new BigDecimal("1000"), 1, "DISPONIBLE");
    }

    @Given("the test product is marked as {string}")
    public void theTestProductIsMarkedAs(String status) {
        Response response = StoreApi.updateProduct(
                context.catalogueManager().token(), context.testProduct.id(), Map.of("status", status));
        assertThat(response.statusCode()).as("marking as %s: %s", status, response.asString()).isBetween(200, 299);
    }

    @When("I change the price of the test product to {bigdecimal}")
    public void iChangeThePriceOfTheTestProductTo(BigDecimal price) {
        context.remember(StoreApi.updateProduct(
                context.catalogueManager().token(), context.testProduct.id(), Map.of("price", price)));
    }

    @Then("the test product has {int} unit(s) in stock")
    public void theTestProductHasUnitsInStock(int expected) {
        Response response = StoreApi.productByName(context.testProduct.name());
        assertThat(response.statusCode()).as("reading the test product: %s", response.asString()).isEqualTo(200);
        assertThat(StoreApi.json(response).getInt("quantity")).as("stock").isEqualTo(expected);
    }
}
