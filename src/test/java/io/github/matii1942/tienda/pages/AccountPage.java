package io.github.matii1942.tienda.pages;

import io.github.matii1942.tienda.support.Customer;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** cuenta.html: log in and register forms. */
public class AccountPage extends BasePage {

    private static final By LOGIN_EMAIL = By.id("login-email");
    private static final By LOGIN_PASSWORD = By.id("login-password");
    private static final By LOGIN_SUBMIT = By.cssSelector("#form-login button[type='submit']");

    private static final By REGISTER_SUBMIT = By.cssSelector("#form-registro button[type='submit']");
    private static final By MESSAGE = By.id("cuenta-mensaje");

    public AccountPage(WebDriver driver) {
        super(driver);
    }

    public AccountPage open() {
        openPath("/cuenta.html");
        return this;
    }

    public void logIn(String email, String password) {
        type(LOGIN_EMAIL, email);
        type(LOGIN_PASSWORD, password);
        driver.findElement(LOGIN_SUBMIT).click();
    }

    public void register(Customer customer) {
        type(By.id("registro-nombre"), customer.nombre());
        type(By.id("registro-apellido"), customer.apellido());
        type(By.id("registro-edad"), String.valueOf(customer.edad()));
        type(By.id("registro-carrera"), customer.carrera());
        type(By.id("registro-email"), customer.email());
        type(By.id("registro-password"), customer.password());
        driver.findElement(REGISTER_SUBMIT).click();
    }

    public String message() {
        return waitUntilShown(MESSAGE).getText().trim();
    }

    private void type(By field, String text) {
        WebElement input = driver.findElement(field);
        input.clear();
        input.sendKeys(text);
    }
}
