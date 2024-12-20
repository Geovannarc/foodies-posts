package org.foodies.model;

import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.util.List;

@DynamoDbBean
@Setter
@Getter
public class Post {

        private String userId;
        private String postId;
        private String mediaFile;
        private String username;
        private String restaurantId;
        private String caption;
        private int rating;
        private List<String> tags;
        private String dateCreation;
        private String restaurantName;
        private int likes;
        private Long sortKey;
        private boolean liked;

        @DynamoDbPartitionKey
        public String getUserid() {
                return userId;
        }

        @DynamoDbSortKey
        public Long getSortKey() {
                return sortKey;
        }
}
