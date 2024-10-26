package org.foodies.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.web.multipart.MultipartFile;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class PostDTO {

    private Long userId;
    private String username;
    private Long restaurantId;
    private String caption;
    private int rating;
    private List<String> tags;
    private List<MultipartFile> files;
    private List<String> mediaFiles;
    private Long likes;
    private Date dateCreation;

    @SneakyThrows
    public String getDateCreation() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dateCreation);
    }
}
