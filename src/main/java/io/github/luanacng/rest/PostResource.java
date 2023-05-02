package io.github.luanacng.rest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;

import io.github.luanacng.dto.CreatePostRequest;
import io.github.luanacng.dto.PostResponse;
import io.github.luanacng.persistence.Post;
import io.github.luanacng.persistence.User;
import io.github.luanacng.repository.FollowerRepository;
import io.github.luanacng.repository.PostRepository;
import io.github.luanacng.repository.UserRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;

@Path("/users/{userId}/posts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PostResource {

    private UserRepository userRepository;
    private PostRepository postRepository;
    private FollowerRepository followerRepository;

    @Inject
    public PostResource(UserRepository userRepository, PostRepository postRepository, FollowerRepository followerRepository){
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.followerRepository = followerRepository;
    }

    @POST
    @Transactional
    @Operation(
        summary = "Criar post",
        description = "Esta requisição cria um post"
    )
    public Response savePost(@PathParam("userId") Long userId, CreatePostRequest request){
        User user = userRepository.findById(userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Post post = new Post();
        post.setText(request.getText());
        post.setUser(user);
        post.setDateTime(LocalDateTime.now());
        postRepository.persist(post);

        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Operation(
        summary = "Listar todos os posts de um user",
        description = "Esta requisição lista todos os posts"
    )
    public Response listPosts(@PathParam("userId") Long userId, @HeaderParam("followerId") Long followerId){

        // Verifica se o usuário existe
        User user = userRepository.findById(userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Verifica se o Header foi passado
        if(followerId == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("You forgot the header followerId").build();
        }

        // Recupera o follower
        User follower = userRepository.findById(followerId);

        // Verifica se o follower existe
        if(follower == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Inexistent followerId").build();
        }

        // Verifica se o usuário é seguido pelo follower
        boolean follows = followerRepository.follows(follower, user);
        if (!follows) {
            // Não permite listar os posts caso o user não for seguido
            return Response.status(Response.Status.FORBIDDEN).entity("You can't see these posts.").build();
        }

        PanacheQuery<Post> query = postRepository.find("user", Sort.by("dateTime", Sort.Direction.Descending), user);

        List<Post> list = query.list();

        List<PostResponse> postResponseList = list.stream().map(post -> PostResponse.fromEntity(post)).collect(Collectors.toList());

        return Response.ok(postResponseList).build();
    }
    
}
