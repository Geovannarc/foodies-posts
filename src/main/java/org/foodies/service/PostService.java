package org.foodies.service;


import org.foodies.dto.MessageDTO;
import org.foodies.dto.PostDTO;
import org.foodies.model.Post;
import org.foodies.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
public class PostService {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private SendMessageService sendMessageService;

    private final String bucketName = "foodies-post-bucket";

    @Autowired
    private PostRepository postRepository;

    public void createPost(PostDTO post, String id) throws IOException {
        post.setUserId(decodeId(id));
        MultipartFile file = post.getMediaFile();
        post.setFileURL(uploadImage(file));
        savePost(post);
        sendMessage(post);
    }

    public static Long decodeId(String encodedId) {
        String decodedString = new String(Base64.getUrlDecoder().decode(encodedId));
        return Long.parseLong(decodedString);
    }

    private void sendMessage(PostDTO post) {
        MessageDTO message = MessageDTO.builder()
                .id(post.getUserId())
                .rating(post.getRating())
                .build();
        sendMessageService.sendMessage(message.toString());
    }

    private String uploadImage(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileKey = "uploads/" + System.currentTimeMillis() + extension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(fileKey)).toString();
    }

    private void savePost(PostDTO post) {
        postRepository.save(post);
    }

    public List<Post> getByUserName(String username) {
        return postRepository.findPostsByUsername(username);
    }

    public List<Post> getFeedPosts(String username) {
        return postRepository.findFeedPosts(username);
    }

    public PostDTO getPostById(Long id) {
        return postRepository.findPostById(id);
    }
}

