package com.yourname.dockertool.controller;

import com.yourname.dockertool.model.ImageInfo;
import com.yourname.dockertool.service.DockerApiService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class ImageController {

    private final DockerApiService dockerApiService;

    @Autowired
    public ImageController(DockerApiService dockerApiService) {
        this.dockerApiService = dockerApiService;
    }

    @PostMapping("/build-image")
    public ResponseEntity<Map<String, String>> buildImage(@RequestBody BuildImageRequest req) {
        try {
            Path workspace = Paths.get(System.getProperty("user.home"), ".dockertool", "workspaces", req.getProjectId());
            String imageId = dockerApiService.buildImage(workspace.toString(), req.getImageTag());
            return ResponseEntity.ok(Map.of("imageId", imageId, "imageTag", req.getImageTag()));
        } catch (IOException e) {
            log.error("Failed to build image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/images")
    public ResponseEntity<List<ImageInfo>> listImages() {
        List<ImageInfo> images = dockerApiService.listImages();
        return ResponseEntity.ok(images);
    }

    @Data
    public static class BuildImageRequest {
        private String projectId;
        private String imageTag;
    }
}