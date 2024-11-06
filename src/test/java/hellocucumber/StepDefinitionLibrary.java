package hellocucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Assertions;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StepDefinitionLibrary {
    private HashMap<String, Object> commandBody;
    private HashMap<String, HashMap<String, Object>> todoBank;
    private MethodType methodType;
    private String specified_id;
    private boolean isPostUpdate;
    private boolean useAuxInput;
    private boolean expectUTF;

    private String auxiliaryInputField;
    private boolean isXML;
    private boolean xmlOutput;

    // Helper Methods
    private static int getTodoCount(){
        return when().get("/todos")
                .then().statusCode(200).extract().body().jsonPath().getList("todos").size();
    }
    private static String createExampleTodo(String title, boolean doneStatus, String description){
        int beforeNumberOfTodos = getTodoCount();

        final HashMap<String, Object> commandBody = new HashMap<String, Object>();
        commandBody.put("title", title);
        commandBody.put("doneStatus", doneStatus);
        commandBody.put("description", description);


        final Response body = given().body(commandBody).
                when().post("/todos").
                then().
                statusCode(201).
                contentType(ContentType.JSON).
                and().extract().response();

        JsonPath responseBody = body.jsonPath();

        // Check that the number of todos has increased
        Assertions.assertEquals(getTodoCount(), beforeNumberOfTodos + 1);

        // Check that fields of Todo are as expected
        if ((getTodoCount() == beforeNumberOfTodos + 1) &&
                (responseBody.get("title").equals(title)) &&
                (responseBody.get("doneStatus").equals(String.valueOf(doneStatus))) &&
                (responseBody.get("description").equals(description)))
            return responseBody.get("id");
        else return "-1";
    }

    private static int countOccurrences(String str, String subStr) {
        if (str == null || subStr == null || subStr.isEmpty()) {
            return 0; // Handle edge cases
        }

        int count = 0;
        int index = 0;

        while ((index = str.indexOf(subStr, index)) != -1) {
            count++;
            index += subStr.length(); // Move index forward to continue searching
        }

        return count;
    }

    /*
    // NOT ACCOUNTING FOR
    // TODO: UTF-8 OUTPUT
    // TODO: NO BODY / NO ID, gotta take / off the end
    private Object executeMethod(int expectedCode){
        int beforeTodoCount = getTodoCount();
        Object returnVal;
        if (this.methodType == MethodType.POST){
            returnVal = executePOST(expectedCode);

            // Check the cardinality of to-do items
            if (expectedCode == 201) Assertions.assertEquals(beforeTodoCount + 1, getTodoCount());
            else Assertions.assertEquals(beforeTodoCount, getTodoCount());

            return returnVal;
        }
        else if (this.methodType == MethodType.PUT){
            returnVal = executePUT(expectedCode);

            // Check the cardinality of to-do items
            Assertions.assertEquals(beforeTodoCount, getTodoCount());

            return returnVal;
        }
        else if (this.methodType == MethodType.DELETE){
            returnVal = executeDELETE(expectedCode);

            // Check the cardinality of to-do items
            if (expectedCode == 200) Assertions.assertEquals(beforeTodoCount - 1, getTodoCount());
            else Assertions.assertEquals(beforeTodoCount, getTodoCount());

            return returnVal;
        }
        else if (this.methodType == MethodType.GET){
            returnVal = executeGET(expectedCode);

            // Check the cardinality of to-do items
            Assertions.assertEquals(beforeTodoCount, getTodoCount());

            return returnVal;
        }
        else return null;
    }

    private Object executePOST(int expectedCode){
        final Response body = given().body(this.commandBody).
                when().post("/todos").
                then().
                statusCode(expectedCode).
                contentType(ContentType.JSON).
                and().extract().response();

        return body.jsonPath();
    }

    // TODO: Also adapted from original
    private Object executePUT(int expectedCode){
        ExtractableResponse<Response> response = given().body(this.commandBody).
                when().put("/todos/" + this.specified_id).
                then().
                statusCode(expectedCode).
                contentType(ContentType.JSON).
                and().extract();

        if (expectedCode == 200) return response.response().jsonPath();
        return response.body().jsonPath();
    }

    // TODO: Mismatch from original text, check back if errors arise
    private Object executeDELETE(int expectedCode){
        final JsonPath body = given().body("").
                when().delete("/todos/" + this.specified_id).
                then().
                statusCode(expectedCode).
                contentType(ContentType.JSON).
                and().extract().body().jsonPath();
        return body;
    }

    private Object executeGET(int expectedCode){
        String key = "";
        if (!this.specified_id.isEmpty()) key = "?id=" + this.specified_id;
        final Response body = given().body("").
                when().get("/todos" + key).
                then().
                statusCode(expectedCode).
                contentType(ContentType.JSON).
                and().extract().response();

        return body.jsonPath();
    }


    private boolean assessCorrectness(Object response){
        if (this.methodType == MethodType.POST) return assessCorrectPOST(response);
        else if (this.methodType == MethodType.PUT) return assessCorrectPUT(response);
        else if (this.methodType == MethodType.DELETE) return assessCorrectDELETE(response);
        else if (this.methodType == MethodType.GET) return assessCorrectGET(response);
        else return false;
    }

    private boolean assessCorrectPOST(Object response){
        ExtractableResponse<Response> responseBody = (ExtractableResponse<Response>) response;
        JsonPath responseBodyJson = responseBody.response().jsonPath();
        return
                responseBodyJson.get("title").equals(this.commandBody.get("title")) &&
                responseBodyJson.get("doneStatus").equals(String.valueOf(this.commandBody.get("doneStatus"))) &&
                responseBodyJson.get("description").equals(this.commandBody.get("description"));
    }

    private boolean assessCorrectPUT(Object response){
        // Check response is correct
        ExtractableResponse<Response> responseBody = (ExtractableResponse<Response>) response;
        JsonPath responseBodyJson = responseBody.response().jsonPath();

        boolean b1 = responseBodyJson.get("title").equals(this.commandBody.get("title")) &&
                responseBodyJson.get("doneStatus").equals(String.valueOf(this.commandBody.get("doneStatus"))) &&
                responseBodyJson.get("description").equals(this.commandBody.get("description"));
        if (!b1) return false;

        // Check result of get is correct
        final Response body = given().body("").
                when().get("/todos?id=" + this.specified_id).
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                and().extract().response();

        JsonPath responseBody2 = body.jsonPath();

        List<Object> returnOfGet = responseBody2.getList("todos");

        if (returnOfGet.isEmpty()) return false;

        HashMap<String, Object> elementOfReturn = (HashMap<String, Object>) returnOfGet.get(0);

        return (this.commandBody.get("title").equals(elementOfReturn.get("title"))) &&
                (String.valueOf(this.commandBody.get("doneStatus")).equals(elementOfReturn.get("doneStatus"))) &&
                (this.commandBody.get("description").equals(elementOfReturn.get("description")));

    }

    private boolean assessCorrectDELETE(Object response){
        return true;
    }

    private boolean assessCorrectGET(Object response){
        return true;
    }



    private boolean assessError400Output(Object response){
        if (this.methodType == MethodType.POST) return assessError400OutputPOST(response);
        else if (this.methodType == MethodType.PUT) return false; //assessCorrectPUT(response);
        else if (this.methodType == MethodType.GET) return false; // assessCorrectGET(response);
        else return false;
    }

    private boolean assessError400OutputPOST(Object response){
        JsonPath body = ((ExtractableResponse<Response>) response).body().jsonPath();
        if (this.commandBody.containsKey("title") && this.commandBody.get("title").equals(""))
            return "title : can not be empty".equals(body.getList("errorMessages").get(0));
        else if (!this.commandBody.containsKey("title"))
            return "title : field is mandatory".equals(body.getList("errorMessages").get(0));
        else if (this.commandBody.containsKey("doneStatus") && !(this.commandBody.get("doneStatus") instanceof Boolean))
            return false;
        return true;
    }

    private boolean assessError404Output(Object response){
        if (this.methodType == MethodType.POST) return assessError404OutputPOST(response);
        else if (this.methodType == MethodType.PUT) return false; //assessCorrectPUT(response);
        else if (this.methodType == MethodType.DELETE) return assessError404OutputDELETE(response); // assessCorrectDELETE(response);
        else return false;
    }

    // TODO: Not accounting for utf-8 output
    private boolean assessError404OutputPOST(Object response){
        JsonPath body = ((ExtractableResponse<Response>) response).body().jsonPath();
        return ("No such todo entity instance with GUID or ID " + this.specified_id + " found").equals(body.getList("errorMessages").get(0));
    }

    private boolean assessError404OutputPUT(Object response){
        JsonPath body = ((ExtractableResponse<Response>) response).body().jsonPath();
        return ("No such todo entity instance with GUID or ID " + this.specified_id + " found").equals(body.getList("errorMessages").get(0));
    }

    private boolean assessError404OutputDELETE(Object response){
        JsonPath body = ((ExtractableResponse<Response>) response).body().jsonPath();
        return ("Could not find any instances with todos/" + this.specified_id).equals(body.getList("errorMessages").get(0));
    }



    // TODO: Weird utf-output
    private boolean assessError405Output(Object response){
        return false;
    }

     */

    // GIVENS
    @Given("We are connected to the REST API")
    public void connect_to_rest_api(){
        RestAssured.baseURI = "http://localhost:4567";
        when().post("/admin/data/thingifier")
                    .then().statusCode(200);

        final JsonPath clearedData = when().get("/todos")
                .then().statusCode(200).extract().body().jsonPath();

        final int newNumberOfTodos = clearedData.getList("todos").size();

        Assertions.assertEquals(0, newNumberOfTodos);

        // Baseline variable setting
        this.specified_id = "";
        this.isPostUpdate = false;
        this.useAuxInput = false;
        this.expectUTF = false;
        this.isXML = false;
        this.xmlOutput = false;

    }

    @Given("There are no todos in the system")
    public void no_todo_start_state(){
        if (0 != getTodoCount()){
            final JsonPath clearedData = when().get("/todos")
                    .then().statusCode(200).extract().body().jsonPath();

            final int newNumberOfTodos = clearedData.getList("todos").size();
        }
    }

    @Given("There is at least one todo present in the system")
    public void at_least_one_todo_start_state(){
        this.todoBank = new HashMap<>();

        final JsonPath clearedData = when().get("/todos")
                .then().statusCode(200).extract().body().jsonPath();

        final int newNumberOfTodos = clearedData.getList("todos").size();

        Assertions.assertEquals(0, newNumberOfTodos);

        HashMap<String, Object> title1_hash = new HashMap<>();
        title1_hash.put("title", "TITLE1");
        title1_hash.put("doneStatus", false);
        title1_hash.put("description", "");
        this.todoBank.put((createExampleTodo("TITLE1", false, "")), title1_hash);

        HashMap<String, Object> title2_hash = new HashMap<>();
        title2_hash.put("title", "TITLE2");
        title2_hash.put("doneStatus", true);
        title2_hash.put("description", "");
        this.todoBank.put((createExampleTodo("TITLE2", true, "")), title2_hash);

        HashMap<String, Object> title3_hash = new HashMap<>();
        title3_hash.put("title", "TITLE3");
        title3_hash.put("doneStatus", false);
        title3_hash.put("description", "description3");
        this.todoBank.put((createExampleTodo("TITLE3", false, "description3")), title3_hash);
    }

    @Given("We want to create a todo with the POST method")
    public void setup_post_creation_usage(){
        this.commandBody = new HashMap<>();
        this.methodType = MethodType.POST;
        this.isPostUpdate = false;
    }

    @Given("We want to call the GET method")
    public void setup_get_method_usage(){
        this.methodType = MethodType.GET;
    }

    @Given("We want to call the HEAD method")
    public void setup_head_method_usage(){
        this.methodType = MethodType.HEAD;
    }

    @Given("We want to delete a todo with the DELETE method")
    public void setup_delete_method_usage(){
        this.methodType = MethodType.DELETE;
    }

    @Given("We want to update a todo with the PUT method")
    public void setup_put_method_usage(){
        this.commandBody = new HashMap<>();
        this.methodType = MethodType.PUT;
        // this.isPostUpdate = true;
    }

    @Given("We want to update a todo with the POST method")
    public void setup_post_update_usage(){
        this.commandBody = new HashMap<>();
        this.methodType = MethodType.POST;
        this.isPostUpdate = true;
    }

    // WHENS
    @When("A valid title is provided to the request body")
    public void provide_valid_title(){
        this.commandBody.put("title", "validExampleTitle");
    }

    @When("A valid description is provided to the request body")
    public void provide_valid_description(){
        this.commandBody.put("description", "validDescription");
    }

    @When("A valid doneStatus is provided to the request body")
    public void provide_valid_doneStatus(){
        this.commandBody.put("doneStatus", true);
    }

    @When("An empty string is provided as the title to the request body")
    public void provide_empty_string_title(){
        this.commandBody.put("title", "");
    }

    @When("No title is provided to the request body")
    public void provide_no_title(){
        this.commandBody.remove("title");
    }

    @When("An invalid doneStatus is provided to the request body")
    public void provide_invalid_doneStatus(){
        this.commandBody.put("doneStatus", "invalidDoneStatus");
    }

    @When("An empty request body is passed")
    public void provide_empty_request_body(){
        this.commandBody.clear();
    }

    @When("A valid integer id is provided")
    public void provide_valid_integer_id(){
        // Select random id from existing todos

        // if no todos fail
        if (getTodoCount() == 0) throw new RuntimeException();

        List<String> keysAsArray = new ArrayList<String>(this.todoBank.keySet());
        Random r = new Random();
        this.specified_id = keysAsArray.get(r.nextInt(keysAsArray.size()));
    }

    @When("An invalid integer id is provided")
    public void provide_invalid_integer_id(){
        Random rand = new Random(1);
        int rand_int = rand.nextInt(1000);
        while (this.todoBank.containsKey(String.valueOf(rand_int)))
            rand_int = rand.nextInt(1000);
        this.specified_id = String.valueOf(rand_int);
    }

    @When("No id is provided")
    public void provide_no_id(){
        this.specified_id = "";
        if (this.methodType == MethodType.PUT) this.expectUTF = true;
    }

    @When("A non-integer value is provided as the id")
    public void provide_non_integer_id(){
        this.specified_id = "hello";
    }

    // TODO
    @When("Valid XML input is provided")
    public void valid_XML_input(){
        this.useAuxInput = true;
        this.auxiliaryInputField = "<todo><title>Hello</title></todo>";
        this.isXML = true;
    }

    // TODO
    @When("Malformed XML input is provided")
    public void malformed_XML_input(){
        this.useAuxInput = true;
        this.auxiliaryInputField = "<todo><title>Hello</title></todo";
        this.expectUTF = true;
        this.isXML = true;
    }

    // TODO
    @When("Malformed JSON input is provided")
    public void malformed_JSON_input(){
        this.useAuxInput = true;
        this.commandBody.put("title", "example title");
        this.commandBody.put("description", "another example field");
        this.auxiliaryInputField = this.commandBody.toString().substring(1);
        this.expectUTF = true;
    }

    @When("XML output is requested")
    public void request_xml_output(){
        this.xmlOutput = true;
    }

    @When("No body is provided")
    public void no_body_provided(){
        this.useAuxInput = true;
        this.auxiliaryInputField = "";
    }


    // THEN'S
    @Then("The specified todo will be successfully deleted")
    public void check_successful_delete(){
        // preliminary to-do count
        int beforeTodoCount = getTodoCount();

        final Response body = given().body("").
                when().delete("/todos/" + this.specified_id).
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                and().extract().response();

        // get check to see that the specified id is no longer in the system
        final Response body2 = given().body("").
                when().get("/todos?id=" + this.specified_id).
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                and().extract().response();


        JsonPath responseBody = body2.jsonPath();

        List<Object> returnOfGet = responseBody.getList("todos");

        Assertions.assertEquals(returnOfGet.size(), 0);

        // Check that the number of to-do's in the system decreased as expected
        Assertions.assertEquals(beforeTodoCount - 1, getTodoCount());
    }

    private void checkSuccessfulXMLPost(){
        Response response = RestAssured
                .given()
                .contentType("application/xml") // Specify the content type as XML
                .body(this.auxiliaryInputField) // Set the request body
                .when()
                .post("/todos") // Specify the endpoint for the POST request
                .then()
                .extract().response(); // Extract the response

        Assertions.assertEquals(response.getStatusCode(), 201);
        JsonPath responseBody = response.jsonPath();

        Assertions.assertEquals(responseBody.get("title"), "Hello");
        Assertions.assertEquals(responseBody.get("doneStatus"), "false");
        Assertions.assertEquals(responseBody.get("description"), "");

    }

    @Then("The described todo will be successfully created")
    public void check_successful_post(){
        // preliminary to-do count
        int beforeTodoCount = getTodoCount();

        if (this.isXML) {
            checkSuccessfulXMLPost();
            Assertions.assertEquals(beforeTodoCount + 1, getTodoCount());
            return;
        }

        // Actually post the to-do
        final Response body = given().body(this.commandBody).
                when().post("/todos").
                then().
                statusCode(201).
                contentType(ContentType.JSON).
                and().extract().response();

        JsonPath responseBody = body.jsonPath();

        // Check that fields of new to-do are as expected
        String expectedTitle = (String) this.commandBody.get("title");
        String expected_doneStatus = "false";
        if (this.commandBody.containsKey("doneStatus"))
            expected_doneStatus = String.valueOf((boolean) this.commandBody.get("doneStatus"));
        String expectedDescription = "";
        if (this.commandBody.containsKey("description"))
            expectedDescription = (String) this.commandBody.get("description");


        Assertions.assertEquals(responseBody.get("title"), expectedTitle);
        Assertions.assertEquals(responseBody.get("doneStatus"), expected_doneStatus);
        Assertions.assertEquals(responseBody.get("description"), expectedDescription);

        // Check that the number of to-do's in the system increased as expected
        Assertions.assertEquals(beforeTodoCount + 1, getTodoCount());
    }

    private void checkSuccessfulXML_update(){
        Response response = RestAssured
                .given()
                .contentType("application/xml") // Specify the content type as XML
                .body(this.auxiliaryInputField) // Set the request body
                .when()
                .put("/todos/"+this.specified_id) // Specify the endpoint for the POST request
                .then()
                .extract().response(); // Extract the response

        Assertions.assertEquals(200, response.getStatusCode());
        JsonPath responseBody = response.jsonPath();

        Assertions.assertEquals(responseBody.get("title"), "Hello");
        Assertions.assertEquals(responseBody.get("doneStatus"), "false");
        Assertions.assertEquals(responseBody.get("description"), "");
    }

    @Then("The specified todo will be successfully updated as described")
    public void check_successful_update(){
        // preliminary to-do count
        int beforeTodoCount = getTodoCount();

        if (this.isXML){
            checkSuccessfulXML_update();
            Assertions.assertEquals(beforeTodoCount, getTodoCount());
            return;
        }

        String commandInputString = this.commandBody.toString();
        if (this.useAuxInput) commandInputString = this.auxiliaryInputField;

        RequestSpecification response_stage_1 = given().body(commandInputString);

        if (this.isXML) response_stage_1 = response_stage_1.accept("application/xml");

        Response response_stage_2;
        if (this.methodType == MethodType.PUT){
            response_stage_2 = response_stage_1.when().put("/todos/" + this.specified_id);
        } else {
            response_stage_2 = response_stage_1.when().post("/todos/" + this.specified_id);
        }
        response_stage_2 = response_stage_2.
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                and().extract().response();

        JsonPath responseBody = response_stage_2.jsonPath();

        // Check that fields of new to-do are as expected
        String expectedTitle = (String) this.commandBody.get("title");

        String expected_doneStatus = "false";
        if (this.methodType == MethodType.POST) expected_doneStatus = String.valueOf((boolean) this.todoBank.get(this.specified_id).get("doneStatus")); //"false";
        if (this.commandBody.containsKey("doneStatus"))
            expected_doneStatus = String.valueOf((boolean) this.commandBody.get("doneStatus"));


        String expectedDescription = "";
        if (this.methodType == MethodType.POST) expectedDescription = (String) this.todoBank.get(this.specified_id).get("description");
        if (this.commandBody.containsKey("description"))
            expectedDescription = (String) this.commandBody.get("description");


        Assertions.assertEquals(responseBody.get("title"), expectedTitle);
        Assertions.assertEquals(responseBody.get("doneStatus"), expected_doneStatus);
        Assertions.assertEquals(responseBody.get("description"), expectedDescription);

        // Verify in get
        final Response body = given().body("").
                when().get("/todos?id=" + this.specified_id).
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                and().extract().response();

        JsonPath responseBody2 = body.jsonPath();
        List<Object> returnOfGet = responseBody2.getList("todos");
        if (returnOfGet.isEmpty()) Assertions.fail();
        else {
            HashMap<String, Object> elementOfReturn = (HashMap<String, Object>) returnOfGet.get(0);

            Assertions.assertTrue(
                    expectedTitle.equals(elementOfReturn.get("title")) &&
                    (expected_doneStatus.equals(elementOfReturn.get("doneStatus"))) &&
                    (expectedDescription.equals(elementOfReturn.get("description"))));
        }

        // Check that the number of to-do's in the system did not change
        Assertions.assertEquals(beforeTodoCount, getTodoCount());

        this.commandBody.clear();
    }

    private void xmlGetById(){
        final Response body = given().body("").
                accept("application/xml").
                when().get("/todos?id=" + this.specified_id).
                then().
                statusCode(200).
                contentType(ContentType.XML).
                and().extract().response();

        String xmlString = body.body().asString();
        xmlString = xmlString.substring(7, xmlString.length() - 8);

        // System.out.println(xmlString);

        HashMap<String, Object> associatedHash = this.todoBank.get(this.specified_id);

        Assertions.assertEquals(1, countOccurrences(xmlString, "<todo"));
        Assertions.assertEquals(1, countOccurrences(xmlString, "<title"));
        Assertions.assertEquals(1, countOccurrences(xmlString, "<description"));
        Assertions.assertEquals(1, countOccurrences(xmlString, "<doneStatus"));
        Assertions.assertEquals(1, countOccurrences(xmlString, "<id"));

        Assertions.assertEquals(
                associatedHash.get("title"),
                xmlString.substring(
                        xmlString.indexOf("<title>") + "<title>".length(),
                        xmlString.indexOf("</title>")
                )
        );

        Assertions.assertEquals(
                String.valueOf(associatedHash.get("doneStatus")),
                xmlString.substring(
                        xmlString.indexOf("<doneStatus>") + "<doneStatus>".length(),
                        xmlString.indexOf("</doneStatus>")
                )
        );

        Assertions.assertEquals(
                this.specified_id,
                xmlString.substring(
                        xmlString.indexOf("<id>") + "<id>".length(),
                        xmlString.indexOf("</id>")
                )
        );
    }

    @Then("The specified todo will be successfully returned")
    public void check_successful_get_by_id(){
        if (this.xmlOutput){
            xmlGetById();
            return;
        }
        // preliminary to-do count
        int beforeTodoCount = getTodoCount();

        // Check that fields of new to-do are as expected
        String expectedTitle = (String) this.todoBank.get(this.specified_id).get("title");
        String expected_doneStatus = "false";
        if (this.todoBank.get(this.specified_id).containsKey("doneStatus"))
            expected_doneStatus = String.valueOf((boolean) this.todoBank.get(this.specified_id).get("doneStatus"));
        String expectedDescription = "";
        if (this.todoBank.get(this.specified_id).containsKey("description"))
            expectedDescription = (String) this.todoBank.get(this.specified_id).get("description");

        final Response body = given().body("").
                when().get("/todos?id=" + this.specified_id).
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                and().extract().response();

        JsonPath responseBody2 = body.jsonPath();
        List<Object> returnOfGet = responseBody2.getList("todos");
        if (returnOfGet.isEmpty()) Assertions.fail();
        else {
            HashMap<String, Object> elementOfReturn = (HashMap<String, Object>) returnOfGet.get(0);

            Assertions.assertTrue(
                    expectedTitle.equals(elementOfReturn.get("title")) &&
                            (expected_doneStatus.equals(elementOfReturn.get("doneStatus"))) &&
                            (expectedDescription.equals(elementOfReturn.get("description"))));
        }

        // Check that the number of to-do's in the system did not change
        Assertions.assertEquals(beforeTodoCount, getTodoCount());
    }

    @Then("The header for the specified todo will be successfully returned")
    public void check_successful_header_by_id(){
        // preliminary to-do count
        int beforeTodoCount = getTodoCount();

        Headers body = given().body("").
                accept("application/xml").
                when().head("/todos?id=" + this.specified_id).
                then().
                statusCode(200).
                contentType(ContentType.XML).
                and().extract().headers();

        String responseBody = body.asList().toString();

        // System.out.println(responseBody);

        // Assertions.assertEquals("", responseBody);
        Assertions.assertTrue(responseBody.contains("Date="));
        Assertions.assertTrue(responseBody.contains("Content-Type=application/xml"));
        Assertions.assertTrue(responseBody.contains("Transfer-Encoding=chunked"));
        Assertions.assertTrue(responseBody.contains("Server"));

        // Check that the number of to-do's in the system did not change
        Assertions.assertEquals(beforeTodoCount, getTodoCount());
    }

    @Then("The header for all todos will be successfully returned")
    public void check_successful_header_all(){
        int beforeNumberOfTodos = getTodoCount();

        Headers body = given().body("").
                accept("application/xml").
                when().head("/todos").
                then().
                statusCode(200).
                contentType(ContentType.XML).
                and().extract().headers();

        String responseBody = body.asList().toString();

        System.out.println(responseBody);

        // Assertions.assertEquals("", responseBody);

        Assertions.assertTrue(responseBody.contains("Date="));
        Assertions.assertTrue(responseBody.contains("Content-Type=application/xml"));
        Assertions.assertTrue(responseBody.contains("Transfer-Encoding=chunked"));
        Assertions.assertTrue(responseBody.contains("Server"));

        Assertions.assertEquals(beforeNumberOfTodos, getTodoCount());
    }

    private static List<String> extractTodos(String input) {
        List<String> todos = new ArrayList<>();
        // Regex to match the content inside <todo>...</todo>
        Pattern pattern = Pattern.compile("<todo>(.*?)</todo>");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            todos.add(matcher.group(1)); // Get the content inside the tags
        }

        return todos;
    }

    private void getAllXML(){
        final Response body = given().body("").
                accept("application/xml").
                when().get("/todos").
                then().
                statusCode(200).
                contentType(ContentType.XML).
                and().extract().response();

        String xmlString = body.body().asString();
        xmlString = xmlString.substring(7, xmlString.length() - 8);

        for (String elem : extractTodos(xmlString)){
            // Assertions.assertEquals(1, countOccurrences(elem, "<todo"));
            Assertions.assertEquals(1, countOccurrences(elem, "<title"));
            Assertions.assertEquals(1, countOccurrences(elem, "<description"));
            Assertions.assertEquals(1, countOccurrences(elem, "<doneStatus"));
            Assertions.assertEquals(1, countOccurrences(elem, "<id"));

            String id =  elem.substring(
                    elem.indexOf("<id>") + "<id>".length(),
                    elem.indexOf("</id>")
            );
            HashMap<String, Object> associatedHash = this.todoBank.get(id);

            Assertions.assertEquals(
                    associatedHash.get("title"),
                    elem.substring(
                            elem.indexOf("<title>") + "<title>".length(),
                            elem.indexOf("</title>")
                    )
            );

            Assertions.assertEquals(
                    String.valueOf(associatedHash.get("doneStatus")),
                    elem.substring(
                            elem.indexOf("<doneStatus>") + "<doneStatus>".length(),
                            elem.indexOf("</doneStatus>")
                    )
            );
        }
    }

    @Then("All todos will be successfully returned")
    public void check_successful_get_all(){
        // preliminary to-do count
        int beforeTodoCount = getTodoCount();

        if (this.xmlOutput) {
            getAllXML();
            return;
        }

        final Response body = given().body("").
                when().get("/todos").
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                and().extract().response();

        JsonPath responseBody = body.jsonPath();

        List<Object> returnOfGet = responseBody.getList("todos");

        Assertions.assertEquals(this.todoBank.size(), returnOfGet.size());

        for (Object o : returnOfGet){
            HashMap<String, Object> elementOfReturn = (HashMap<String, Object>) o;
            HashMap<String, Object> correspondingEntryOfId = this.todoBank.get(elementOfReturn.get("id"));
            Assertions.assertEquals(correspondingEntryOfId.get("title"), elementOfReturn.get("title"));
            Assertions.assertEquals(String.valueOf(correspondingEntryOfId.get("doneStatus")), elementOfReturn.get("doneStatus"));
            Assertions.assertEquals(correspondingEntryOfId.get("description"), elementOfReturn.get("description"));
        }

        // Check that the number of to-do's in the system did not change
        Assertions.assertEquals(beforeTodoCount, getTodoCount());
    }

    @Then("No todo will be returned")
    public void check_no_return(){
        final Response body = given().body("").
                when().get("/todos?id=" + this.specified_id).
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                and().extract().response();


        JsonPath responseBody = body.jsonPath();

        List<Object> returnOfGet = responseBody.getList("todos");

        Assertions.assertEquals(returnOfGet.size(), 0);
    }

    @Then("The call will fail with a 400 response code")
    public void assess400_fail(){
        // preliminary to-do count
        int beforeTodoCount = getTodoCount();

        RequestSpecification response_stage_1;
        if (this.useAuxInput) response_stage_1 = given().body(this.auxiliaryInputField);
        else response_stage_1 = given().body(this.commandBody);

        if (this.isXML) response_stage_1 = response_stage_1.accept("application/xml");

        Response response_stage_2;

        if (this.methodType == MethodType.PUT){
            response_stage_2 = response_stage_1.when().put("/todos/" + this.specified_id);
        } else {
            String requestUrl = "/todos";
            if (this.isPostUpdate) requestUrl = "/todos/" + this.specified_id;

            // System.out.println(requestUrl);
            // System.out.println(commandInputString);

            response_stage_2 = response_stage_1.when().post(requestUrl);
        }

        /*
        String contentType = String.valueOf(ContentType.JSON);
        if (this.expectUTF) contentType = "text/html;charset=utf-8";

        JsonPath body = response_stage_2.
                then().
                statusCode(400).
                contentType(contentType).
                and().extract().body().jsonPath();

         */
        JsonPath body;
        if (this.expectUTF) body = response_stage_2.
                then().
                statusCode(400).
                contentType("text/html;charset=utf-8").
                and().extract().body().jsonPath();
        else body = response_stage_2.
                then().
                statusCode(400).
                contentType(ContentType.JSON).
                and().extract().body().jsonPath();

        /*
        // TODO: check for malformed JSON / XML
        if (this.expectUTF){
            if (this.methodType == MethodType.POST){
                String bodyInput = this.commandBody.toString();
                // Check if you need to use the auxiliary input field
                if (this.useAuxInput) bodyInput = this.auxiliaryInputField;

                // Create request url
                String requestUrl = "/todos";
                if (this.isPostUpdate) requestUrl = "/todos/" + this.specified_id;

                // If the input is in XML form modify request
                if (this.isXML){
                    final JsonPath body = given().body(bodyInput).
                            accept("application/xml").
                            when().post(requestUrl).
                            then().
                            statusCode(400).
                            contentType("text/html;charset=utf-8").
                            and().extract().body().jsonPath();
                } else {
                    final JsonPath body = given().body(bodyInput).
                            when().post(requestUrl).
                            then().
                            statusCode(400).
                            contentType("text/html;charset=utf-8").
                            and().extract().body().jsonPath();
                }
            } else if (this.methodType == MethodType.PUT){
                String bodyInput = this.commandBody.toString();
                // Check if you need to use the auxiliary input field
                if (this.useAuxInput) bodyInput = this.auxiliaryInputField;

                // If the input is in XML form modify request
                if (this.isXML){
                    final JsonPath body = given().body(bodyInput).
                            accept("application/xml").
                            when().put("/todos/" +  this.specified_id).
                            then().
                            statusCode(400).
                            contentType("text/html;charset=utf-8").
                            and().extract().body().jsonPath();
                } else {
                    final JsonPath body = given().body(bodyInput).
                            when().put("/todos/" + this.specified_id).
                            then().
                            statusCode(400).
                            contentType("text/html;charset=utf-8").
                            and().extract().body().jsonPath();
                }
            }
        }

         */
        if (this.expectUTF){

        }
        else if (this.methodType == MethodType.POST){
            /*
            String requestUrl = "/todos";
            if (this.isPostUpdate) requestUrl = "/todos/" + this.specified_id;

            final JsonPath body = given().body(this.commandBody).
                    when().post(requestUrl).
                    then().
                    statusCode(400).
                    contentType(ContentType.JSON).
                    and().extract().body().jsonPath();

             */

            if (this.commandBody.containsKey("title") && this.commandBody.get("title").equals(""))
                Assertions.assertEquals("Failed Validation: title : can not be empty", body.getList("errorMessages").get(0));
            else if (!this.commandBody.containsKey("title"))
                Assertions.assertEquals("title : field is mandatory", body.getList("errorMessages").get(0));
            else if (this.commandBody.containsKey("doneStatus") && !(this.commandBody.get("doneStatus") instanceof Boolean))
                Assertions.assertEquals("Failed Validation: doneStatus should be BOOLEAN",
                        body.getList("errorMessages").get(0));
        }
        else if (this.methodType == MethodType.PUT){
            /*
            final JsonPath body = given().body(this.commandBody).
                    when().put("/todos/" + this.specified_id).
                    then().
                    statusCode(400).
                    contentType(ContentType.JSON).
                    and().extract().body().jsonPath();

             */

            if (this.commandBody.containsKey("title") && this.commandBody.get("title").equals(""))
                Assertions.assertEquals("Failed Validation: title : can not be empty", body.getList("errorMessages").get(0));
            else if (!this.commandBody.containsKey("title"))
                Assertions.assertEquals("title : field is mandatory", body.getList("errorMessages").get(0));
            else if (this.commandBody.containsKey("doneStatus") && !(this.commandBody.get("doneStatus") instanceof Boolean))
                Assertions.assertEquals("Failed Validation: doneStatus should be BOOLEAN",
                        body.getList("errorMessages").get(0));
        }

        // Check that the number of to-do's in the system did not change
        Assertions.assertEquals(beforeTodoCount, getTodoCount());

        this.commandBody.clear();
    }

    @Then("The call will fail with a 404 response code")
    public void assess404_fail(){
        // preliminary to-do count
        int beforeTodoCount = getTodoCount();

        if (this.methodType == MethodType.POST){
            final JsonPath body = given().body(this.commandBody).
                    when().post("/todos/" + this.specified_id).
                    then().
                    statusCode(404).
                    contentType(ContentType.JSON).
                    and().extract().body().jsonPath();

            if (this.commandBody.containsKey("title") && this.commandBody.get("title").equals(""))
                Assertions.assertEquals("Failed Validation: title : can not be empty", body.getList("errorMessages").get(0));
            else if (!this.commandBody.containsKey("title"))
                Assertions.assertEquals("title : field is mandatory", body.getList("errorMessages").get(0));
            else if (this.commandBody.containsKey("doneStatus") && !(this.commandBody.get("doneStatus") instanceof Boolean))
                Assertions.assertEquals("Failed Validation: doneStatus should be BOOLEAN",
                        body.getList("errorMessages").get(0));
            else {
                Assertions.assertEquals("No such todo entity instance with GUID or ID " + this.specified_id +" found",
                        body.getList("errorMessages").get(0));
            }
        }
        else if (this.methodType == MethodType.PUT){
            if (this.expectUTF){
                final Response body = given().body("").
                        when().put("/todos/" + this.specified_id).
                        then().
                        statusCode(404).
                        contentType("text/html;charset=utf-8").
                        and().extract().response();
            }
            else {
                final JsonPath body = given().body(this.commandBody).
                        when().put("/todos/" + this.specified_id).
                        then().
                        statusCode(404).
                        contentType(ContentType.JSON).
                        and().extract().body().jsonPath();

                Assertions.assertEquals("Invalid GUID for " + this.specified_id + " entity todo",
                        body.getList("errorMessages").get(0));
            }
        }
        else if (this.methodType == MethodType.DELETE){
            if (Objects.equals(this.specified_id, "")){
                final Response body = given().body("").
                        when().delete("/todos/").
                        then().
                        statusCode(404).
                        contentType("text/html;charset=utf-8").
                        and().extract().response();

                int todoCountPostDelete = getTodoCount();

                Assertions.assertEquals(beforeTodoCount, todoCountPostDelete);
                return;
            }


            // TODO: NOT HANDLING UTF OUTPUT CASE
            final JsonPath body = given().body("").
                    when().delete("/todos/" + this.specified_id).
                    then().
                    statusCode(404).
                    contentType(ContentType.JSON).
                    and().extract().body().jsonPath();
            Assertions.assertEquals("Could not find any instances with todos/" + this.specified_id,
                    body.getList("errorMessages").get(0));
        }

        // Check that the number of to-do's in the system did not change
        Assertions.assertEquals(beforeTodoCount, getTodoCount());

        if (this.commandBody != null) this.commandBody.clear();
    }

    @Then("The call will fail with a 405 response code")
    public void assess405_fail(){
        // preliminary to-do count
        int beforeTodoCount = getTodoCount();

        // TODO: UTF OUTPUT CASE --> HOW TO ASSESS UTF OUTPUT?

        // Check that the number of to-do's in the system did not change
        Assertions.assertEquals(beforeTodoCount, getTodoCount());

        if (this.commandBody != null) this.commandBody.clear();
    }

    @Then("Other than a successful response code nothing will be returned")
    public void return_nothing(){
        if (this.methodType == MethodType.HEAD){
            int beforeNumberOfTodos = getTodoCount();

            final Response body = given().body("").
                    accept("application/xml").
                    when().head("/todos?id=" + this.specified_id).
                    then().
                    statusCode(200).
                    contentType(ContentType.XML).
                    and().extract().response();

            String responseBody = body.body().asString();

            Assertions.assertEquals("", responseBody);

            Assertions.assertEquals(beforeNumberOfTodos, getTodoCount());
        }
    }




}
