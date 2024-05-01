package today.tecktip.killbill.backend;

import static org.junit.Assert.*;

import java.beans.Transient;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 * Testing mini-assignment for demo 4.
 * @author caleb zea
 */
@SpringBootTest(webEnvironment =  SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CalebTests {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @LocalServerPort
    int port;

    private String apiKey;
    private String apiKey2;

    private UUID userId1;
    private UUID userId2;
    private static String messageId;

    @Before
    public void signIn() throws JsonProcessingException {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        // Our test user, 'test1', is guaranteed to exist before this on each installation.
        Response response = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("charset", "utf-8")
            .body("{\"username\": \"test1\", \"password\": \"password\"}")
            .when()
            .post("/auth");
            
        assertEquals(200, response.getStatusCode());

        // Parse the response
        JsonNode body = MAPPER.readTree(response.getBody().asString());

        apiKey = body.get("data").get("key").asText();
        assertNotNull(apiKey);
        assertTrue(apiKey.length() > 1);
        apiKey = "Bearer " + apiKey;

        // Test2 also exists
        response = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("charset", "utf-8")
            .body("{\"username\": \"test2\", \"password\": \"password\"}")
            .when()
            .post("/auth");
            
        assertEquals(200, response.getStatusCode());

        // Parse the response
        body = MAPPER.readTree(response.getBody().asString());

        apiKey2 = body.get("data").get("key").asText();
        assertNotNull(apiKey2);
        assertTrue(apiKey2.length() > 1);
        apiKey2 = "Bearer " + apiKey2;

        // Store users
        response = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("charset", "utf-8")
            .header("Authorization", apiKey)
            .when()
            .get("/users");
            
        assertEquals(200, response.getStatusCode());
        body = MAPPER.readTree(response.getBody().asString());
        System.err.println(body);
        userId1 = UUID.fromString(body.get("data").get("user").get("id").asText());

        response = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("charset", "utf-8")
            .header("Authorization", apiKey2)
            .when()
            .get("/users");
            
        assertEquals(200, response.getStatusCode());
        body = MAPPER.readTree(response.getBody().asString());
        userId2 = UUID.fromString(body.get("data").get("user").get("id").asText());

        //make them friends
        response = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("charset", "utf-8")
            .header("Authorization", apiKey)
            .body("{\"userId\": \"" + userId2 + "\"}")
            .when()
            .post("/friends");
            
        assertEquals(200, response.getStatusCode());

        response = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("charset", "utf-8")
            .header("Authorization", apiKey2)
            .body("{\"userId\": \"" + userId1 + "\"}")
            .when()
            .put("/friends");
            
        assertEquals(200, response.getStatusCode());
    }

    @After
    public void signOut() {

        Response response = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("charset", "utf-8")
            .header("Authorization", apiKey2)
            .when()
            .delete("/friends?userId=" + userId1);
            
        assertEquals(200, response.getStatusCode());

        // Free up the API keys
        response = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("charset", "utf-8")
            .header("Authorization", apiKey)
            .when()
            .delete("/auth?all=false");
            
        assertEquals(200, response.getStatusCode());

        response = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("charset", "utf-8")
            .header("Authorization", apiKey2)
            .when()
            .delete("/auth?all=false");
            
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testASendMessage() throws JsonProcessingException {
        Response response = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("charset", "utf-8")
            .header("Authorization", apiKey)
            .body("{\"toId\": \"" + userId2 + "\", \"message\": \"Hello Friend\"}")
            .when()
            .post("/dms");
        assertEquals(200, response.getStatusCode());

        Response response2 = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("charset", "utf-8")
            .header("Authorization", apiKey)
            .body("{\"toId\": \"" + userId2 + "\", \"message\": \"THIS IS A DECOY. YOUVYE FAILED\"}")
            .when()
            .post("/dms");
        assertEquals(200, response.getStatusCode());

        //Parse the response
        JsonNode body = MAPPER.readTree(response.getBody().asString());
        assertEquals(userId1.toString(), body.get("data").get("dm").get("fromId").asText());
        assertEquals(userId2.toString(), body.get("data").get("dm").get("toId").asText());
        assertEquals("Hello Friend", body.get("data").get("dm").get("message").asText());
        assertEquals("UNREAD", body.get("data").get("dm").get("state").asText());
        
        messageId = body.get("data").get("dm").get("id").asText();
        System.out.println(messageId);
    }

    @Test
    public void testBGetDmByID() throws JsonProcessingException {

        System.out.println(messageId);
        Response response = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("charset", "utf-8")
            .header("Authorization", apiKey)
            .when()
            .get("/dms?messageId=" + messageId);
            
        assertEquals(200, response.getStatusCode());

        // Parse the response
        JsonNode body = MAPPER.readTree(response.getBody().asString());
        assertEquals(userId1.toString(), body.get("data").get("dm").get("fromId").asText());
        assertEquals(userId2.toString(), body.get("data").get("dm").get("toId").asText());
        assertEquals("Hello Friend", body.get("data").get("dm").get("message").asText());
        assertEquals("UNREAD", body.get("data").get("dm").get("state").asText());
    }

    @Test
    public void testHGetNumUnread() throws JsonProcessingException {
        Response response = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("charset", "utf-8")
            .header("Authorization", apiKey2)
            .when()
            .get("/dms/count_unread?userId=" + userId2);

        assertEquals(200,response.getStatusCode());

        //Parse the response
        JsonNode body = MAPPER.readTree(response.getBody().asString());
        assertEquals(2, body.get("data").get("count").asInt());
    }

    @Test
    public void testIDeleteMessage() throws JsonProcessingException {
        Response response = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("charset", "utf-8")
            .header("Authorization", apiKey)
            .when()
            .delete("/dms?messageId=" + messageId);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testJDeleteAll() throws JsonProcessingException {
        Response response = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("charset", "utf-8")
            .header("Authorization", apiKey)
            .when()
            .delete("/dms/all?userId1=" + userId1 + "&userId2=" + userId2);
            
        assertEquals(200, response.getStatusCode());
    }

}
