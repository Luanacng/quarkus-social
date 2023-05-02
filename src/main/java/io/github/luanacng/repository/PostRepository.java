package io.github.luanacng.repository;

import javax.enterprise.context.ApplicationScoped;

import io.github.luanacng.persistence.Post;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class PostRepository implements PanacheRepository<Post>{
    
}
