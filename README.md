# Tienda QA automation

[![QA](https://github.com/matii1942/tienda-qa-automation/actions/workflows/qa.yml/badge.svg)](https://github.com/matii1942/tienda-qa-automation/actions/workflows/qa.yml)

BDD test suite for [ZapasDep](https://github.com/matii1942/E-commerce), a storefront with an
Express + MongoDB API: catalogue, accounts, cart and checkout.

**Java 21 · Cucumber · Selenium WebDriver · Rest Assured · JUnit 5 · Maven · GitHub Actions**

- **API tests** with Rest Assured: catalogue, registration and login, orders, authorization.
- **UI tests** with Selenium and the Page Object pattern: browsing, filters, search, cart
  and checkout in Chrome.
- **Scenarios in Gherkin**, readable by someone who does not read Java.
- **CI** that brings up the real store with Docker, seeds it, and runs everything on every
  push.
- **[10 defects found](BUGS.md)**, each with a scenario that reproduces it.

## What is covered

| Feature | Scenarios | Highlights |
| --- | --- | --- |
| `api/catalogue.feature` | Catalogue, categories, lookup by name, write protection | 37 seeded products, no Mongo internals leak |
| `api/authentication.feature` | Register, log in, duplicates, wrong credentials, 401s | The password never comes back in a response |
| `api/orders.feature` | Totals, stock, invalid orders, isolation between customers | The server ignores a price sent by the client; 1299.99 × 3 is exactly 3899.97 |
| `api/authorization.feature` | Acting on someone else's account, roles | All `@known-bug` |
| `ui/storefront.feature` | Grid, category filter, accent-insensitive search | Filters combine |
| `ui/cart.feature` | Badge, lines, totals, remove, empty, reload | Prices come from the API, not the browser |
| `ui/checkout.feature` | Register from the cart and buy; out-of-stock | End to end: UI action, then checked through the API |

## Running it

**Requirements:** Java 21, Maven, Chrome, and Docker for the store.

**1. Start the store** (in a clone of the E-commerce repository):

```bash
cp .env.example .env              # PowerShell: Copy-Item .env.example .env
# put a long random value in JWT_SECRET
docker compose up -d --build
docker compose exec api npm run seed
```

The store is now at http://localhost:3001.

**2. Run the tests** (in this repository):

```bash
mvn test                                         # everything that must pass
mvn test -Dcucumber.filter.tags="@api"           # API only
mvn test -Dcucumber.filter.tags="@ui"            # UI only
mvn test -Dheadless=false                        # watch Chrome do it
mvn test -Dcucumber.filter.tags="@known-bug"     # reproduce the defects in BUGS.md
mvn test -DbaseUrl=https://another-host          # a store running somewhere else
```

Selenium Manager downloads the right chromedriver on the first run; nothing to install by
hand.

**Reports:** `target/cucumber-report.html`. In CI, the reports of both runs are attached to
each workflow run as the `cucumber-reports` artifact.

## How it is built

```
src/test/java/io/github/matii1942/tienda/
├─ api/StoreApi.java        Rest Assured client: one method per endpoint
├─ pages/                   Page Objects: StorefrontPage, CartPage, AccountPage
├─ steps/                   Step definitions, one class per area
├─ support/                 TestContext (shared per scenario), Hooks, test data
└─ config/Config.java       baseUrl, headless, timeouts
src/test/resources/features/
├─ api/
└─ ui/
```

Some decisions worth explaining:

- **Every scenario creates its own data.** Customers get a unique email, and anything that
  buys uses its own product with a known price and stock, created through the API. A test
  that depended on the seeded stock of 10 would break as soon as anything else bought that
  product. An `@After` hook deletes what the scenario created.
- **UI actions are checked through the API.** After buying in the browser, the scenario asks
  the API for the stock. The confirmation message proves the page *said* the purchase
  worked; the stock proves it *did*.
- **Money is compared as `BigDecimal`.** Rest Assured is configured to parse numbers exactly,
  so a total of `3899.9700000000003` fails instead of passing by rounding.
- **Explicit waits, no `sleep`.** The store draws the grid and the cart after the API
  answers; every page object waits for that state.
- **Names are read with `textContent`.** The CSS capitalizes product names, so `getText()`
  returns "Zapatilla 01" while the API says "zapatilla 01". The tests compare the data, not
  the styling.
- **Known bugs are tests, not notes.** A `@known-bug` scenario describes the correct
  behaviour. It is excluded from the regression run so CI stays green, runs separately so
  the report shows it failing, and becomes a regular test the day the bug is fixed.
- **Failures carry evidence.** A failing scenario gets a screenshot and the last API response
  attached to the HTML report.

## Defects found

See [BUGS.md](BUGS.md): steps to reproduce, expected and actual result, cause in the store's
code, and a suggested fix for each one. The most serious: any customer can delete another
customer's account, and any customer can add a product whose name runs a script in every
visitor's browser.
