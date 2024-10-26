package org.foodies.repository;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class PostMedia {

        private String postId;
        private String fileId;
        private String fileType;
        private String fileUrl;

        @DynamoDbPartitionKey
        public String getPostId() {
            return postId;
        }

}
