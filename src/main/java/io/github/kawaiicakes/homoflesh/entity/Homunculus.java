package io.github.kawaiicakes.homoflesh.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class Homunculus extends ServerPlayer implements NeutralMob {
    public Homunculus(ServerLevel level, GameProfile name) {
        super(level.getServer(), level, name, null);
        this.connection = new FakePlayerNetHandler(level.getServer(), this);
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
    private static class FakePlayerNetHandler extends ServerGamePacketListenerImpl {
        private static Connection DUMMY_CONNECTION = new Connection(PacketFlow.CLIENTBOUND);

        private FakePlayerNetHandler(MinecraftServer server, ServerPlayer player) {
            super(server, DUMMY_CONNECTION, player);
        }
    }
}
