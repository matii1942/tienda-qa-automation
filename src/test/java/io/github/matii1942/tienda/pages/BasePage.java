package io.github.matii1942.tienda.pages;

import io.github.matii1942.tienda.config.Config;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * What every page of the store has in common: the header with the cart badge, and the
 * way the store hides things — a {@code disabled} CSS class instead of removing them.
 */
abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    private static final By CART_BADGE = By.id("numero");

    BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Config.uiTimeout());
    }

    protected void openPath(String path) {
        driver.get(Config.baseUrl() + path);
    }

    /** Units in the cart, as the badge in the header shows them. */
    public int cartBadge() {
        // When the cart is empty the badge is hidden, and getText() returns "" for hidden
        // elements. textContent is what the page actually wrote.
        String text = driver.findElement(CART_BADGE).getDomProperty("textContent");
        return Integer.parseInt(text.trim());
    }

    public void waitForCartBadge(int expected) {
        wait.until(d -> cartBadge() == expected);
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    public void waitForUrlContaining(String fragment) {
        wait.until(ExpectedConditions.urlContains(fragment));
    }

    /** The store shows and hides panels by toggling the "disabled" class. */
    protected boolean isShown(By locator) {
        WebElement element = driver.findElement(locator);
        String classes = element.getDomAttribute("class");
        return classes == null || !classes.contains("disabled");
    }

    protected WebElement waitUntilShown(By locator) {
        wait.until(d -> isShown(locator));
        return driver.findElement(locator);
    }

    protected Object js(String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }
}
