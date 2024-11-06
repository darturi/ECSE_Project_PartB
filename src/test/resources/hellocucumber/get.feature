Feature: Get
  Scenario: GetNormalFlow_get_all
    Given We are connected to the REST API
    And There is at least one todo present in the system
    And We want to call the GET method
    Then All todos will be successfully returned

  Scenario: GetAlternateFlow_by_id
    Given We are connected to the REST API
    And There is at least one todo present in the system
    And We want to call the GET method
    When A valid integer id is provided
    Then The specified todo will be successfully returned

  Scenario: GetAlternateFlow_valid_id_XML_output_requested
    Given We are connected to the REST API
    And There is at least one todo present in the system
    And We want to call the GET method
    When A valid integer id is provided
    And XML output is requested
    Then The specified todo will be successfully returned

  Scenario: GetAlternateFlow_get_all_XML_output_requested
    Given We are connected to the REST API
    And There is at least one todo present in the system
    And We want to call the GET method
    When XML output is requested
    Then All todos will be successfully returned

  Scenario: GetErrorFlow_invalid_integer_id
    Given We are connected to the REST API
    And There is at least one todo present in the system
    And We want to call the GET method
    When An invalid integer id is provided
    Then No todo will be returned

  Scenario: GetErrorFlow_non_integer_id
    Given We are connected to the REST API
    And There is at least one todo present in the system
    And We want to call the GET method
    When A non-integer value is provided as the id
    Then No todo will be returned