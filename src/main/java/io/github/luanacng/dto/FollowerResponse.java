package io.github.luanacng.dto;

import io.github.luanacng.persistence.Follower;
import lombok.Data;

@Data
public class FollowerResponse {
    
    private Long id;

    private String name;

    public FollowerResponse() {
    }

    public FollowerResponse(Follower follower) {
        this(follower.getFollower().getId(), follower.getFollower().getName());
    }

    public FollowerResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
