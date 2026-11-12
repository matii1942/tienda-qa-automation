package io.github.matii1942.tienda.steps;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.matii1942.tienda.api.StoreApi;
import io.github.matii1942.tienda.support.TestContext;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CatalogueSteps {

    /** Seeded products are named "zapatilla 01", "remera 12", "gorra 07"... (stored lowercase). */
    private static final Pattern SEEDED_NAME = Pattern.compile("^(zapatilla|remera|gorra) \\d{2}$");

    private final TestContext context;

    public CatalogueSteps(TestContext context) {
        this.context = context;
    }

    @When("I call the health check")
    public void iCallTheHealthCheck() {
        context.remember(StoreApi.health());
    }

    @When("I request the product catalogue")
    public void iRequestTheProductCatalogue() {
        context.remember(StoreApi.products());
    }

    @When("I request the list of categories")
    public void iRequestTheListOfCategories() {
        context.remember(StoreApi.categories());
    }

    @When("I request the product named {string}")
    public void iRequestTheProductNamed(String name) {
        context.remember(StoreApi.productByName(name));
    }

    @Then("the catalogue contains the {int} seeded products")
    public void theCatalogueContainsTheSeededProducts(int expected) {
        assertThat(seededProducts()).hasSize(expected);
    }

    @Then("every seeded product has a category with a name and a slug")
    public void everySeededProductHasACategory() {
        assertThat(seededProducts()).allSatisfy(product -> {
            assertThat(product.get("category"))
                    .as("category of %s", product.get("name"))
                    .isInstanceOf(Map.class);

            Map<?, ?> category = (Map<?, ?>) product.get("category");
            assertThat((String) category.get("name")).as("category name of %s", product.get("name")).isNotBlank();
            assertThat((String) category.get("slug")).as("category slug of %s", product.get("name")).isNotBlank();
        });
    }

    @Then("no product in the catalogue exposes Mongo internals")
    public void noProductExposesMongoInternals() {
        List<Map<String, Object>> products = StoreApi.json(context.lastResponse()).getList("$");
        assertThat(products).isNotEmpty().allSatisfy(product -> assertThat(product)
                .containsKey("id")
                .doesNotContainKeys("_id", "__v"));
    }

    @Then("the category slugs are {string}")
    public void theCategorySlugsAre(String commaSeparated) {
        List<String> expected = Arrays.stream(commaSeparated.split(",")).map(String::trim).toList();
        List<String> slugs = StoreApi.json(context.lastResponse()).getList("slug", String.class);
        assertThat(slugs).containsExactlyInAnyOrderElementsOf(expected);
    }

    private List<Map<String, Object>> seededProducts() {
        List<Map<String, Object>> products = StoreApi.json(context.lastResponse()).getList("$");
        return products.stream()
                .filter(p -> p.get("name") instanceof String name && SEEDED_NAME.matcher(name).matches())
                .toList();
    }
}
