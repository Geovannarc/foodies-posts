package org.foodies.repository;


import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

public class Comment {

        private String postId;
        private String sortKey;
        private String commentId;
        private String userId;
        private String content;
        private String dateCreation;

        @DynamoDbPartitionKey
        public String getPostId() {
                return postId;
        }

        @DynamoDbSortKey
        public String getSortKey() {
                return sortKey;
        }
}
