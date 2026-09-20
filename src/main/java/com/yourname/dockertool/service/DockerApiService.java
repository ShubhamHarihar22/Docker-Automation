package com.yourname.dockertool.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.yourname.dockertool.model.ContainerInfo;
import com.yourname.dockertool.model.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DockerApiService {

    private final DockerClient dockerClient;

    public DockerApiService() {
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("unix://" + System.getProperty("user.home") + "/.docker/run/docker.sock")
                .build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();
        this.dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }

    public String buildImage(String dockerfileDir, String imageTag) throws IOException {
        try {
            return dockerClient.buildImageCmd(new File(dockerfileDir))
                    .withTags(Set.of(imageTag))
                    .exec(new BuildImageResultCallback())
                    .awaitImageId();
        } catch (Exception e) {
            log.error("Docker build failed for tag {}", imageTag, e);
            throw new IOException("Docker build failed: " + e.getMessage(), e);
        }
    }

    public List<ImageInfo> listImages() {
        return dockerClient.listImagesCmd().exec().stream()
                .map(img -> {
                    String tag = (img.getRepoTags() != null && img.getRepoTags().length > 0)
                            ? img.getRepoTags()[0]
                            : "<none>";
                    ImageInfo info = new ImageInfo();
                    info.setId(img.getId());
                    info.setTag(tag);
                    info.setCreatedAt(img.getCreated());
                    info.setSizeBytes(img.getSize());
                    return info;
                })
                .collect(Collectors.toList());
    }

    public String startContainer(String imageTag, int hostPort, int containerPort) throws IOException {
        try {
            ExposedPort exposed = ExposedPort.tcp(containerPort);
            PortBinding[] bindings = new PortBinding[]{PortBinding.parse(hostPort + ":" + containerPort)};
            HostConfig hostConfig = HostConfig.newHostConfig().withPortBindings(bindings);
            CreateContainerResponse resp = dockerClient.createContainerCmd(imageTag)
                    .withExposedPorts(exposed)
                    .withHostConfig(hostConfig)
                    .exec();
            dockerClient.startContainerCmd(resp.getId()).exec();
            return resp.getId();
        } catch (Exception e) {
            log.error("Failed to start container from image {}", imageTag, e);
            throw new IOException("Failed to start container: " + e.getMessage(), e);
        }
    }

    public void stopContainer(String containerId) throws IOException {
        try {
            dockerClient.stopContainerCmd(containerId).exec();
        } catch (Exception e) {
            log.error("Failed to stop container {}", containerId, e);
            throw new IOException("Failed to stop container: " + e.getMessage(), e);
        }
    }

    public List<ContainerInfo> listContainers() {
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();
        return containers.stream().map(c -> {
            String name = (c.getNames() != null && c.getNames().length > 0)
                    ? c.getNames()[0].replaceFirst("^/", "")
                    : "<unnamed>";
            String[] portStrings = Arrays.stream(c.getPorts())
                    .map(p -> p.getPrivatePort() + "->" + (p.getPublicPort() != null ? p.getPublicPort() : "?"))
                    .toArray(String[]::new);
            return ContainerInfo.builder()
                    .id(c.getId())
                    .name(name)
                    .image(c.getImage())
                    .status(c.getStatus())
                    .state(c.getState())
                    .created(c.getCreated())
                    .ports(portStrings)
                    .build();
        }).collect(Collectors.toList());
    }

    public void pullImage(String imageName, String tag) throws IOException {
        try {
            dockerClient.pullImageCmd(imageName)
                    .withTag(tag)
                    .exec(new com.github.dockerjava.api.async.ResultCallback.Adapter<>())
                    .awaitCompletion();
        } catch (Exception e) {
            log.error("Failed to pull Docker image {}:{}", imageName, tag, e);
            throw new IOException("Failed to pull Docker image " + imageName + ":" + tag
                    + ": " + e.getMessage(), e);
        }
    }
}