package org.foodies.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class PostDTO {

    private String postId;
    private Long userId;
    private String username;
    @NotNull
    private Long restaurantId;
    @NotNull
    private String restaurantName;
    private String caption;
    @NotNull
    private int rating;
    private List<String> tags;
    @NotNull
    private MultipartFile mediaFile;
    private String fileURL;
    private Long likes;
    private Date dateCreation;
    private String sortKey;

    @SneakyThrows
    public String getDateCreation() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date.from(Instant.now()));
    }

    public void setPostId() {
        this.postId = this.userId + "_" + this.getSortKey();
    }

    public void setSortKey() {
        this.sortKey = String.valueOf(-System.currentTimeMillis());
    }
}
