package io.github.kawaiicakes.homoflesh.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import io.github.kawaiicakes.homoflesh.networking.SpoofedClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.network.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.stats.Stat;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.DualStackUtils;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static net.minecraft.network.Connection.NETWORK_EPOLL_WORKER_GROUP;
import static net.minecraft.network.Connection.NETWORK_WORKER_GROUP;
import static net.minecraftforge.network.NetworkHooks.registerClientLoginChannel;

public class Homunculus extends ServerPlayer implements NeutralMob {
    public static Homunculus CAMELIA;
    public static Homunculus CAMELIA() {
        return CAMELIA;
    }

    public Homunculus(ServerLevel level, GameProfile name) throws InterruptedException {
        super(level.getServer(), level, name, null);
        this.connection = new FakePlayerNetHandler(level.getServer(), this);
    }

    @Override public void displayClientMessage(Component chatComponent, boolean actionBar) { }
    @Override public void awardStat(Stat stat, int amount) { }
    @Override public boolean isInvulnerableTo(DamageSource source) { return true; }
    @Override public boolean canHarmPlayer(Player player) { return false; }
    @Override public void die(DamageSource source) { }
    @Override public void tick() { }
    @Override public void updateOptions(ServerboundClientInformationPacket packet) { }
    @Override @Nullable
    public MinecraftServer getServer() { return ServerLifecycleHooks.getCurrentServer(); }

    @Override
    public int getRemainingPersistentAngerTime() {
        return 0;
    }

    @Override
    public void setRemainingPersistentAngerTime(int pRemainingPersistentAngerTime) {

    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return null;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID pPersistentAngerTarget) {

    }

    @Override
    public void startPersistentAngerTimer() {

    }

    @Override
    public void setTarget(@Nullable LivingEntity pLivingEntity) {

    }

    @Nullable
    @Override
    public LivingEntity getTarget() {
        return null;
    }

    @ParametersAreNonnullByDefault
    public static class FakePlayerNetHandler extends ServerGamePacketListenerImpl {
        private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger();
        private static final Logger LOGGER = LogUtils.getLogger();
        private static volatile Connection DUMMY_CONNECTION;

        static Connection getDummyConnection() {
            return DUMMY_CONNECTION;
        }

        @NotNull
        public static Thread establishDummyConnection(final String pAddress) {
            Thread thread = new Thread("Server Connector #" + UNIQUE_THREAD_ID.incrementAndGet()) {
                @Override
                public void run() {
                    if (DUMMY_CONNECTION == null) {
                        InetSocketAddress serverAddress = ServerNameResolver.DEFAULT
                                .resolveAddress(ServerAddress.parseString(pAddress))
                                .map(ResolvedServerAddress::asInetSocketAddress).orElseThrow();

                        DualStackUtils.checkIPv6(serverAddress.getAddress());
                        final Connection connection = new Connection(PacketFlow.CLIENTBOUND) {
                            @Override
                            public void channelActive(ChannelHandlerContext pContext) throws Exception {
                                super.channelActive(pContext);

                            }


                        };
                        // FIX THIS?
                        Consumer<Connection> activationHandler = NetworkHooks::registerClientLoginChannel;
                        Class<? extends SocketChannel> oclass;
                        @SuppressWarnings("deprecation")
                        LazyLoadedValue<? extends EventLoopGroup> lazyloadedvalue;
                        if (Epoll.isAvailable()) {
                            oclass = EpollSocketChannel.class;
                            lazyloadedvalue = NETWORK_EPOLL_WORKER_GROUP;
                        } else {
                            oclass = NioSocketChannel.class;
                            lazyloadedvalue = NETWORK_WORKER_GROUP;
                        }

                        (new Bootstrap()).group(lazyloadedvalue.get()).handler(new ChannelInitializer<>() {
                            protected void initChannel(Channel p_129552_) {
                                try {
                                    p_129552_.config().setOption(ChannelOption.TCP_NODELAY, true);
                                } catch (ChannelException ignored) {
                                }

                                p_129552_.pipeline().addLast("timeout", new ReadTimeoutHandler(30)).addLast("splitter", new Varint21FrameDecoder()).addLast("decoder", new PacketDecoder(PacketFlow.CLIENTBOUND)).addLast("prepender", new Varint21LengthFieldPrepender()).addLast("encoder", new PacketEncoder(PacketFlow.SERVERBOUND)).addLast("packet_handler", connection);
                            }
                        }).channel(oclass).connect(serverAddress.getHostName(), serverAddress.getPort()).syncUninterruptibly();

                        DUMMY_CONNECTION = connection;
                    }
                    try {
                        Homunculus.CAMELIA = new Homunculus(ServerLifecycleHooks.getCurrentServer().overworld(), new GameProfile(UUID.fromString("7d9c612a-813e-4610-8d7e-46a65376aae0"), "axolotlite"));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

            thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
            return thread;
        }

        private FakePlayerNetHandler(MinecraftServer server, ServerPlayer player) {
            super(server, getDummyConnection(), player);
        }

        @Override public void tick() { }
        @Override public void resetPosition() { }
        @Override public void disconnect(Component message) { }
        @Override public void handlePlayerInput(ServerboundPlayerInputPacket packet) { }
        @Override public void handleMoveVehicle(ServerboundMoveVehiclePacket packet) { }
        @Override public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket packet) { }
        @Override public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket packet) { }
        @Override public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket packet) { }
        @Override public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket packet) { }
        @Override public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket packet) { }
        @Override public void handleSetCommandBlock(ServerboundSetCommandBlockPacket packet) { }
        @Override public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket packet) { }
        @Override public void handlePickItem(ServerboundPickItemPacket packet) { }
        @Override public void handleRenameItem(ServerboundRenameItemPacket packet) { }
        @Override public void handleSetBeaconPacket(ServerboundSetBeaconPacket packet) { }
        @Override public void handleSetStructureBlock(ServerboundSetStructureBlockPacket packet) { }
        @Override public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket packet) { }
        @Override public void handleJigsawGenerate(ServerboundJigsawGeneratePacket packet) { }
        @Override public void handleSelectTrade(ServerboundSelectTradePacket packet) { }
        @Override public void handleEditBook(ServerboundEditBookPacket packet) { }
        @Override public void handleEntityTagQuery(ServerboundEntityTagQuery packet) { }
        @Override public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQuery packet) { }
        @Override public void handleMovePlayer(ServerboundMovePlayerPacket packet) { }
        @Override public void teleport(double x, double y, double z, float yaw, float pitch) { }
        @Override public void teleport(double x, double y, double z, float yaw, float pitch, Set<ClientboundPlayerPositionPacket.RelativeArgument> flags) { }
        @Override public void handlePlayerAction(ServerboundPlayerActionPacket packet) { }
        @Override public void handleUseItemOn(ServerboundUseItemOnPacket packet) { }
        @Override public void handleUseItem(ServerboundUseItemPacket packet) { }
        @Override public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket packet) { }
        @Override public void handleResourcePackResponse(ServerboundResourcePackPacket packet) { }
        @Override public void handlePaddleBoat(ServerboundPaddleBoatPacket packet) { }
        @Override public void onDisconnect(Component message) { }
        @Override public void send(Packet<?> packet) { }
        @Override public void send(Packet<?> packet, @Nullable PacketSendListener sendListener) { }
        @Override public void handleSetCarriedItem(ServerboundSetCarriedItemPacket packet) { }
        @Override public void handleChat(ServerboundChatPacket packet) { }
        @Override public void handleAnimate(ServerboundSwingPacket packet) { }
        @Override public void handlePlayerCommand(ServerboundPlayerCommandPacket packet) { }
        @Override public void handleInteract(ServerboundInteractPacket packet) { }
        @Override public void handleClientCommand(ServerboundClientCommandPacket packet) { }
        @Override public void handleContainerClose(ServerboundContainerClosePacket packet) { }
        @Override public void handleContainerClick(ServerboundContainerClickPacket packet) { }
        @Override public void handlePlaceRecipe(ServerboundPlaceRecipePacket packet) { }
        @Override public void handleContainerButtonClick(ServerboundContainerButtonClickPacket packet) { }
        @Override public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket packet) { }
        @Override public void handleSignUpdate(ServerboundSignUpdatePacket packet) { }
        @Override public void handleKeepAlive(ServerboundKeepAlivePacket packet) { }
        @Override public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket packet) { }
        @Override public void handleClientInformation(ServerboundClientInformationPacket packet) { }
        @Override public void handleCustomPayload(ServerboundCustomPayloadPacket packet) { }
        @Override public void handleChangeDifficulty(ServerboundChangeDifficultyPacket packet) { }
        @Override public void handleLockDifficulty(ServerboundLockDifficultyPacket packet) { }
    }
}
