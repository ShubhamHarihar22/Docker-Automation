package com.yourname.dockertool.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
public class WorkspaceService {

    private static final Path WORKSPACE_ROOT = Paths.get(
            System.getProperty("user.home"), ".dockertool", "workspaces");

    public String createWorkspace() throws IOException {
        String projectId = UUID.randomUUID().toString();
        Path workspace = WORKSPACE_ROOT.resolve(projectId).normalize();
        Files.createDirectories(workspace);
        log.info("Created workspace {} for project {}", workspace, projectId);
        return workspace.toString();
    }

    public void extractZip(MultipartFile file, String workspacePath) throws IOException {
        Path target = Paths.get(workspacePath).toAbsolutePath().normalize();
        try (InputStream in = file.getInputStream();
             ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path dest = resolveSafe(target, entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(dest);
                } else {
                    Files.createDirectories(dest.getParent());
                    Files.copy(zis, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        log.info("Extracted {} into {}", file.getOriginalFilename(), target);
    }

    private Path resolveSafe(Path workspace, String entryName) throws IOException {
        Path dest = workspace.resolve(entryName).normalize();
        if (!dest.startsWith(workspace)) {
            throw new IOException("Zip entry escapes workspace: " + entryName);
        }
        return dest;
    }
}