package org.foodies.service;


import org.foodies.dto.PostDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.List;

@Service
public class PostService {

    @Autowired
    private S3AsyncClient s3Client;
    @Autowired
    private DynamoDbAsyncClient dynamoClient;

    private final String bucketName = "foodies-post-bucket";

    public String create(PostDTO post) {
        return null;
    }

    public void createPost(PostDTO post) {
        List<CompletableFuture<String>> uploadFutures = post.getFiles().stream()
                .map(file -> {
                    try {
                        return uploadImageAsync(file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        CompletableFuture.allOf(uploadFutures.toArray(new CompletableFuture[0]))
                .thenCompose(v -> {
                    List<String> imageUrls = uploadFutures.stream()
                            .map(CompletableFuture::join)
                            .toList();
                    post.setMediaFiles(imageUrls);
                    return savePostAsync(post);
                });
    }

    private CompletableFuture<String> uploadImageAsync(MultipartFile file) throws IOException {
        String fileKey = "images/" + file.getName() + "_" + System.currentTimeMillis();
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        return s3Client.putObject(putObjectRequest, AsyncRequestBody.fromFile(file.getResource().getFile()))
                .thenApply(response -> s3Client.utilities().getUrl(builder -> builder.bucket(bucketName)
                        .key(fileKey)).toString())
                .exceptionally(ex -> {
                    System.err.println("Falha no upload: " + ex.getMessage());
                    return null;
                });
    }

    private CompletableFuture<Void> savePostAsync(PostDTO post) {
        Map<String, AttributeValue> itemValues = new HashMap<>();
        itemValues.put("post_id", AttributeValue.builder().s(getPostId(post.getUserId())).build());
        itemValues.put("user_id", AttributeValue.builder()
                .l(AttributeValue.fromS(String.valueOf(post.getUserId()))).build());
        itemValues.put("restaurant_id", AttributeValue.builder().s(String.valueOf(post.getRestaurantId())).build());
        itemValues.put("caption", AttributeValue.builder().s(post.getCaption()).build());
        itemValues.put("rating", AttributeValue.builder().n(String.valueOf(post.getRating())).build());
        itemValues.put("date_creation", AttributeValue.builder().s(post.getDateCreation()).build());
        itemValues.put("tags", AttributeValue.builder().ss(post.getTags()).build());
        itemValues.put("media_files", AttributeValue.builder().ss(post.getMediaFiles()).build());
        itemValues.put("likes", AttributeValue.builder().n(String.valueOf(post.getLikes())).build());

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName("PostsTable")
                .item(itemValues)
                .build();

        return dynamoClient.putItem(putItemRequest)
                .thenRun(() -> System.out.println("Post salvo com sucesso no DynamoDB"))
                .exceptionally(ex -> {
                    System.err.println("Falha ao salvar o post: " + ex.getMessage());
                    return null;
                });
    }

    private String getPostId(Long userId) {
        return userId + "_" + System.currentTimeMillis();
    }
}

