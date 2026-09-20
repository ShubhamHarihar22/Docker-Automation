package com.yourname.dockertool.controller;

import com.yourname.dockertool.service.DockerApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/images/pull")
public class ImagePullController {

    private final DockerApiService dockerApiService;

    public ImagePullController(DockerApiService dockerApiService) {
        this.dockerApiService = dockerApiService;
    }

    @PostMapping
    public ResponseEntity<Void> pullImage(@RequestParam("imageName") String imageName) {
        return ResponseEntity.ok().build();
    }
}
