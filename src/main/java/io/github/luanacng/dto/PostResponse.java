package io.github.luanacng.dto;

import java.time.LocalDateTime;

import io.github.luanacng.persistence.Post;
import lombok.Data;

@Data
public class PostResponse {

    private String text;

    private LocalDateTime dateTime;

    public static PostResponse fromEntity(Post post) {
        PostResponse response = new PostResponse();
        response.setText(post.getText());
        response.setDateTime(post.getDateTime());
        return response;
    }
    
}
