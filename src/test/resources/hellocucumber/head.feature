Feature: Head
  Scenario: HeadNormalFlow_no_id
    Given We are connected to the REST API
    And There is at least one todo present in the system
    And We want to call the HEAD method
    When No id is provided
    Then The header for all todos will be successfully returned

  Scenario: HeadAlternateFlow_valid_integer_id_provided
    Given We are connected to the REST API
    And There is at least one todo present in the system
    And We want to call the HEAD method
    When A valid integer id is provided
    Then The header for the specified todo will be successfully returned

  Scenario: HeadErrorFlow_non_integer_id_provided
    Given We are connected to the REST API
    And There is at least one todo present in the system
    And We want to call the HEAD method
    When A non-integer value is provided as the id
    Then Other than a successful response code nothing will be returned

  Scenario: HeadErrorFlow_nonexistent_integer_id_provided
    Given We are connected to the REST API
    And There is at least one todo present in the system
    And We want to call the HEAD method
    When An invalid integer id is provided
    Then Other than a successful response code nothing will be returned