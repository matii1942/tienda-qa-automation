@api
Feature: Registration and login
  Customers register with their personal data and log in to receive a session token.

  Scenario: A new customer can register and log in
    Given a new customer has registered
    When the customer logs in with the correct password
    Then the response status is 200
    And the response contains a session token

  Scenario: The registration answer never includes the password
    When a new customer registers
    Then the response status is 200
    And the response does not contain the field "password"

  Scenario: The same email cannot be registered twice
    Given a new customer has registered
    When another customer registers with the same email
    Then the response status is 400

  Scenario: Logging in with a wrong password is rejected
    Given a new customer has registered
    When the customer logs in with the password "Wrong1234"
    Then the response status is 400
    And the response does not contain a session token

  Scenario: Logging in with an unknown email is rejected
    When someone logs in as "nobody@example.test" with the password "Secret123"
    Then the response status is 400
    And the response does not contain a session token

  Scenario Outline: Protected endpoints reject requests without a valid session
    When I call "<method>" "<path>" with the authorization header "<header>"
    Then the response status is 401

    Examples:
      | method | path               | header                  |
      | GET    | /api/orders        |                         |
      | POST   | /api/orders        |                         |
      | GET    | /api/users/getAll  |                         |
      | GET    | /api/orders        | Bearer not.a.real.token |
      | GET    | /api/orders        | Bearer                  |

  # ---------------------------------------------------------------- defects
  # These reproduce BUGS.md entries and fail until the store is fixed.

  @known-bug @BUG-005
  Scenario: A password that breaks the rules is a client error, not a server error
    When a new customer registers with the password "abc"
    Then the response status is 400

  @known-bug @BUG-006
  Scenario: A password longer than 12 characters is rejected
    When a new customer registers with the password "Abcdef1234567890"
    Then the response status is 400

  @known-bug @BUG-007
  Scenario: Registering without a password is rejected
    When a new customer registers without a password
    Then the response status is 400
