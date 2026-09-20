package com.yourname.dockertool.service;

import com.yourname.dockertool.model.DetectedStack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class DockerfileGeneratorService {

    public String generate(DetectedStack stack, int port) throws IOException {
        String filename;
        switch (stack) {
            case JAVA:
                filename = "java-template.txt";
                break;
            case NODE:
                filename = "node-template.txt";
                break;
            case PYTHON:
                filename = "python-template.txt";
                break;
            default:
                throw new IllegalArgumentException("Cannot generate Dockerfile for unknown stack");
        }
        ClassPathResource resource = new ClassPathResource("dockerfile-templates/" + filename);
        if (!resource.exists()) {
            throw new IOException("Template not found: " + filename);
        }
        String template;
        try (InputStream in = resource.getInputStream()) {
            template = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        return template.replace("{{PORT}}", String.valueOf(port));
    }
}