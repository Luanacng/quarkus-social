package io.github.luanacng.repository;

import javax.enterprise.context.ApplicationScoped;

import io.github.luanacng.persistence.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
    
}
