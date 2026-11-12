package io.github.matii1942.tienda.pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** carrito.html: the cart lines, the total, and the buy / empty buttons. */
public class CartPage extends BasePage {

    private static final By LINES = By.id("carrito-productos");
    private static final By LINE = By.cssSelector("#carrito-productos .carrito-producto");
    private static final By LINE_NAME = By.cssSelector(".carrito-producto-titulo h3");
    private static final By LINE_QUANTITY = By.cssSelector(".carrito-producto-cantidad p");
    private static final By LINE_REMOVE = By.cssSelector(".carrito-producto-eliminar");
    private static final By EMPTY_STATE = By.id("carrito-vacio");
    private static final By SUMMARY = By.id("carrito-acciones");
    private static final By TOTAL = By.id("total");
    private static final By BUY = By.id("carrito-acciones-comprar");
    private static final By EMPTY_CART = By.id("carrito-acciones-vaciar");
    private static final By CONFIRMATION = By.id("carrito-comprado");
    private static final By WARNING = By.id("carrito-aviso");

    public record Line(String productId, String name, int quantity) {
    }

    public CartPage(WebDriver driver) {
        super(driver);
    }

    public CartPage open() {
        openPath("/carrito.html");
        return waitUntilLoaded();
    }

    /**
     * The "empty cart" message is visible in the HTML before any script runs, so it cannot
     * tell us the page is ready. If the browser has items saved, wait until the cart has
     * fetched the prices and drawn the summary (or shown a warning).
     */
    public CartPage waitUntilLoaded() {
        Object saved = js("return JSON.parse(localStorage.getItem('productosEnCarrito') || '[]').length;");
        boolean hasItems = saved instanceof Number n && n.intValue() > 0;

        if (hasItems) {
            wait.until(d -> isShown(SUMMARY) || isShown(WARNING));
        }
        return this;
    }

    /**
     * The lines the customer can see. When the cart is emptied the store hides the list
     * but leaves the old rows in the HTML, so a hidden list counts as no lines.
     */
    public List<Line> lines() {
        if (!isShown(LINES)) {
            return List.of();
        }
        return driver.findElements(LINE).stream().map(CartPage::toLine).toList();
    }

    /** textContent, not getText(): the CSS capitalizes the name on screen. */
    private static Line toLine(WebElement row) {
        return new Line(
                row.findElement(LINE_REMOVE).getDomAttribute("id"),
                row.findElement(LINE_NAME).getDomProperty("textContent").trim(),
                Integer.parseInt(row.findElement(LINE_QUANTITY).getDomProperty("textContent").trim()));
    }

    public String total() {
        return driver.findElement(TOTAL).getText().trim();
    }

    public boolean isEmptyStateShown() {
        return isShown(EMPTY_STATE);
    }

    public CartPage remove(String productId) {
        driver.findElement(By.cssSelector(".carrito-producto-eliminar[id='" + productId + "']")).click();
        return this;
    }

    public CartPage empty() {
        driver.findElement(EMPTY_CART).click();
        wait.until(d -> isShown(EMPTY_STATE));
        return this;
    }

    public void buy() {
        driver.findElement(BUY).click();
    }

    public String confirmation() {
        return waitUntilShown(CONFIRMATION).getText().trim();
    }

    public String warning() {
        wait.until(d -> isShown(WARNING) && !d.findElement(WARNING).getText().isBlank());
        return driver.findElement(WARNING).getText().trim();
    }
}
