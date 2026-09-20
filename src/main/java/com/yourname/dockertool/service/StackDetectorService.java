package com.yourname.dockertool.service;

import com.yourname.dockertool.model.DetectedStack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class StackDetectorService {

    public DetectedStack detect(String workspacePath) {
        Path root = Paths.get(workspacePath);
        if (exists(root, "pom.xml") || exists(root, "build.gradle")) {
            return DetectedStack.JAVA;
        }
        if (exists(root, "package.json")) {
            return DetectedStack.NODE;
        }
        if (exists(root, "requirements.txt") || exists(root, "pyproject.toml")) {
            return DetectedStack.PYTHON;
        }
        log.warn("No recognizable stack marker found in {}", workspacePath);
        return DetectedStack.UNKNOWN;
    }

    private boolean exists(Path root, String name) {
        return Files.exists(root.resolve(name));
    }
}