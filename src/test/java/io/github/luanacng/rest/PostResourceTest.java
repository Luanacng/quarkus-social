package io.github.luanacng.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.luanacng.dto.CreatePostRequest;
import io.github.luanacng.persistence.Follower;
import io.github.luanacng.persistence.Post;
import io.github.luanacng.persistence.User;
import io.github.luanacng.repository.FollowerRepository;
import io.github.luanacng.repository.PostRepository;
import io.github.luanacng.repository.UserRepository;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;

@QuarkusTest
@TestHTTPEndpoint(PostResource.class)
public class PostResourceTest {

    @Inject
    UserRepository userRepository;

    @Inject
    FollowerRepository followerRepository;

    @Inject
    PostRepository postRepository;

    Long userID;
    Long userNotFollowerId;
    Long userFollowerId;

    @BeforeEach
    @Transactional
    public void setUP(){
        // Usuario padrão dos testes
        var user = new User();
        user.setAge(30);
        user.setName("Leticya");
        userRepository.persist(user);
        userID = user.getId();

        // Postagem criada para o usuario
        Post post = new Post();
        post.setText("Hello");
        post.setUser(user);
        postRepository.persist(post);

        // usuario que não segue ninguém
        var userNotFollower = new User();
        userNotFollower.setAge(20);
        userNotFollower.setName("Lua");
        userRepository.persist(userNotFollower);
        userNotFollowerId = userNotFollower.getId();

        // usuario seguidor
        var userFollower = new User();
        userFollower.setAge(25);
        userFollower.setName("Gama");
        userRepository.persist(userFollower);
        userFollowerId = userFollower.getId();

        Follower follower = new Follower();
        follower.setUser(user);
        follower.setFollower(userFollower);
        followerRepository.persist(follower);

    }

    @Test
    @DisplayName("should create a post for a user")
    public void createPostTest() {

        var postRequest = new CreatePostRequest(); 
        postRequest.setText("Some text");

        given()
            .contentType(ContentType.JSON)
            .body(postRequest)
            .pathParam("userId", userID)
        .when()
            .post()
        .then()
            .statusCode(201);
            
    }

    
    @Test
    @DisplayName("should return 404 when trying to make a post for an inexistent user")
    public void postForAnInexistentUserTest() {

        var postRequest = new CreatePostRequest(); 
        postRequest.setText("Some text");

        var inexistentUserId = 999;

        given()
            .contentType(ContentType.JSON)
            .body(postRequest)
            .pathParam("userId", inexistentUserId)
        .when()
            .post()
        .then()
            .statusCode(404);
            
    }

    @Test
    @DisplayName("should return 404 when user doens't exist")
    public void listPostUserNotFoundTest() {

        var inexistentUserId = 999;

        given()
            .pathParam("userId", inexistentUserId)
        .when()
            .get()
        .then()
            .statusCode(404);

    }

    @Test
    @DisplayName("should return 400 when followerId is not present")
    public void listPostFollowerHeaderNotSendTest() {

        given()
            .pathParam("userId", userID)
        .when()
            .get()
        .then()
            .statusCode(400)
            .body(Matchers.is("You forgot the header followerId"));

    }

    @Test
    @DisplayName("should return 400 when follower doens't exist")
    public void listPostFollowerNotFoundTest() {

        var inexistentFollowerId = 999;

        given()
            .pathParam("userId", userID)
            .header("followerId", inexistentFollowerId)
        .when()
            .get()
        .then()
            .statusCode(400)
            .body(Matchers.is("Inexistent followerId"));

    }

    @Test
    @DisplayName("should return 403 when follower isn't a follower")
    public void listPostNotAFollowerTest() {

        given()
            .pathParam("userId", userID)
            .header("followerId", userNotFollowerId)
        .when()
            .get()
        .then()
            .statusCode(403)
            .body(Matchers.is("You can't see these posts."));

    }

    @Test
    @DisplayName("should return posts")
    public void listPostsTest() {

        given()
            .pathParam("userId", userID)
            .header("followerId", userFollowerId)
        .when()
            .get()
        .then()
            .statusCode(200)
            .body("size()", Matchers.is(1));

    }
}
