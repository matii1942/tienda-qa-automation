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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderSteps {

    /** A well-formed Mongo id that no product has. */
    private static final String MISSING_PRODUCT_ID = "0123456789abcdef01234567";

    private final TestContext context;

    public OrderSteps(TestContext context) {
        this.context = context;
    }

    @When("I order {int} unit(s) of the test product")
    public void iOrderUnitsOfTheTestProduct(int quantity) {
        order(List.of(line(context.testProduct.id(), quantity)));
    }

    @Given("I have ordered {int} unit(s) of the test product")
    public void iHaveOrderedUnitsOfTheTestProduct(int quantity) {
        Response response = order(List.of(line(context.testProduct.id(), quantity)));
        assertThat(response.statusCode()).as("placing the order: %s", response.asString()).isEqualTo(201);
    }

    /** The store must ignore anything but id and quantity: these extra fields try to set the price. */
    @When("I order {int} unit(s) of the test product claiming a price of {bigdecimal}")
    public void iOrderClaimingAPriceOf(int quantity, BigDecimal fakePrice) {
        Map<String, Object> line = line(context.testProduct.id(), quantity);
        line.put("price", fakePrice);
        line.put("unitPrice", fakePrice);
        line.put("subtotal", fakePrice);

        Map<String, Object> body = new HashMap<>();
        body.put("items", List.of(line));
        body.put("total", fakePrice);

        context.remember(StoreApi.placeOrder(context.me.token(), body));
    }

    @When("I order the test product in two lines of {int} and {int} units")
    public void iOrderTheTestProductInTwoLines(int first, int second) {
        String id = context.testProduct.id();
        order(List.of(line(id, first), line(id, second)));
    }

    @When("I order {int} unit(s) of a product that does not exist")
    public void iOrderAProductThatDoesNotExist(int quantity) {
        order(List.of(line(MISSING_PRODUCT_ID, quantity)));
    }

    @When("I send an order with no items")
    public void iSendAnOrderWithNoItems() {
        order(List.of());
    }

    @When("I send an order with a quantity of 0")
    public void iSendAnOrderWithAQuantityOf0() {
        order(List.of(line(context.testProduct.id(), 0)));
    }

    @When("I send an order with a negative quantity")
    public void iSendAnOrderWithANegativeQuantity() {
        order(List.of(line(context.testProduct.id(), -2)));
    }

    @When("I send an order with a fractional quantity")
    public void iSendAnOrderWithAFractionalQuantity() {
        order(List.of(line(context.testProduct.id(), 1.5)));
    }

    @When("I send an order with more than 20 units per line")
    public void iSendAnOrderWithMoreThan20UnitsPerLine() {
        order(List.of(line(context.testProduct.id(), 21)));
    }

    @When("I send an order with a malformed product id")
    public void iSendAnOrderWithAMalformedProductId() {
        order(List.of(line("not-a-mongo-id", 1)));
    }

    @When("another customer lists their orders")
    public void anotherCustomerListsTheirOrders() {
        Customer other = context.track(StoreApi.registerAndLogIn(Customer.named("Other")));
        context.remember(StoreApi.myOrders(other.token()));
    }

    @When("I list my orders")
    public void iListMyOrders() {
        context.remember(StoreApi.myOrders(context.me.token()));
    }

    @Then("my order is in the list")
    public void myOrderIsInTheList() {
        assertThat(orderIds()).contains(context.lastOrderId);
    }

    @Then("my order is not in the list")
    public void myOrderIsNotInTheList() {
        assertThat(orderIds()).doesNotContain(context.lastOrderId);
    }

    @Then("the order total is {bigdecimal}")
    public void theOrderTotalIs(BigDecimal expected) {
        assertThat(StoreApi.decimal(context.lastResponse(), "total")).isEqualByComparingTo(expected);
    }

    @Then("the order has {int} line(s)")
    public void theOrderHasLines(int expected) {
        assertThat(StoreApi.json(context.lastResponse()).getList("items")).hasSize(expected);
    }

    @Then("the order line has unit price {bigdecimal}, quantity {int} and subtotal {bigdecimal}")
    public void theOrderLineHas(BigDecimal unitPrice, int quantity, BigDecimal subtotal) {
        Response response = context.lastResponse();
        assertThat(StoreApi.decimal(response, "items[0].unitPrice")).as("unit price").isEqualByComparingTo(unitPrice);
        assertThat(StoreApi.json(response).getInt("items[0].quantity")).as("quantity").isEqualTo(quantity);
        assertThat(StoreApi.decimal(response, "items[0].subtotal")).as("subtotal").isEqualByComparingTo(subtotal);
    }

    private Response order(List<Map<String, Object>> lines) {
        Response response = context.remember(StoreApi.placeOrder(context.me.token(), Map.of("items", lines)));

        if (response.statusCode() == 201) {
            context.lastOrderId = StoreApi.json(response).getString("id");
        }
        return response;
    }

    private static Map<String, Object> line(String productId, Number quantity) {
        Map<String, Object> line = new HashMap<>();
        line.put("productId", productId);
        line.put("quantity", quantity);
        return line;
    }

    private List<String> orderIds() {
        return StoreApi.json(context.lastResponse()).getList("id", String.class);
    }
}
