package io.github.matii1942.tienda.pages;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/** index.html: the catalogue grid, the category buttons and the search box. */
public class StorefrontPage extends BasePage {

    private static final By PRODUCT_CARD = By.cssSelector("#contenedor-productos .product");
    private static final By PRODUCT_TITLE = By.cssSelector(".title-product");
    private static final By ADD_BUTTON = By.cssSelector(".button-add");
    private static final By RESULT_COUNTER = By.id("resultado-conteo");
    private static final By EMPTY_GRID = By.cssSelector("#contenedor-productos .grilla-vacia");
    private static final By TITLE = By.id("titulo-principal");
    private static final By SEARCH = By.id("buscador");
    private static final By CART_LINK = By.cssSelector("a.accion-carrito");

    private static final String CART_STORAGE_KEY = "productosEnCarrito";

    public StorefrontPage(WebDriver driver) {
        super(driver);
    }

    public StorefrontPage open() {
        openPath("/index.html");
        waitForCatalogue();
        return this;
    }

    /** Removes the cart from localStorage, keeping the session, and reloads. */
    public StorefrontPage clearCart() {
        js("localStorage.removeItem(arguments[0]);", CART_STORAGE_KEY);
        driver.navigate().refresh();
        waitForCatalogue();
        return this;
    }

    /** The grid is drawn after /api/products answers: wait for the counter or the empty message. */
    private void waitForCatalogue() {
        wait.until(d -> !d.findElement(RESULT_COUNTER).getText().isBlank()
                || !d.findElements(EMPTY_GRID).isEmpty());
    }

    public List<String> productNames() {
        return driver.findElements(PRODUCT_CARD).stream()
                .map(StorefrontPage::nameOf)
                .toList();
    }

    /**
     * The CSS capitalizes titles ("zapatilla 01" is shown as "Zapatilla 01") and getText()
     * returns the rendered text. textContent is the name the API sent, which is what the
     * scenarios talk about.
     */
    private static String nameOf(WebElement card) {
        return card.findElement(PRODUCT_TITLE).getDomProperty("textContent").trim();
    }

    public String resultCounter() {
        return driver.findElement(RESULT_COUNTER).getText().trim();
    }

    public String title() {
        return driver.findElement(TITLE).getDomProperty("textContent").trim();
    }

    public String emptyMessage() {
        return driver.findElement(EMPTY_GRID).getText().trim();
    }

    /** Category buttons use the category slug as id: "zapatillas", "remeras", "gorras". */
    public StorefrontPage selectCategory(String slug) {
        driver.findElement(By.id(slug)).click();
        return this;
    }

    public StorefrontPage search(String text) {
        WebElement box = driver.findElement(SEARCH);
        box.clear();
        box.sendKeys(text);
        return this;
    }

    public StorefrontPage addToCartByName(String productName, int times) {
        WebElement card = driver.findElements(PRODUCT_CARD).stream()
                .filter(c -> nameOf(c).equalsIgnoreCase(productName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No product called '" + productName + "' in the grid"));

        clickTimes(card.findElement(ADD_BUTTON), times);
        return this;
    }

    /**
     * The add button's id is the product's Mongo id, which can start with a digit. "#6650..."
     * is not a valid CSS selector, so match the attribute instead of using By.id.
     */
    public StorefrontPage addToCartById(String productId, int times) {
        WebElement button = driver.findElement(By.cssSelector("button.button-add[id='" + productId + "']"));
        clickTimes(button, times);
        return this;
    }

    private void clickTimes(WebElement button, int times) {
        int before = cartBadge();
        for (int i = 0; i < times; i++) {
            button.click();
        }
        waitForCartBadge(before + times);
    }

    public CartPage goToCart() {
        driver.findElement(CART_LINK).click();
        waitForUrlContaining("carrito.html");
        return new CartPage(driver).waitUntilLoaded();
    }

    /**
     * True if a script planted in the page set {@code window[flag]} within the given time.
     * Used by the stored-XSS check: the payload sets a flag instead of opening an alert.
     */
    public boolean scriptFlagWasSet(String flag, Duration within) {
        try {
            new WebDriverWait(driver, within)
                    .until(d -> Boolean.TRUE.equals(js("return window[arguments[0]] === 1;", flag)));
            return true;
        } catch (TimeoutException notSet) {
            return false;
        }
    }
}
