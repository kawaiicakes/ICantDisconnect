package io.github.kawaiicakes.homoflesh.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.UUID;

import static io.github.kawaiicakes.homoflesh.networking.SpoofedClient.getDummyConnection;

@ParametersAreNonnullByDefault
public class Homunculus extends ServerPlayer implements NeutralMob {
    public Homunculus(ServerLevel level, GameProfile name) {
        super(level.getServer(), level, name, null);
        this.connection = new FakePlayerNetHandler(level.getServer(), this);
    }

    public Homunculus(ServerLevel level, Connection connection, GameProfile name) {
        super(level.getServer(), level, name, null);
        this.connection = new FakePlayerNetHandler(level.getServer(), connection, this);
    }

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
    private class FakePlayerNetHandler extends ServerGamePacketListenerImpl {

        private FakePlayerNetHandler(MinecraftServer server, ServerPlayer player) throws NullPointerException {
            super(server, Objects.requireNonNull(getDummyConnection()), player);
        }

        private FakePlayerNetHandler(MinecraftServer server, Connection connection, ServerPlayer player) {
            super(server, connection, player);
        }
    }
}
