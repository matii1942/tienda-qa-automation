package io.github.matii1942.tienda.steps;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.matii1942.tienda.api.StoreApi;
import io.github.matii1942.tienda.pages.AccountPage;
import io.github.matii1942.tienda.pages.CartPage;
import io.github.matii1942.tienda.pages.StorefrontPage;
import io.github.matii1942.tienda.support.Customer;
import io.github.matii1942.tienda.support.Money;
import io.github.matii1942.tienda.support.TestContext;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

public class UiSteps {

    private final TestContext context;

    public UiSteps(TestContext context) {
        this.context = context;
    }

    private StorefrontPage storefront() {
        return new StorefrontPage(context.driver);
    }

    private CartPage cart() {
        return new CartPage(context.driver);
    }

    private AccountPage account() {
        return new AccountPage(context.driver);
    }

    // ------------------------------------------------------------ storefront

    @Given("I open the storefront")
    public void iOpenTheStorefront() {
        storefront().open();
    }

    @Given("I open the storefront with an empty cart")
    public void iOpenTheStorefrontWithAnEmptyCart() {
        storefront().open().clearCart();
    }

    @When("I reload the page")
    public void iReloadThePage() {
        context.driver.navigate().refresh();
    }

    @Then("I see at least {int} products")
    public void iSeeAtLeastProducts(int minimum) {
        assertThat(storefront().productNames()).hasSizeGreaterThanOrEqualTo(minimum);
    }

    @Then("I see {int} products")
    public void iSeeProducts(int expected) {
        assertThat(storefront().productNames()).hasSize(expected);
    }

    @Then("the result counter matches the number of products shown")
    public void theResultCounterMatches() {
        int shown = storefront().productNames().size();
        assertThat(storefront().resultCounter()).isEqualTo(shown == 1 ? "1 resultado" : shown + " resultados");
    }

    @Then("the result counter says {string}")
    public void theResultCounterSays(String expected) {
        assertThat(storefront().resultCounter()).isEqualTo(expected);
    }

    @When("I select the {string} category")
    public void iSelectTheCategory(String slug) {
        storefront().selectCategory(slug);
    }

    @Then("the page title is {string}")
    public void thePageTitleIs(String expected) {
        assertThat(storefront().title()).isEqualTo(expected);
    }

    @When("I search for {string}")
    public void iSearchFor(String text) {
        storefront().search(text);
    }

    @Then("every product shown starts with {string}")
    public void everyProductShownStartsWith(String prefix) {
        List<String> names = storefront().productNames();
        assertThat(names).isNotEmpty().allSatisfy(name -> assertThat(name).startsWithIgnoringCase(prefix));
    }

    @Then("the only product shown is {string}")
    public void theOnlyProductShownIs(String name) {
        assertThat(storefront().productNames()).containsExactly(name);
    }

    @Then("the grid shows the message {string}")
    public void theGridShowsTheMessage(String fragment) {
        assertThat(storefront().emptyMessage()).contains(fragment);
    }

    @Then("no script hidden in a product name has run")
    public void noScriptHiddenInAProductNameHasRun() {
        // The payload sets window.__xss instead of opening an alert, so the check is silent.
        assertThat(storefront().scriptFlagWasSet("__xss", Duration.ofSeconds(3)))
                .as("a product name was rendered as HTML and its onerror handler ran")
                .isFalse();
    }

    // ------------------------------------------------------------------ cart

    @When("I add {string} to the cart {int} time(s)")
    public void iAddToTheCart(String productName, int times) {
        storefront().addToCartByName(productName, times);
    }

    @When("I add the test product to the cart {int} time(s)")
    public void iAddTheTestProductToTheCart(int times) {
        storefront().addToCartById(context.testProduct.id(), times);
    }

    @Then("the cart badge shows {int}")
    public void theCartBadgeShows(int expected) {
        storefront().waitForCartBadge(expected);
    }

    @When("I open the cart")
    public void iOpenTheCart() {
        cart().open();
    }

    @Then("the cart has {int} line(s)")
    public void theCartHasLines(int expected) {
        assertThat(cart().lines()).hasSize(expected);
    }

    @Then("the cart contains the test product with quantity {int}")
    public void theCartContainsTheTestProductWithQuantity(int quantity) {
        assertThat(cart().lines())
                .contains(new CartPage.Line(context.testProduct.id(), context.testProduct.name(), quantity));
    }

    @Then("the cart total is {bigdecimal}")
    public void theCartTotalIs(BigDecimal total) {
        assertThat(cart().total()).isEqualTo(Money.pesos(total));
    }

    @When("I remove the test product from the cart")
    public void iRemoveTheTestProductFromTheCart() {
        cart().remove(context.testProduct.id());
    }

    @When("I empty the cart")
    public void iEmptyTheCart() {
        cart().empty();
    }

    @Then("the cart shows that it is empty")
    public void theCartShowsThatItIsEmpty() {
        assertThat(cart().isEmptyStateShown()).isTrue();
        assertThat(cart().lines()).isEmpty();
    }

    // -------------------------------------------------------------- checkout

    @When("I click Buy now")
    public void iClickBuyNow() {
        cart().buy();
    }

    @Then("I am sent to the account page")
    public void iAmSentToTheAccountPage() {
        account().waitForUrlContaining("cuenta.html?volver=carrito");
    }

    @Then("I am taken back to the cart")
    public void iAmTakenBackToTheCart() {
        cart().waitForUrlContaining("carrito.html");
        cart().waitUntilLoaded();
    }

    @When("I create an account from the account page")
    public void iCreateAnAccountFromTheAccountPage() {
        context.me = Customer.random();
        account().register(context.me);
    }

    @Given("I am logged in through the account page")
    public void iAmLoggedInThroughTheAccountPage() {
        context.me = context.track(StoreApi.registerAndLogIn(Customer.random()));
        account().open().logIn(context.me.email(), context.me.password());
        account().waitForUrlContaining("index.html");
    }

    @Then("I see the purchase confirmation with the total {bigdecimal}")
    public void iSeeThePurchaseConfirmationWithTheTotal(BigDecimal total) {
        assertThat(cart().confirmation())
                .contains("Gracias por tu compra")
                .contains(Money.pesos(total));
    }

    @Then("the cart shows the warning {string}")
    public void theCartShowsTheWarning(String fragment) {
        assertThat(cart().warning()).contains(fragment);
    }
}
