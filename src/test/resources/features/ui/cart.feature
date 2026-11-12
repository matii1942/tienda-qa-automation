@ui
Feature: Shopping cart
  The browser keeps only product ids and quantities. Names and prices are fetched from
  the API every time the cart is drawn.

  Scenario: Adding products updates the cart badge
    Given I open the storefront with an empty cart
    When I add "zapatilla 01" to the cart 2 times
    And I add "gorra 01" to the cart 1 time
    Then the cart badge shows 3

  Scenario: The cart shows the product with the server's price and the total
    Given a test product priced 1299.99 with 5 units in stock
    And I open the storefront with an empty cart
    When I add the test product to the cart 2 times
    And I open the cart
    Then the cart has 1 line
    And the cart contains the test product with quantity 2
    And the cart total is 2599.98

  Scenario: The cart survives a page reload
    Given I open the storefront with an empty cart
    When I add "remera 01" to the cart 2 times
    And I reload the page
    Then the cart badge shows 2

  Scenario: Removing a line from the cart
    Given a test product priced 1000 with 5 units in stock
    And I open the storefront with an empty cart
    When I add "gorra 02" to the cart 1 time
    And I add the test product to the cart 1 time
    And I open the cart
    And I remove the test product from the cart
    Then the cart has 1 line
    And the cart badge shows 1

  Scenario: Emptying the cart
    Given I open the storefront with an empty cart
    When I add "zapatilla 02" to the cart 3 times
    And I open the cart
    And I empty the cart
    Then the cart shows that it is empty
    And the cart badge shows 0

  Scenario: Buying without a session sends you to log in and keeps the cart
    Given I open the storefront with an empty cart
    When I add "zapatilla 03" to the cart 1 time
    And I open the cart
    And I click Buy now
    Then I am sent to the account page
    And the cart badge shows 1
