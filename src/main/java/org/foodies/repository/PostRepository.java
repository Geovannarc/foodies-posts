package org.foodies.repository;

import lombok.extern.log4j.Log4j2;
import org.foodies.dto.PostDTO;
import org.foodies.model.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;
import java.util.stream.Collectors;

@DynamoDbBean
@Repository
@Log4j2
public class PostRepository {

    @Autowired
    private DynamoDbClient dynamoDbClient;

    public void save(PostDTO post) {
        Map<String, AttributeValue> itemValues = new HashMap<>();
        itemValues.put("user_id", AttributeValue.builder().s(String.valueOf(post.getUserId())).build());
        itemValues.put("restaurant_id", AttributeValue.builder().s(String.valueOf(post.getRestaurantId())).build());
        itemValues.put("caption", AttributeValue.builder().s(post.getCaption()).build());
        itemValues.put("rating", AttributeValue.builder().n(String.valueOf(post.getRating())).build());
        itemValues.put("created_at", AttributeValue.builder().s(post.getDateCreation()).build());
        itemValues.put("tags", AttributeValue.builder().ss(post.getTags()).build());
        itemValues.put("media_file", AttributeValue.builder().s(post.getFileURL()).build());
        itemValues.put("likes", AttributeValue.builder().n(String.valueOf(0)).build());
        itemValues.put("username", AttributeValue.builder().s(post.getUsername()).build());
        itemValues.put("sort_key", AttributeValue.builder().s(post.getSortKey()).build());
        itemValues.put("post_id", AttributeValue.builder().s(post.getPostId()).build());
        itemValues.put("restaurant_name", AttributeValue.builder().s(post.getRestaurantName()).build());

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

        return new ArrayList<>(queryResponse.items().stream()
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
                    post.setSortKey(item.get("sort_key").s());
                    post.setRestaurantName(item.get("restaurant_name").s());
                    post.setDateCreation(item.get("created_at").s());
                    post.setUsername(item.get("username").s());
                    return post;
                })
                .toList());
    }

    public Map<String, Object> findFeedPosts(Long id, Map<String, AttributeValue> exclusiveStartKey) {
        QueryRequest queryRequest = QueryRequest.builder()
                .tableName("FollowTable")
                .keyConditionExpression("follower_id = :follower_id")
                .expressionAttributeValues(Map.of(":follower_id", AttributeValue.builder().s(String.valueOf(id)).build()))
                .build();

        QueryResponse queryResponse = dynamoDbClient.query(queryRequest);

        List<String> followedIds = new ArrayList<>(queryResponse.items().stream()
                .map(item -> item.get("following_id").s())
                .toList());
        Map<String, Object> postResponse;
        List<Post> allPosts;
        log.info("Followed IDs: {}", followedIds);
        if (followedIds.isEmpty()) {
            log.info("No posts to show");
            postResponse = findLastCreatedPosts(exclusiveStartKey);
        } else {
            followedIds.add(String.valueOf(id));
            postResponse = new HashMap<>();
            allPosts = new ArrayList<>();
            for (String userId : followedIds) {
                QueryRequest request = QueryRequest.builder()
                        .tableName("PostsTable")
                        .keyConditionExpression("user_id = :user_id")
                        .expressionAttributeValues(Map.of(":user_id", AttributeValue.builder().s(userId).build()))
                        .scanIndexForward(false)
                        .limit(10)
                        .exclusiveStartKey(exclusiveStartKey)
                        .build();

                QueryResponse response = dynamoDbClient.query(request);
                response.items().forEach(item -> {
                    Post post = new Post();
                    post.setUserId(item.get("user_id").s());
                    post.setPostId(item.get("post_id").s());
                    post.setMediaFile(item.get("media_file").s());
                    post.setCaption(item.get("caption").s());
                    post.setDateCreation(item.get("created_at").s());
                    post.setRestaurantName(item.get("restaurant_name").s());
                    post.setRating(Integer.parseInt(item.get("rating").n()));
                    post.setLikes(Integer.parseInt(item.get("likes").n()));
                    post.setTags(item.get("tags").ss());
                    post.setRestaurantId(item.get("restaurant_id").s());
                    post.setSortKey(item.get("sort_key").s());
                    post.setUsername(item.get("username").s());
                    allPosts.add(post);
                });
                Map<String, Object> serializableKey = null;
                if (response.hasLastEvaluatedKey()) {
                    serializableKey = response.lastEvaluatedKey().entrySet().stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    entry -> entry.getValue().s() != null ? entry.getValue().s() :
                                            entry.getValue().n() != null ? entry.getValue().n() :
                                                    entry.getValue().ss()
                            ));
                }
                postResponse.put("posts", allPosts);
                postResponse.put("exclusiveStartKey", serializableKey);
            }
        }
        return postResponse;
    }

    private Map<String, Object> findLastCreatedPosts(Map<String, AttributeValue> exclusiveStartKey) {
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName("PostsTable")
                .limit(10)
                .exclusiveStartKey(exclusiveStartKey)
                .build();
        log.info("Scan request: {}", scanRequest);
        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);
        log.info("Scan response: {}", scanResponse.count());
        List<Post> posts = scanResponse.items().stream()
                .map(item -> {
                    Post post = new Post();
                    post.setUserId(item.get("user_id").s());
                    post.setPostId(item.get("post_id").s());
                    post.setMediaFile(item.get("media_file").s());
                    post.setCaption(item.get("caption").s());
                    post.setDateCreation(item.get("created_at").s());
                    post.setRestaurantName(item.get("restaurant_name").s());
                    post.setRating(Integer.parseInt(item.get("rating").n()));
                    post.setLikes(Integer.parseInt(item.get("likes").n()));
                    post.setTags(item.get("tags").ss());
                    post.setRestaurantId(item.get("restaurant_id").s());
                    post.setSortKey(item.get("sort_key").s());
                    post.setUsername(item.get("username").s());
                    return post;
                })
                .collect(Collectors.toList());
        Map<String, Object> serializableKey = null;
        if (scanResponse.hasLastEvaluatedKey()) {
            serializableKey = scanResponse.lastEvaluatedKey().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().s() != null ? entry.getValue().s() :
                                    entry.getValue().n() != null ? entry.getValue().n() :
                                            entry.getValue().ss()
                    ));
        }
        Map<String, Object> postResponse = new HashMap<>();
        postResponse.put("posts", posts);
        postResponse.put("exclusiveStartKey", serializableKey);
        return postResponse;
    }

    public PostDTO findPostById(Long id) {
        String tableName = "PostsTable";

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .indexName("UserIdIndex")
                .keyConditionExpression("userId = :userId")
                .expressionAttributeValues(Map.of(":userId", AttributeValue.builder().s(String.valueOf(id)).build()))
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
