package io.github.kawaiicakes.homoflesh.networking;

import com.mojang.logging.LogUtils;
import io.github.kawaiicakes.homoflesh.entity.Homunculus;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.kawaiicakes.homoflesh.entity.Homunculus.CAMELIA;

/**
 * This class runs on an ephemeral thread on the server's end to fake a connection from a client.
 */
public class SpoofedClient {
    private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    private static final Logger LOGGER = LogUtils.getLogger();

    private MinecraftServer server;
    volatile Connection connection;
    volatile boolean aborted;

    public <T extends MinecraftServer> SpoofedClient(final T server) {
        this.aborted = false;
        this.server = server;
        this.startNewConnection(server);
    }

    private <T extends MinecraftServer> void startNewConnection(final T server) {
        if (server instanceof DedicatedServer dedicatedServer) {
            /* new ProfileKeyPairManager(this.userApiService, UUID.fromString("7d9c612a-813e-4610-8d7e-46a65376aae0"), this.gameDirectory.toPath());
            final CompletableFuture<Optional<ProfilePublicKey.Data>> completablefuture = pMinecraft.getProfileKeyPairManager().preparePublicKey();
             */
            LOGGER.info("Connecting to {}, {}", dedicatedServer.getServerIp(), dedicatedServer.getPort());
            Thread thread = newConnectionThread(dedicatedServer.getServerIp(), dedicatedServer.getServerPort());
            thread.start();
        } else if (server instanceof IntegratedServer integratedServer) {
            LOGGER.info("Connecting to {}, {}", "127.0.0.1", integratedServer.getPort());
            Thread thread = newConnectionThread("127.0.0.1", integratedServer.getPort());
            thread.start();
        }
    }

    @NotNull
    private Thread newConnectionThread(final String serverIp, final int serverPort) {
        Thread thread = new Thread("Server Connector #" + UNIQUE_THREAD_ID.incrementAndGet()) {
            public void run() {
                InetSocketAddress inetsocketaddress = null;

                try {
                    if (SpoofedClient.this.aborted) {
                        return;
                    }

                    Thread thread1 = Homunculus.FakePlayerNetHandler.establishDummyConnection(serverIp);
                    thread1.start();
                    thread1.join();

                    Optional<InetSocketAddress> optional = ServerNameResolver.DEFAULT
                            .resolveAddress(
                                    new ServerAddress(serverIp, serverPort))
                            .map(ResolvedServerAddress::asInetSocketAddress);

                    if (SpoofedClient.this.aborted) {
                        return;
                    }

                    if (optional.isEmpty()) {
                        LOGGER.warn("Spoofed connection failed; unknown host");
                        return;
                    }

                    inetsocketaddress = optional.get();
                    SpoofedClient.this.server.getPlayerList().placeNewPlayer(CAMELIA().connection.getConnection(), CAMELIA());
                    // SpoofedClient.this.connection = Connection.connectToServer(inetsocketaddress, true);
                    // SpoofedClient.this.connection.setListener(new ClientHandshakePacketListenerImpl(SpoofedClient.this.connection, pMinecraft, ConnectScreen.this.parent, ConnectScreen.this::updateStatus));
                    // SpoofedClient.this.connection.send(new ClientIntentionPacket(inetsocketaddress.getHostName(), inetsocketaddress.getPort(), ConnectionProtocol.LOGIN));
                    // ((ServerLoginPacketListenerImpl) SpoofedClient.this.server.getConnection().getConnections().get(UNIQUE_THREAD_ID.get() - 1).getPacketListener()).handleAcceptedLogin();
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
        };

        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        return thread;
    }
}
