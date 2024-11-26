package org.foodies.repository;

import lombok.extern.log4j.Log4j2;
import org.foodies.dto.PostDTO;
import org.foodies.model.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@DynamoDbBean
@Repository
@Log4j2
public class PostRepository {

    @Autowired
    private DynamoDbClient dynamoDbClient;

    public void save(PostDTO post) {
        Map<String, AttributeValue> itemValues = new HashMap<>();
        itemValues.put("post_id", AttributeValue.builder().s(post.getPostId()).build());
        itemValues.put("user_id", AttributeValue.builder().s(String.valueOf(post.getUserId())).build());
        itemValues.put("restaurant_id", AttributeValue.builder().s(String.valueOf(post.getRestaurantId())).build());
        itemValues.put("caption", AttributeValue.builder().s(post.getCaption()).build());
        itemValues.put("rating", AttributeValue.builder().n(String.valueOf(post.getRating())).build());
        itemValues.put("date_creation", AttributeValue.builder().s(post.getDateCreation()).build());
        itemValues.put("tags", AttributeValue.builder().ss(post.getTags()).build());
        itemValues.put("media_file", AttributeValue.builder().s(post.getFileURL()).build());
        itemValues.put("likes", AttributeValue.builder().n(String.valueOf(0)).build());
        itemValues.put("username", AttributeValue.builder().s(post.getUsername()).build());
        itemValues.put("sortKey", AttributeValue.builder().s(post.getSortKey()).build());

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName("PostsTable")
                .item(itemValues)
                .build();

        try {
            dynamoDbClient.putItem(putItemRequest);
            System.out.println("Post salvo com sucesso no DynamoDB");
        } catch (Exception ex) {
            throw new RuntimeException("Falha ao salvar post no DynamoDB: " + ex.getMessage());
        }
    }

    public List<Post> findPostsByUsername(String username) {
        String tableName = "PostsTable";

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .indexName("UsernameIndex")
                .keyConditionExpression("username = :username")
                .expressionAttributeValues(Map.of(":username", AttributeValue.builder().s(username).build()))
                .build();

        QueryResponse queryResponse = dynamoDbClient.query(queryRequest);

        return queryResponse.items().stream()
                .map(item -> {
                    Post post = new Post();
                    post.setUserId(item.get("user_id").s());
                    post.setPostId(item.get("post_id").s());
                    post.setRestaurantId(item.get("restaurant_id").s());
                    post.setMediaFile(item.get("media_file").s());
                    post.setCaption(item.get("caption").s());
                    post.setRating(Integer.parseInt(item.get("rating").n()));
                    post.setLikes(Integer.parseInt(item.get("likes").n()));
                    post.setTags(item.get("tags").ss());
                    return post;
                })
                .collect(Collectors.toList());
    }

    public List<Post> findFeedPosts(String username) {
        String tableName = "FollowingTable";

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("username = :username")
                .expressionAttributeValues(Map.of(":username", AttributeValue.builder().s(username).build()))
                .build();

        QueryResponse queryResponse = dynamoDbClient.query(queryRequest);

        return queryResponse.items().stream()
                .map(item -> {
                    Post post = new Post();
                    post.setUserId(item.get("user_id").s());
                    post.setPostId(item.get("post_id").s());
                    post.setRestaurantId(item.get("restaurant_id").s());
                    post.setMediaFile(item.get("media_file").s());
                    post.setCaption(item.get("caption").s());
                    post.setRating(Integer.parseInt(item.get("rating").n()));
                    return post;
                })
                .collect(Collectors.toList());
    }

    public PostDTO findPostById(Long id) {
        String tableName = "PostsTable";

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("post_id = :post_id")
                .expressionAttributeValues(Map.of(":post_id", AttributeValue.builder().s(String.valueOf(id)).build()))
                .build();

        QueryResponse queryResponse = dynamoDbClient.query(queryRequest);

        return queryResponse.items().stream()
                .map(item -> {
                    PostDTO post = new PostDTO();
                    post.setUserId(Long.parseLong(item.get("user_id").s()));
                    post.setPostId(item.get("post_id").s());
                    post.setRestaurantId(Long.parseLong(item.get("restaurant_id").s()));
                    post.setFileURL(item.get("media_file").s());
                    post.setCaption(item.get("caption").s());
                    post.setRating(Integer.parseInt(item.get("rating").n()));
                    return post;
                })
                .findFirst()
                .orElse(null);
    }
}
