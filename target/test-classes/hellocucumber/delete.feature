Feature: Delete
  Scenario: DeleteNormalFlow
    Given We are connected to the REST API
    And There is at least one todo present in the system
    And We want to delete a todo with the DELETE method
    When A valid integer id is provided
    Then The specified todo will be successfully deleted

  Scenario: DeleteErrorFlow_no_id
    Given We are connected to the REST API
    And There is at least one todo present in the system
    And We want to delete a todo with the DELETE method
    When No id is provided
    Then The call will fail with a 404 response code

  Scenario: DeleteErrorFlow_invalid_integer_id
    Given We are connected to the REST API
    And There is at least one todo present in the system
    And We want to delete a todo with the DELETE method
    When An invalid integer id is provided
    Then The call will fail with a 404 response code

  Scenario: DeleteErrorFlow_non_integer_id
    Given We are connected to the REST API
    And There is at least one todo present in the system
    And We want to delete a todo with the DELETE method
    When A non-integer value is provided as the id
    Then The call will fail with a 404 response code