package com.yourname.dockertool.controller;

import com.yourname.dockertool.model.DetectedStack;
import com.yourname.dockertool.service.DockerfileGeneratorService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class DockerfileController {

    private final DockerfileGeneratorService generator;

    @Autowired
    public DockerfileController(DockerfileGeneratorService generator) {
        this.generator = generator;
    }

    @PostMapping("/generate-dockerfile")
    public ResponseEntity<Map<String, String>> generate(@RequestBody GenerateRequest req) {
        try {
            DetectedStack stack = DetectedStack.valueOf(req.getDetectedStack());
            String dockerfile = generator.generate(stack, req.getPort());
            Path workspace = Paths.get(System.getProperty("user.home"), ".dockertool", "workspaces", req.getProjectId());
            Files.createDirectories(workspace);
            Path dockerfilePath = workspace.resolve("Dockerfile");
            Files.writeString(dockerfilePath, dockerfile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return ResponseEntity.ok(Map.of("dockerfile", dockerfile));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            log.error("Failed to generate or write Dockerfile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to generate Dockerfile"));
        }
    }

    @Data
    public static class GenerateRequest {
        private String projectId;
        private String detectedStack;
        private int port;
    }
}