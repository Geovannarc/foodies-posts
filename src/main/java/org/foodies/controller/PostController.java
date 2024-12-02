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

import java.io.IOException;

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
                                    @RequestParam("dXNlcklk") String id, @Validated @ModelAttribute PostDTO post) throws IOException {
        if(!jwtUtil.validateToken(token, post.getUsername()))
            throw new RuntimeException("Invalid token");
        postService.createPost(post, id);
        return new ResponseEntity<>(new ResponseBuilder(), HttpStatus.CREATED);

    }

    @GetMapping(value = "/getByUsername")
    public ResponseEntity<ResponseBuilder> getPostsByUsername(@RequestHeader("Authorization") String token,
                                   @RequestParam("username") String username) {
        if(!jwtUtil.validateToken(token, username))
            throw new RuntimeException("Invalid token");
        return new ResponseEntity<>(new ResponseBuilder(postService.getByUserName(username)), HttpStatus.OK);
    }

    @GetMapping("/get")
    public ResponseEntity<ResponseBuilder> getPosts(@RequestHeader("Authorization") String token,
                                                    @RequestParam("username") String username,
                                                    @RequestParam("dXNlcklk") String id) {
        if(!jwtUtil.validateToken(token, username))
            throw new RuntimeException("Invalid token");
        return new ResponseEntity<>(new ResponseBuilder(postService.getFeedPosts(id)), HttpStatus.OK);
    }

    @GetMapping("/getById")
    public ResponseEntity<ResponseBuilder> getPostById(@RequestHeader("Authorization") String token,
                                                       @RequestParam("username") String username,
                                                       @RequestParam("id") Long id) {
        if(!jwtUtil.validateToken(token, username))
            throw new RuntimeException("Invalid token");
        return new ResponseEntity<>(new ResponseBuilder(postService.getPostById(id)), HttpStatus.OK);
    }
}
