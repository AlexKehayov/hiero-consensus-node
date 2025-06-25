// SPDX-License-Identifier: Apache-2.0
package com.hedera.services.bdd.junit.hedera.containers;

import java.time.Duration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * A test container for running a block node server instance.
 */
public class BlockNodeContainer extends GenericContainer<BlockNodeContainer> {
    private static final DockerImageName DEFAULT_IMAGE_NAME =
            DockerImageName.parse("ghcr.io/hiero-ledger/hiero-block-node:0.14.0-SNAPSHOT");
    private static final String blockNodeVersion = "0.14.0-SNAPSHOT";
    private final int port;

    /**
     * Creates a new block node container with the default image.
     */
    public BlockNodeContainer(final long blockNodeId, final int port) {
        this(DEFAULT_IMAGE_NAME, blockNodeId, port);
    }

    /**
     * Creates a new block node container with the specified image.
     *
     * @param dockerImageName the docker image to use
     */
    private BlockNodeContainer(DockerImageName dockerImageName, final long blockNodeId, final int port) {
        super(dockerImageName);
        this.port = port;
        this.withExposedPorts(port)
                .withNetworkAliases("block-node-" + blockNodeId)
                .withEnv("VERSION", blockNodeVersion)
                .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)))
                .waitingFor(Wait.forHealthcheck());
    }

    @Override
    public void start() {
        if (!isRunning()) {
            super.start();
            /*// Add a wait to ensure gRPC service is fully ready
            try {
                Thread.sleep(2000); // Give the service 2 more seconds after container is healthy
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }*/
        }
        waitForHealthy(Duration.ofMinutes(2));
    }

    @Override
    public void stop() {
        if (isRunning()) {
            super.stop();
        }
    }

    /**
     * Gets the mapped port for the block node gRPC server.
     *
     * @return the host port mapped to the container's internal port
     */
    public int getPort() {
        return getMappedPort(port);
    }

    /**
     * Waits for the block node container to be healthy by configuring the health check timeout.
     *
     * @param timeout the maximum duration to wait for the container's health check to pass
     */
    public void waitForHealthy(final Duration timeout) {
        this.waitingFor(Wait.forHealthcheck().withStartupTimeout(timeout));
    }

    @Override
    public String toString() {
        return this.getHost() + ":" + this.getPort();
    }
}
