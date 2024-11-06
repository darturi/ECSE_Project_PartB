Feature: PostCreate
  Scenario: PostNormalFlow_title_only
    Given We are connected to the REST API
    And We want to create a todo with the POST method
    When A valid title is provided to the request body
    Then The described todo will be successfully created

  Scenario: PostAlternateFlow_title_and_description
    Given We are connected to the REST API
    And We want to create a todo with the POST method
    When A valid title is provided to the request body
    And A valid description is provided to the request body
    Then The described todo will be successfully created

  Scenario: PostAlternateFlow_title_and_doneStatus
    Given We are connected to the REST API
    And We want to create a todo with the POST method
    When A valid title is provided to the request body
    And A valid doneStatus is provided to the request body
    Then The described todo will be successfully created

  Scenario: PostAlternateFlow_title_description_and_doneStatus
    Given We are connected to the REST API
    And We want to create a todo with the POST method
    When A valid title is provided to the request body
    And A valid doneStatus is provided to the request body
    And A valid description is provided to the request body
    Then The described todo will be successfully created

  Scenario: PostAlternateFlow_valid_XML_input
    Given We are connected to the REST API
    And We want to create a todo with the POST method
    When Valid XML input is provided
    Then The described todo will be successfully created

  Scenario: PostErrorFlow_no_title
    Given We are connected to the REST API
    And We want to create a todo with the POST method
    When No title is provided to the request body
    Then The call will fail with a 400 response code

  Scenario: PostErrorFlow_empty_title
    Given We are connected to the REST API
    And We want to create a todo with the POST method
    When An empty string is provided as the title to the request body
    Then The call will fail with a 400 response code

  Scenario: PostErrorFlow_valid_title_invalid_doneStatus
    Given We are connected to the REST API
    And We want to create a todo with the POST method
    When A valid title is provided to the request body
    And An invalid doneStatus is provided to the request body
    Then The call will fail with a 400 response code

  Scenario: PostErrorFlow_empty_body
    Given We are connected to the REST API
    And We want to create a todo with the POST method
    When An empty request body is passed
    Then The call will fail with a 400 response code

  Scenario: PostErrorFlow_no_body
    Given We are connected to the REST API
    And We want to create a todo with the POST method
    When No body is provided
    Then The call will fail with a 400 response code

  Scenario: PostErrorFlow_malformedJSON
    Given We are connected to the REST API
    And We want to create a todo with the POST method
    When Malformed JSON input is provided
    Then The call will fail with a 400 response code

  Scenario: PostErrorFlow_malformedXML
    Given We are connected to the REST API
    And We want to create a todo with the POST method
    When Malformed XML input is provided
    Then The call will fail with a 400 response code