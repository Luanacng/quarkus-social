package io.github.luanacng.rest;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.luanacng.dto.FollowerRequest;
import io.github.luanacng.persistence.Follower;
import io.github.luanacng.persistence.User;
import io.github.luanacng.repository.FollowerRepository;
import io.github.luanacng.repository.UserRepository;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestHTTPEndpoint(FollowerResource.class)
public class FollowerResourceTest {

    @Inject
    UserRepository userRepository;

    @Inject
    FollowerRepository followerRepository;

    Long userID;
    Long followerID;

    @BeforeEach
    @Transactional
    void setUp() {
        // Usuario padrão dos testes
        var user = new User();
        user.setAge(30);
        user.setName("Leticya");
        userRepository.persist(user);
        userID = user.getId();


        // o seguidor
        var userFollower = new User();
        userFollower.setAge(25);
        userFollower.setName("Gama");
        userRepository.persist(userFollower);
        followerID = userFollower.getId();

        
        Follower followerEntity = new Follower();
        followerEntity.setUser(user);
        followerEntity.setFollower(userFollower);
        followerRepository.persist(followerEntity);

    }

    @Test
    @DisplayName("should return 409 when followerId is equal to userId")
    public void sameUserAsFollowerTest() {
        
        var body = new FollowerRequest();
        body.setFollowerId(userID);

        given()
            .contentType(ContentType.JSON)
            .body(body)
            .pathParam("userId", userID)
        .when()
            .put()
        .then()
            .statusCode(409)
            .body(Matchers.is("You can't follow yourself"));

    }

    @Test
    @DisplayName("should return 404 when follow a userId doens't exist")
    public void userNotFoundWhenTryingToFollowTest() {
        
        var body = new FollowerRequest();
        body.setFollowerId(userID);

        var inexistentUserId = 999;

        given()
            .contentType(ContentType.JSON)
            .body(body)
            .pathParam("userId", inexistentUserId)
        .when()
            .put()
        .then()
            .statusCode(404);

    }
 
    @Test
    @DisplayName("should follow a user")
    public void followUserTest() {
        
        var body = new FollowerRequest();
        body.setFollowerId(followerID);

        given()
            .contentType(ContentType.JSON)
            .body(body)
            .pathParam("userId", userID)
        .when()
            .put()
        .then()
            .statusCode(204);

    }

    @Test
    @DisplayName("should return 404 on list user followers and userId doens't exist")
    public void userNotFoundWhenListFollowersTest() {

        var inexistentUserId = 999;

        given()
            .contentType(ContentType.JSON)
            .pathParam("userId", inexistentUserId)
        .when()
            .get()
        .then()
            .statusCode(404);

    }
    
    @Test
    @DisplayName("should list a user followers")
    public void listFollowersTest() {

        var response = 
            given()
                .contentType(ContentType.JSON)
                .pathParam("userId", userID)
            .when()
                .get()
            .then()
                .extract().response();

        var followersCount = response.jsonPath().get("followersCount");
        var followersContent = response.jsonPath().getList("content");

        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
        assertEquals(1, followersCount);
        assertEquals(1, followersContent.size());

    }

    @Test
    @DisplayName("should return 404 on unfollow user and userId doens't exist")
    public void userNotFoundWhenUnfollowingAUserTest() {

        var inexistentUserId = 999;

        given()
            .contentType(ContentType.JSON)
            .pathParam("userId", inexistentUserId)
            .queryParam("followerId", followerID)
        .when()
            .delete()
        .then()
            .statusCode(404);

    }

    @Test
    @DisplayName("should unfollow a user")
    public void unfollowingAUserTest() {

        given()
            .pathParam("userId", userID)
            .queryParam("followerId", followerID)
        .when()
            .delete()
        .then()
            .statusCode(204);

    }
}