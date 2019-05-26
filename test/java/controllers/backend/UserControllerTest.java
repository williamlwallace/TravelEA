package controllers.backend;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.BAD_REQUEST;
import static play.test.Helpers.DELETE;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.UNAUTHORIZED;
import static play.test.Helpers.route;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import models.User;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

public class UserControllerTest extends ControllersTest {

    /**
     * Runs evolutions before each test These evolutions are found in conf/test/(whatever), and
     * should contain minimal sql data needed for tests
     */
    @Before
    public void runEvolutions() {
        applyEvolutions("test/user/");
    }


    @Test
    public void searchUsers() throws IOException {
        //Create request to GET all users
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/user/search");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Deserialize result to list of users
        List<User> users = Arrays
            .asList(new ObjectMapper().readValue(Helpers.contentAsString(result), User[].class));

        // Check that list has exactly one result
        assertEquals(1, users.size());

        // Check that the user is what we expect having run destination test evolution
        User user = users.get(0);
        assertEquals(Long.valueOf(2), user.id);
        assertEquals("bob@gmail.com", user.username);
        assertEquals("password", user.password); //cat
        assertEquals("salt", user.salt);
    }

    @Test
    public void createValidUser() throws IOException {
        // Create new json object node
        ObjectNode node = Json.newObject();
        node.put("username", "catsinhats123@live.com");
        node.put("password", "MeowMeow123");

        // Create request to create a new user
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node)
            .uri("/api/user");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Check a success message was sent
        String message = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), String.class);
        assertEquals("Success", message);
    }

    @Test
    public void createUserTakenUsername() {
        // Create new json object node with registered email
        ObjectNode node = Json.newObject();
        node.put("username", "dave@gmail.com");
        node.put("password", "0hYeahYeah");

        // Create request to create a new user
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node)
            .uri("/api/user");

        // Get result and check a 400 was sent back
        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void createUserInvalidUsername() {
        // Create new json object node
        ObjectNode node = Json.newObject();
        node.put("username", "catsanddogs");
        node.put("password", "0hYeahYeah");

        // Create request to create a new user
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node)
            .uri("/api/user");

        // Get result and check a 400 was sent back
        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void createUserInvalidPassword() {
        // Create new json object node
        ObjectNode node = Json.newObject();
        node.put("username", "catsinhats@live.com");
        node.put("password", "a");

        // Create request to create a new user
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node)
            .uri("/api/user");

        // Get result and check a 400 was sent back
        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());
    }


    @Test
    public void createUserEmptyFields() throws IOException {
        // Create new json object node
        ObjectNode node = Json.newObject();
        // Fields missing: district, name, _type
        node.put("username", "");
        node.put("password", "");

        // Create request to create a new user
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node)
            .uri("/api/user");

        // Get result and check it was bad request
        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());

        // Get error response
        HashMap<String, String> response = new ObjectMapper()
            .readValue(Helpers.contentAsString(result),
                new TypeReference<HashMap<String, String>>() {
                });

        // Expected error messages
        HashMap<String, String> expectedMessages = new HashMap<>();
        expectedMessages.put("username", "Username field must be present");
        expectedMessages.put("password", "Password field must be present");

        // Check all error messages were present
        for (String key : response.keySet()) {
            assertEquals(expectedMessages.get(key), response.get(key));
        }
    }

    @Test
    public void validLogin() {
        // Create new new user, so password is hashed
        ObjectNode node = Json.newObject();
        node.put("username", "dave@gmail.com");
        node.put("password", "cats");

        // Create request to login
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node)
            .uri("/api/login");

        // Get result and check OK was sent back
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void loginInvalidPassword() {
        //Invalid password
        ObjectNode node2 = Json.newObject();
        node2.put("username", "dave@gmail.com");
        node2.put("password", "yeetyeet");

        // Create request to login
        Http.RequestBuilder request2 = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node2)
            .uri("/api/login");

        // Get result and check 401 was sent back
        Result result2 = route(fakeApp, request2);
        assertEquals(UNAUTHORIZED, result2.status());
    }

    @Test
    public void loginInvalidUsername() {
        //Invalid password
        ObjectNode node2 = Json.newObject();
        node2.put("username", "steve@gmail.com");
        node2.put("password", "password");

        // Create request to login
        Http.RequestBuilder request2 = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node2)
            .uri("/api/login");

        // Get result and check 401 was sent back
        Result result2 = route(fakeApp, request2);
        assertEquals(UNAUTHORIZED, result2.status());
    }

    @Test
    public void logout() {
        // Create request to login
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .uri("/api/logout");

        // Get result and check the user was redirected
        Result result = route(fakeApp, request);
        assertEquals(401, result.status());
    }

    @Test
    public void deleteValidUser() {
        // Create request to delete newly created user
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(DELETE)
            .cookie(adminAuthCookie)
            .uri("/api/user/2");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void deleteInvalidUser() {
        // Create request to delete a user that does not exist
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(DELETE)
            .cookie(adminAuthCookie)
            .uri("/api/user/12");

        // Get result and check it failed
        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());
    }

}