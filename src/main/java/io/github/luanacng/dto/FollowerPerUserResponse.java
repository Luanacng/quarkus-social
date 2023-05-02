package io.github.luanacng.dto;

import java.util.List;

import lombok.Data;

@Data
public class FollowerPerUserResponse {

    private Integer followersCount;

    private List<FollowerResponse> content;
    
}
