package com.yourname.dockertool.controller;

import com.yourname.dockertool.model.DetectedStack;
import com.yourname.dockertool.service.StackDetectorService;
import com.yourname.dockertool.service.WorkspaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class UploadController {

    private final WorkspaceService workspaceService;
    private final StackDetectorService stackDetectorService;

    public UploadController(WorkspaceService workspaceService, StackDetectorService stackDetectorService) {
        this.workspaceService = workspaceService;
        this.stackDetectorService = stackDetectorService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !file.getOriginalFilename().toLowerCase().endsWith(".zip")) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Only .zip files are supported");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        try {
            String workspacePath = workspaceService.createWorkspace();
            workspaceService.extractZip(file, workspacePath);
            DetectedStack detectedStack = stackDetectorService.detect(workspacePath);

            String projectId = workspacePath.substring(workspacePath.lastIndexOf('/') + 1);

            Map<String, String> response = new HashMap<>();
            response.put("projectId", projectId);
            response.put("extractedPath", workspacePath);
            response.put("detectedStack", detectedStack.name());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Upload failed", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to process upload: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}