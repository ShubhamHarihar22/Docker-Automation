package com.yourname.dockertool.controller;

import com.yourname.dockertool.model.ContainerInfo;
import com.yourname.dockertool.service.DockerApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class ContainerController {

    private final DockerApiService dockerApiService;

    public ContainerController(DockerApiService dockerApiService) {
        this.dockerApiService = dockerApiService;
    }

    @GetMapping("/containers")
    public ResponseEntity<List<ContainerInfo>> listContainers() {
        return ResponseEntity.ok(dockerApiService.listContainers());
    }

    @PostMapping("/containers/start")
    public ResponseEntity<?> startContainer(@RequestBody Map<String, Object> body) {
        try {
            String imageTag = (String) body.get("imageTag");
            int hostPort = (int) body.get("hostPort");
            int containerPort = (int) body.get("containerPort");
            String containerId = dockerApiService.startContainer(imageTag, hostPort, containerPort);
            Map<String, String> response = new HashMap<>();
            response.put("containerId", containerId);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Failed to start container", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/containers/{id}/stop")
    public ResponseEntity<?> stopContainer(@PathVariable String id) {
        try {
            dockerApiService.stopContainer(id);
            Map<String, String> response = new HashMap<>();
            response.put("status", "stopped");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Failed to stop container {}", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}