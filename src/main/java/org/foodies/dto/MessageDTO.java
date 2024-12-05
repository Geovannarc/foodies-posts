package org.foodies.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MessageDTO {
    private Long id;
    private Integer rating;
    private Long userId;

    @Override
    public String toString() {
        return "{" +
                "\"id\":" + "\""+ id + "\""+
                ", \"rating\":" + "\"" + rating + "\""+
                ", \"userId\":" + "\"" + userId + "\""+
                '}';
    }
}
