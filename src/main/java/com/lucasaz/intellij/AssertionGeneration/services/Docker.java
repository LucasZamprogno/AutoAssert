package com.lucasaz.intellij.AssertionGeneration.services;

import com.lucasaz.intellij.AssertionGeneration.exceptions.PluginException;
import com.lucasaz.intellij.AssertionGeneration.model.task.Task;
import com.lucasaz.intellij.AssertionGeneration.util.Util;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;

public class Docker {

    public static void runContainer(long id, Task task) throws PluginException {
        try {
            String containerVolumeRoot = "/app/main"; // This will be constant between containers
            String volumeFromFS = Util.hostFSVolumeDir + "/" + id;
            String hostTestDir = Util.joinStringPaths(volumeFromFS, task.testDir);
            String containerTestDir = Util.joinStringPaths(containerVolumeRoot, task.testDir);
            String volumeTest = (hostTestDir + ":" + containerTestDir).replaceAll("\\\\", "/"); // TODO make this better
            DockerClient docker = DefaultDockerClient.fromEnv().build();
            HostConfig hc = HostConfig.builder().appendBinds(volumeTest).build();
            ContainerConfig cc = ContainerConfig
                    .builder()
                    .image(task.image)
                    .hostConfig(hc)
                    .env("TEST_DIR=" + task.testDir)
                    .build();
            ContainerCreation container = docker.createContainer(cc);
            String containerId = container.id();
            System.out.println("Starting container");
            docker.startContainer(containerId);
            docker.waitContainer(containerId);
            System.out.println("Container finished, removing");
            docker.removeContainer(containerId);
        } catch (DockerCertificateException | DockerException | InterruptedException err) {
            err.printStackTrace();
            throw new PluginException("Error starting/running sub-container");
        }
    }
}
