package io.github.matii1942.tienda.support;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.github.matii1942.tienda.api.StoreApi;
import io.github.matii1942.tienda.config.Config;
import java.nio.charset.StandardCharsets;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class Hooks {

    private final TestContext context;

    public Hooks(TestContext context) {
        this.context = context;
    }

    @Before("@ui")
    public void openBrowser() {
        ChromeOptions options = new ChromeOptions();

        if (Config.headless()) {
            options.addArguments("--headless=new");
        }

        options.addArguments(
                "--window-size=1366,900",
                // Needed inside containers and CI runners; harmless on a desktop.
                "--no-sandbox",
                "--disable-dev-shm-usage");

        context.driver = new ChromeDriver(options);
    }

    /**
     * Evidence first, then cleanup: a failing scenario gets a screenshot and the last API
     * answer attached to the report, and every product and account it created is removed.
     */
    @After
    public void tearDown(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                attachEvidence(scenario);
            }
        } finally {
            if (context.driver != null) {
                context.driver.quit();
            }
            cleanUp();
        }
    }

    private void attachEvidence(Scenario scenario) {
        if (context.driver instanceof TakesScreenshot camera) {
            try {
                byte[] png = camera.getScreenshotAs(OutputType.BYTES);
                scenario.attach(png, "image/png", "screenshot");
            } catch (RuntimeException ignored) {
                // The browser may already be gone; the report still has the step error.
            }
        }

        try {
            var response = context.lastResponse();
            String evidence = response.statusLine() + "\n\n" + response.asPrettyString();
            scenario.attach(evidence.getBytes(StandardCharsets.UTF_8), "text/plain", "last API response");
        } catch (IllegalStateException noApiCall) {
            // UI-only scenario: nothing to attach.
        }
    }

    /** Best effort: a failed cleanup must not hide the real result of the scenario. */
    private void cleanUp() {
        for (String[] product : context.productsToDelete) {
            try {
                StoreApi.deleteProduct(product[1], product[0]);
            } catch (RuntimeException ignored) {
                // Store unreachable: nothing else to do.
            }
        }

        for (Customer customer : context.customersToDelete) {
            if (customer.id() == null || customer.token() == null) {
                continue;
            }
            try {
                StoreApi.deleteUser(customer.token(), customer.id());
            } catch (RuntimeException ignored) {
                // Already deleted by the scenario, or store unreachable.
            }
        }
    }
}
