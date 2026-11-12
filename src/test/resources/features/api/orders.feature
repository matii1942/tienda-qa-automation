@api
Feature: Placing orders
  The browser sends product ids and quantities, never prices. The server looks up the
  prices, checks the stock and computes the total itself.

  Background:
    Given I am logged in as a new customer
    And a test product priced 1299.99 with 5 units in stock

  Scenario: The server computes the total without floating point drift
    When I order 3 units of the test product
    Then the response status is 201
    And the order total is 3899.97
    And the order has 1 line
    And the order line has unit price 1299.99, quantity 3 and subtotal 3899.97

  Scenario: A price sent by the client is ignored
    When I order 1 unit of the test product claiming a price of 1
    Then the response status is 201
    And the order total is 1299.99

  Scenario: Placing an order takes the units out of stock
    When I order 3 units of the test product
    Then the response status is 201
    And the test product has 2 units in stock

  Scenario: Repeated lines for the same product are merged
    When I order the test product in two lines of 2 and 1 units
    Then the response status is 201
    And the order has 1 line
    And the order line has unit price 1299.99, quantity 3 and subtotal 3899.97

  Scenario: Ordering more than the stock is rejected and the stock is untouched
    When I order 6 units of the test product
    Then the response status is 409
    And the response message contains "Stock insuficiente"
    And the test product has 5 units in stock

  Scenario: The last units can be bought, and then the product is sold out
    When I order 5 units of the test product
    Then the response status is 201
    And the test product has 0 units in stock
    When I order 1 unit of the test product
    Then the response status is 409

  Scenario: A product that is not available cannot be ordered
    Given the test product is marked as "VENDIDO"
    When I order 1 unit of the test product
    Then the response status is 409
    And the response message contains "no esta disponible"

  Scenario: Ordering a product that does not exist returns 404
    When I order 1 unit of a product that does not exist
    Then the response status is 404

  Scenario Outline: Malformed orders are rejected with 400 before touching the stock
    When I send an order with <problem>
    Then the response status is 400
    And the test product has 5 units in stock

    Examples:
      | problem                     |
      | no items                    |
      | a quantity of 0             |
      | a negative quantity         |
      | a fractional quantity       |
      | more than 20 units per line |
      | a malformed product id      |

  Scenario: Customers only see their own orders
    Given I have ordered 1 unit of the test product
    When another customer lists their orders
    Then the response status is 200
    And my order is not in the list
    When I list my orders
    Then my order is in the list
