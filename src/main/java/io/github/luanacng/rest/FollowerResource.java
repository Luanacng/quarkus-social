package io.github.luanacng.rest;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;

import io.github.luanacng.dto.FollowerPerUserResponse;
import io.github.luanacng.dto.FollowerRequest;
import io.github.luanacng.dto.FollowerResponse;
import io.github.luanacng.persistence.Follower;
import io.github.luanacng.repository.FollowerRepository;
import io.github.luanacng.repository.UserRepository;

@Path("/users/{userId}/followers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FollowerResource {

    private FollowerRepository repository;
    private UserRepository userRepository;
    
    @Inject
    public FollowerResource(FollowerRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @PUT
    @Transactional
    @Operation(
        summary = "Seguir um user",
        description = "Esta operação segue um user"
    )
    public Response followUser(@PathParam("userId") Long userId, FollowerRequest request) {

        // Tratamento para quando o follower tentar seguir ele mesmo
        if (userId.equals(request.getFollowerId())) {
            return Response.status(Response.Status.CONFLICT).entity("You can't follow yourself").build();
        }

        // Verifica se o usuário a ser seguido existe
        var user = userRepository.findById(userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }     

        var follower = userRepository.findById(request.getFollowerId());

        boolean follows = repository.follows(follower, user);

        // Verifica se o usuário já não é seguido
        if(!follows) {
            var entity = new Follower();
            entity.setUser(user);
            entity.setFollower(follower);
            repository.persist(entity);
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Operation(
        summary = "Listar followers",
        description = "Esta operação lista os followers de um user pelo userId"
    )
    public Response listFollowers(@PathParam("userId") Long userId){

        // Verifica se o usuário existe
        var user = userRepository.findById(userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }   

        var list = repository.findByUser(userId);
        FollowerPerUserResponse responseObject = new FollowerPerUserResponse();
        responseObject.setFollowersCount(list.size());

        List<FollowerResponse> followerList = list.stream().map( FollowerResponse::new ).collect(Collectors.toList());

        responseObject.setContent(followerList);

        return Response.ok(responseObject).build();
    }

    @DELETE
    @Operation(
        summary = "Unfollow user",
        description = "Esta operação deixa de seguir um user"
    )
    @Transactional
    public Response unfollowUser(@PathParam("userId") Long userId, @QueryParam("followerId") Long followerId){
  
        // Verifica se o usuário a ser deletado existe
        var user = userRepository.findById(userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }   

        repository.deleteByFollowerAndUser(followerId, userId);

        return Response.status(Response.Status.NO_CONTENT).build();
    }

}
