package io.github.kawaiicakes.homoflesh.networking;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import io.github.kawaiicakes.homoflesh.entity.Homunculus;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class runs ephemeral thread on the server's end to fake a connection from a client. One connection is used for
 * all clients. Packets sent to the homunculi therefore must include the UUID(s) of the target homunculus(i).
 */
public class SpoofedClient {
    private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Connection DUMMY_CONNECTION;
    private static Homunculus CAMELIA;

    private MinecraftServer server;
    private String serverIp;
    private int serverPort;
    volatile boolean aborted;

    public static Connection getDummyConnection() {
        return DUMMY_CONNECTION;
    }

    public <T extends MinecraftServer> SpoofedClient(final T server) throws InterruptedException {
        this.aborted = false;
        this.server = server;

        if (server instanceof DedicatedServer dedicatedServer) {
            LOGGER.info("Connecting new Homunculus to {}, {}", dedicatedServer.getServerIp(), dedicatedServer.getPort());
            this.serverIp = dedicatedServer.getServerIp();
            this.serverPort = dedicatedServer.getServerPort();
        } else if (server instanceof IntegratedServer integratedServer) {
            LOGGER.info("Connecting new Homunculus to {}, {}", "localhost", integratedServer.getPort());
            this.serverIp = "127.0.0.1";
            this.serverPort = integratedServer.getPort();
        }

        if (DUMMY_CONNECTION == null) {
            Thread startDummyConnection = new DummyConnectionThread();
            startDummyConnection.start();
            startDummyConnection.join();
        }

        Thread spoofedServerConnectorThread = new SpoofedServerConnectorThread();
        spoofedServerConnectorThread.start();
    }

    private class SpoofedServerConnectorThread extends Thread {
        private SpoofedServerConnectorThread() {
            super("Server Connector #" + UNIQUE_THREAD_ID.incrementAndGet());
        }

        @Override
        public void run() {
            InetSocketAddress inetsocketaddress = null;

            try {
                if (SpoofedClient.this.aborted) {
                    return;
                }

                if (CAMELIA == null) {
                    CAMELIA = new Homunculus(SpoofedClient.this.server.overworld(), SpoofedClient.DUMMY_CONNECTION, new GameProfile(UUID.fromString("7d9c612a-813e-4610-8d7e-46a65376aae0"), "axolotlite"));
                }

                Optional<InetSocketAddress> optional = ServerNameResolver.DEFAULT
                        .resolveAddress(
                                new ServerAddress(SpoofedClient.this.serverIp, SpoofedClient.this.serverPort))
                        .map(ResolvedServerAddress::asInetSocketAddress);

                if (SpoofedClient.this.aborted) {
                    return;
                }

                if (optional.isEmpty()) {
                    LOGGER.warn("Spoofed connection failed; unknown host");
                    return;
                }

                inetsocketaddress = optional.get();
                SpoofedClient.this.server.getPlayerList().placeNewPlayer(CAMELIA.connection.getConnection(), CAMELIA);

            } catch (Exception exception2) {
                if (SpoofedClient.this.aborted) {
                    return;
                }

                Throwable throwable = exception2.getCause();
                Exception exception;
                if (throwable instanceof Exception exception1) {
                    exception = exception1;
                } else {
                    exception = exception2;
                }

                LOGGER.error("Couldn't connect to server", exception2);
                String s = inetsocketaddress == null ? exception.getMessage() : exception.getMessage().replaceAll(inetsocketaddress.getHostName() + ":" + inetsocketaddress.getPort(), "").replaceAll(inetsocketaddress.toString(), "");
                LOGGER.error(s);
            }
        }
    }

    private class DummyConnectionThread extends Thread {
        private DummyConnectionThread() {
            super("Dummy Connection #" + UNIQUE_THREAD_ID.get());
        }

        // TODO: add exception/disconnection handling.
        @Override
        public void run() {
            InetSocketAddress socketAddress = ServerNameResolver.DEFAULT
                    .resolveAddress(new ServerAddress(SpoofedClient.this.serverIp, SpoofedClient.this.serverPort))
                    .map(ResolvedServerAddress::asInetSocketAddress).orElseThrow();

            LOGGER.info("Resolving dummy connection at " + socketAddress);

            /*
            for some reason this actually causes the connection to be refused

            if (SpoofedClient.this.server instanceof IntegratedServer) {
                DUMMY_CONNECTION = Connection.connectToLocalServer(socketAddress);
                return;
            }
             */
            DUMMY_CONNECTION = Connection.connectToServer(socketAddress, true);
        }
    }
}
