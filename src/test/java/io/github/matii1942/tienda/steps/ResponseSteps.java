package io.github.matii1942.tienda.steps;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.github.matii1942.tienda.api.StoreApi;
import io.github.matii1942.tienda.support.TestContext;
import io.restassured.response.Response;
import java.util.Map;

/** Assertions on the last API answer, shared by every API feature. */
public class ResponseSteps {

    private final TestContext context;

    public ResponseSteps(TestContext context) {
        this.context = context;
    }

    @Then("the response status is {int}")
    public void theResponseStatusIs(int expected) {
        Response response = context.lastResponse();
        assertThat(response.statusCode())
                .as("status of %s", describe(response))
                .isEqualTo(expected);
    }

    @Then("the response is successful")
    public void theResponseIsSuccessful() {
        Response response = context.lastResponse();
        assertThat(response.statusCode()).as("status of %s", describe(response)).isBetween(200, 299);
    }

    @Then("the response is a client error")
    public void theResponseIsAClientError() {
        Response response = context.lastResponse();
        assertThat(response.statusCode()).as("status of %s", describe(response)).isBetween(400, 499);
    }

    @Then("the response field {string} is {string}")
    public void theResponseFieldIs(String path, String expected) {
        assertThat(StoreApi.json(context.lastResponse()).getString(path))
                .as("field '%s'", path)
                .isEqualTo(expected);
    }

    @Then("the response does not contain the field {string}")
    public void theResponseDoesNotContainTheField(String field) {
        Map<String, Object> body = StoreApi.json(context.lastResponse()).get();
        assertThat(body).as("response body").doesNotContainKey(field);
    }

    @Then("the response message contains {string}")
    public void theResponseMessageContains(String fragment) {
        assertThat(StoreApi.json(context.lastResponse()).getString("message")).contains(fragment);
    }

    /** Status line plus body, so a failed assertion says what the API actually answered. */
    private static String describe(Response response) {
        String body = response.asString();
        if (body.length() > 500) {
            body = body.substring(0, 500) + "...";
        }
        return "response " + body;
    }
}
