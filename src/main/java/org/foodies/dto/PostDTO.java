package org.foodies.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class PostDTO {

    private Long userId;
    private String username;
    @NotNull
    private Long restaurantId;
    private String caption;
    @NotNull
    private int rating;
    private List<String> tags;
    @NotNull
    private MultipartFile mediaFile;
    private String fileURL;
    private Long likes;
    private Date dateCreation;

    @SneakyThrows
    public String getDateCreation() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dateCreation);
    }
}
