@api @security
Feature: Authorization
  A session proves who you are. It should not let you act on someone else's account,
  and a customer account should not be able to edit the shop.

  Every scenario here reproduces a defect from BUGS.md: they describe the expected
  behaviour and fail against the current store.

  Background:
    Given the customers "Alice" and "Bob" are registered and logged in

  @known-bug @BUG-001
  Scenario: A customer cannot delete another customer's account
    When "Bob" deletes the account of "Alice"
    Then the response status is 403
    And "Alice" can still log in

  @known-bug @BUG-001
  Scenario: A customer cannot change another customer's data
    When "Bob" changes the name of "Alice" to "Hacked"
    Then the response status is 403

  @known-bug @BUG-002
  Scenario: A customer cannot list every registered account
    When "Bob" requests the list of all users
    Then the response status is 403

  @known-bug @BUG-003
  Scenario: Changing the password through the update endpoint keeps the account usable
    When "Alice" changes her own password to "NewPass123"
    Then the response is successful
    And "Alice" can log in with the password "NewPass123"

  @known-bug @BUG-004
  Scenario: A customer account cannot add products to the shop
    When "Bob" creates a product
    Then the response status is 403

  @known-bug @BUG-009
  Scenario: A product cannot be given a negative price
    Given a test product priced 1000 with 5 units in stock
    When I change the price of the test product to -100
    Then the response status is 400

  @known-bug @BUG-010
  Scenario: A malformed product id is a client error, not a server error
    When "Alice" deletes the product with id "not-a-valid-id"
    Then the response is a client error
