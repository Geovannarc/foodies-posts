package org.foodies.model;

import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.util.List;

@DynamoDbBean
@Setter
public class Post {

        private String postId;
        private String sortKey;
        private String userId;
        private String mediaFile;
        private String restaurantId;
        private String caption;
        private int rating;
        private List<String> tags;
        private String dateCreation;
        private int likes;

        @DynamoDbPartitionKey
        public String getPostId() {
                return postId;
        }

        @DynamoDbSortKey
        public String getSortKey() {
                return sortKey;
        }
}
