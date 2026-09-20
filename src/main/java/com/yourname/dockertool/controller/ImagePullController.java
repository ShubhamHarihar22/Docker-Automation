package com.yourname.dockertool.controller;

import com.yourname.dockertool.service.DockerApiService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/images")
public class ImagePullController {

    private final DockerApiService dockerApiService;

    public ImagePullController(DockerApiService dockerApiService) {
        this.dockerApiService = dockerApiService;
    }

    @PostMapping("/pull")
    public ResponseEntity<Map<String, String>> pullImage(@RequestBody PullImageRequest request) {
        String tag = request.getTag() == null || request.getTag().isBlank()
                ? "latest" : request.getTag();
        try {
            dockerApiService.pullImage(request.getImageName(), tag);
            return ResponseEntity.ok(Map.of(
                    "status", "pulled",
                    "image", request.getImageName() + ":" + tag));
        } catch (IOException e) {
            log.error("Failed to pull image {}", request.getImageName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Map<String, String>>> popularImages() {
        return ResponseEntity.ok(List.of(
                image("postgres", "16", "PostgreSQL relational database"),
                image("mysql", "8", "MySQL relational database"),
                image("redis", "7", "In-memory key-value store"),
                image("nginx", "latest", "Web server and reverse proxy"),
                image("mongo", "7", "MongoDB document database"),
                image("node", "20-slim", "Node.js runtime"),
                image("python", "3.12-slim", "Python runtime"),
                image("ubuntu", "22.04", "Ubuntu base OS image")
        ));
    }

    private Map<String, String> image(String name, String tag, String description) {
        return Map.of("name", name, "tag", tag, "description", description);
    }

    @Data
    public static class PullImageRequest {
        private String imageName;
        private String tag;
    }
}