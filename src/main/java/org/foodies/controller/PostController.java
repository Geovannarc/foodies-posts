package org.foodies.controller;

import lombok.extern.log4j.Log4j2;
import org.foodies.dto.PostDTO;
import org.foodies.service.PostService;
import org.foodies.util.JwtUtil;
import org.foodies.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Log4j2
@RequestMapping("/post")
public class PostController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PostService postService;

    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseBuilder> savePost(@RequestHeader("Authorization") String token,
                                   @RequestParam("username") String username, @Validated @ModelAttribute PostDTO post) {
        if(!jwtUtil.validateToken(token, username))
            throw new RuntimeException("Invalid token");
        postService.createPost(post);
        return new ResponseEntity<>(new ResponseBuilder("Post saved successfully"), HttpStatus.CREATED);

    }
}
