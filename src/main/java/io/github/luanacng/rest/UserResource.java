package io.github.luanacng.rest;

import java.util.Set;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;

import io.github.luanacng.dto.CreateUserRequest;
import io.github.luanacng.dto.ResponseError;
import io.github.luanacng.persistence.User;
import io.github.luanacng.repository.UserRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    private UserRepository repository;
    private Validator validator;

    @Inject
    public UserResource(UserRepository repository, Validator validator){
        this.repository = repository;
        this.validator = validator;
    }

    @POST
    @Operation(
        summary = "Criar usuário",
        description = "Esta requisição cria um usuário"
    )
    @Transactional
    public Response createUser( CreateUserRequest userRequest) {

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(userRequest);
        if(!violations.isEmpty()){
            return ResponseError.createFromValidation(violations).withStatusCode(ResponseError.UNPROCESSABLE_ENTITY_STATUS);
        }

        User user = new User();
        user.setAge(userRequest.getAge());
        user.setName(userRequest.getName());

        repository.persist(user);

        return Response.status(Response.Status.CREATED.getStatusCode()).entity(user).build();
    }
    
    @GET
    @Operation(
        summary = "Listar usuários",
        description = "Esta requisição lista todos os usuários"
    )
    public Response listAllUsers() {
        PanacheQuery<User> query = repository.findAll();
        return Response.ok(query.list()).build();
    }

    @DELETE
    @Operation(
        summary = "Deletar usuário",
        description = "Esta requisição deleta um usuário pelo id"
    )
    @Path("{id}")
    @Transactional
    public Response deleteUser(@PathParam("id") Long id) {
        User user = repository.findById(id);

        if(user != null){
            repository.delete(user);
            return Response.noContent().build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
        
    }

    @PUT
    @Operation(
        summary = "Atualizar usuário",
        description = "Esta requisição atualiza um usuário pelo id"
    )
    @Path("{id}")
    @Transactional
    public Response updateUser(@PathParam("id") Long id, CreateUserRequest userData) {
        User user = repository.findById(id);

        if (user != null) {
            user.setAge(userData.getAge());
            user.setName(userData.getName());
            return Response.ok(user).build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }


}
