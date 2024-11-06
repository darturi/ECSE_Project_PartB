Feature: PostUpdate
  Scenario: PostUpdateNormalFlow_valid_id_valid_title
    Given We are connected to the REST API
    And There is at least one todo present in the system
    And We want to update a todo with the POST method
    When A valid title is provided to the request body
    And A valid integer id is provided
    Then The specified todo will be successfully updated as described

  Scenario: PostUpdateAlternateFlow_valid_id_title_and_description
    Given We are connected to the REST API
    And There is at least one todo present in the system
    And We want to update a todo with the POST method
    When A valid title is provided to the request body
    And A valid description is provided to the request body
    And A valid integer id is provided
    Then The specified todo will be successfully updated as described

  Scenario: PostUpdateAlternateFlow_valid_id_title_and_doneStatus
    Given We are connected to the REST API
    And There is at least one todo present in the system
    And We want to update a todo with the POST method
    When A valid title is provided to the request body
    And A valid doneStatus is provided to the request body
    And A valid integer id is provided
    Then The specified todo will be successfully updated as described

  Scenario: PostUpdateAlternateFlow_valid_id_title_description_and_doneStatus
    Given We are connected to the REST API
    And There is at least one todo present in the system
    And We want to update a todo with the POST method
    When A valid title is provided to the request body
    And A valid integer id is provided
    And A valid doneStatus is provided to the request body
    And A valid description is provided to the request body
    Then The specified todo will be successfully updated as described

  Scenario: PostUpdateAlternateFlow_valid_id_with_XML_input
    Given We are connected to the REST API
    And There is at least one todo present in the system
    And We want to update a todo with the POST method
    When A valid integer id is provided
    And Valid XML input is provided
    Then The specified todo will be successfully updated as described

  Scenario: PostUpdateErrorFlow_valid_id_no_title
    Given We are connected to the REST API
    And There is at least one todo present in the system
    And We want to update a todo with the POST method
    When A valid integer id is provided
    And No title is provided to the request body
    Then The specified todo will be successfully returned

  Scenario: PostUpdateErrorFlow_valid_id_empty_title
    Given We are connected to the REST API
    And There is at least one todo present in the system
    And We want to update a todo with the POST method
    When A valid integer id is provided
    And An empty string is provided as the title to the request body
    Then The call will fail with a 400 response code

  Scenario: PostUpdateErrorFlow_valid_id_and_title_invalid_doneStatus
    Given We are connected to the REST API
    And There is at least one todo present in the system
    And We want to update a todo with the POST method
    When A valid title is provided to the request body
    And A valid integer id is provided
    And An invalid doneStatus is provided to the request body
    Then The call will fail with a 400 response code

  Scenario: PostUpdateErrorFlow_valid_id_empty_body
    Given We are connected to the REST API
    And There is at least one todo present in the system
    And We want to update a todo with the POST method
    When A valid integer id is provided
    And An empty request body is passed
    Then The specified todo will be successfully returned

  Scenario: PostUpdateErrorFlow_valid_id_no_body
    Given We are connected to the REST API
    And We want to update a todo with the POST method
    And There is at least one todo present in the system
    When A valid integer id is provided
    And No body is provided
    Then The specified todo will be successfully returned

  Scenario: PostUpdateErrorFlow_valid_id_malformed_JSON
    Given We are connected to the REST API
    And We want to update a todo with the POST method
    And There is at least one todo present in the system
    When A valid integer id is provided
    And Malformed JSON input is provided
    Then The call will fail with a 400 response code

  Scenario: PostUpdateErrorFlow_valid_id_malformed_XML
    Given We are connected to the REST API
    And We want to update a todo with the POST method
    And There is at least one todo present in the system
    When A valid integer id is provided
    And Malformed XML input is provided
    Then The call will fail with a 400 response code

  Scenario: PostUpdateErrorFlow_non_existent_integer_id
    Given We are connected to the REST API
    And There is at least one todo present in the system
    And We want to update a todo with the POST method
    When A valid title is provided to the request body
    And An invalid integer id is provided
    Then The call will fail with a 404 response code

  Scenario: PostUpdateErrorFlow_non_integer_id
    Given We are connected to the REST API
    And There is at least one todo present in the system
    And We want to update a todo with the POST method
    When A valid title is provided to the request body
    And A non-integer value is provided as the id
    Then The call will fail with a 404 response code


