package today.tecktip.killbill.backend;

import static org.junit.Assert.*;

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
 * @author cs
 */
@SpringBootTest(webEnvironment =  SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CamdenTests {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @LocalServerPort
    int port;

    private String apiKey;
    private String apiKey2;

    private UUID userId1;
    private UUID userId2;

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

        // Test2 also exists (accept friend).
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
    }

    @After
    public void signOut() {
        // Free up the API keys
        Response response = RestAssured.given()
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
    public void testAAddFriend() throws JsonProcessingException {
        // test2 also exists.
        Response response = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("charset", "utf-8")
            .header("Authorization", apiKey)
            .body("{\"userId\": \"" + userId2 + "\"}")
            .when()
            .post("/friends");
            
        assertEquals(200, response.getStatusCode());

        // Parse the response
        JsonNode body = MAPPER.readTree(response.getBody().asString());
        assertEquals(userId1.toString(), body.get("data").get("friend").get("fromId").asText());
        assertEquals(userId2.toString(), body.get("data").get("friend").get("toId").asText());
        assertEquals("INVITED", body.get("data").get("friend").get("state").asText());
    }

    @Test
    public void testBGetFriend() throws JsonProcessingException {
        Response response = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("charset", "utf-8")
            .header("Authorization", apiKey)
            .when()
            .get("/friends?userId1=" + userId1 + "&userId2=" + userId2);
            
        assertEquals(200, response.getStatusCode());

        // Parse the response
        JsonNode body = MAPPER.readTree(response.getBody().asString());
        assertEquals(userId1.toString(), body.get("data").get("friend").get("fromId").asText());
        assertEquals(userId2.toString(), body.get("data").get("friend").get("toId").asText());
        assertEquals("INVITED", body.get("data").get("friend").get("state").asText());
    }

    @Test
    public void testCListFriends() throws JsonProcessingException {
        Response response = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("charset", "utf-8")
            .header("Authorization", apiKey)
            .when()
            .get("/friends/list?userId=" + userId1);
            
        assertEquals(200, response.getStatusCode());

        // Parse the response
        JsonNode body = MAPPER.readTree(response.getBody().asString());
        assertEquals(JsonNodeType.ARRAY, body.get("data").get("friends").getNodeType());
        body.get("data").get("friends").forEach(
            node -> {
                assertEquals(userId1.toString(), node.get("fromId").textValue());
                assertEquals(userId2.toString(), node.get("toId").textValue());
                assertEquals("INVITED", node.get("state").textValue());
            }
        );
    }

    @Test
    public void testDAcceptFriend() throws JsonProcessingException {
        Response response = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("charset", "utf-8")
            .header("Authorization", apiKey2)
            .body("{\"userId\": \"" + userId1 + "\"}")
            .when()
            .put("/friends");
            
        assertEquals(200, response.getStatusCode());

        // Parse the response
        JsonNode body = MAPPER.readTree(response.getBody().asString());
        assertEquals(userId1.toString(), body.get("data").get("friend").get("fromId").asText());
        assertEquals(userId2.toString(), body.get("data").get("friend").get("toId").asText());
        assertEquals("FRIENDS", body.get("data").get("friend").get("state").asText());
    }

    @Test
    public void testEDeleteFriend() throws JsonProcessingException {
        Response response = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("charset", "utf-8")
            .header("Authorization", apiKey2)
            .when()
            .delete("/friends?userId=" + userId1);
            
        assertEquals(200, response.getStatusCode());

        // Parse the response (returns friend link that was deleted)
        JsonNode body = MAPPER.readTree(response.getBody().asString());
        assertEquals(userId1.toString(), body.get("data").get("friend").get("fromId").asText());
        assertEquals(userId2.toString(), body.get("data").get("friend").get("toId").asText());
        assertEquals("FRIENDS", body.get("data").get("friend").get("state").asText());
    }

}
