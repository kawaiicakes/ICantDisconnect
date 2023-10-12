package io.github.kawaiicakes.homoflesh.mixin;

import io.github.kawaiicakes.homoflesh.entity.Homunculus;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Final
    @Shadow
    private List<ServerPlayer> players;

    @Final
    @Shadow
    private Map<UUID, ServerPlayer> playersByUUID;

    @Final
    @Shadow
    private MinecraftServer server;

    @Shadow
    public void broadcastAll(Packet<?> pPacket) {}

    @Inject(
            method = "placeNewPlayer",
            at = @At("HEAD")
    )
    private void placeHomunculus(Connection pNetManager, ServerPlayer pPlayer, CallbackInfo ci) {
        if (pPlayer instanceof Homunculus homo) {
            homo.setLevel(server.overworld());
            this.players.add(pPlayer);
            this.playersByUUID.put(pPlayer.getUUID(), pPlayer);
            this.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, pPlayer));
            server.overworld().addNewPlayer(homo);
        }
    }
}
