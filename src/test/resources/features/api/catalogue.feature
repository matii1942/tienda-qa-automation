@api
Feature: Product catalogue API
  Reading the catalogue is public. Changing it requires a session token.

  Scenario: The health check answers
    When I call the health check
    Then the response status is 200
    And the response field "status" is "ok"

  Scenario: The catalogue lists the 37 seeded products, each with its category
    When I request the product catalogue
    Then the response status is 200
    And the catalogue contains the 37 seeded products
    And every seeded product has a category with a name and a slug
    And no product in the catalogue exposes Mongo internals

  Scenario: The three categories are available
    When I request the list of categories
    Then the response status is 200
    And the category slugs are "gorras, remeras, zapatillas"

  Scenario: A product can be fetched by its name
    When I request the product named "zapatilla 01"
    Then the response status is 200
    And the response field "name" is "zapatilla 01"
    And the response field "category.slug" is "zapatillas"

  Scenario: Asking for a product that does not exist returns 404
    When I request the product named "this product does not exist"
    Then the response status is 404

  Scenario Outline: Changing the catalogue without a session is rejected
    When I call "<method>" "<path>" without a token
    Then the response status is 401

    Examples:
      | method | path                                    |
      | POST   | /api/products                           |
      | PUT    | /api/products/000000000000000000000000  |
      | DELETE | /api/products/000000000000000000000000  |
      | POST   | /api/categories                         |
