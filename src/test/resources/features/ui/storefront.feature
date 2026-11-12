@ui
Feature: Browsing the storefront
  The catalogue grid, filtered by category and by a search box that ignores case and
  accents. Both filters apply together.

  Background:
    Given I open the storefront

  Scenario: The catalogue is displayed with a matching result counter
    Then I see at least 37 products
    And the result counter matches the number of products shown

  Scenario Outline: Filtering by category
    When I select the "<slug>" category
    Then the page title is "<title>"
    And I see <count> products
    And every product shown starts with "<prefix>"

    Examples:
      | slug       | title      | count | prefix    |
      | zapatillas | Zapatillas | 14    | zapatilla |
      | remeras    | Remeras    | 12    | remera    |
      | gorras     | Gorras     | 11    | gorra     |

  Scenario: Search ignores upper case and accents
    When I search for "GORRÁ"
    Then I see 11 products
    And every product shown starts with "gorra"

  Scenario: Category and search are combined
    When I select the "zapatillas" category
    And I search for "01"
    Then the only product shown is "zapatilla 01"

  Scenario: A search with no results says so
    When I search for "zzzz"
    Then the grid shows the message "No encontramos nada"
    And the result counter says "0 resultados"
