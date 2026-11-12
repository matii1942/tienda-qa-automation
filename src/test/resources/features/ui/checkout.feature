@ui
Feature: Checkout
  "Buy now" sends the order to the API. The total the customer sees on the confirmation
  is the one the server computed.

  Scenario: A new customer registers from the cart and completes the purchase
    Given a test product priced 1299.99 with 5 units in stock
    And I open the storefront with an empty cart
    When I add the test product to the cart 2 times
    And I open the cart
    And I click Buy now
    Then I am sent to the account page
    When I create an account from the account page
    Then I am taken back to the cart
    When I click Buy now
    Then I see the purchase confirmation with the total 2599.98
    And the cart badge shows 0
    And the test product has 3 units in stock

  Scenario: Buying more than the stock shows the server's reason and keeps the cart
    Given a test product priced 1000 with 1 unit in stock
    And I am logged in through the account page
    And I open the storefront with an empty cart
    When I add the test product to the cart 2 times
    And I open the cart
    And I click Buy now
    Then the cart shows the warning "Stock insuficiente"
    And the cart contains the test product with quantity 2
    And the test product has 1 unit in stock

  @known-bug @BUG-008 @security
  Scenario: Product names are shown as text, not run as HTML (stored XSS)
    Given a product whose name contains the HTML "<img src=x onerror=window.__xss=1>"
    When I open the storefront
    Then no script hidden in a product name has run
