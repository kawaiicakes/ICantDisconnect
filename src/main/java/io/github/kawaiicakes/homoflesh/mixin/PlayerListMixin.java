package io.github.kawaiicakes.homoflesh.mixin;

import com.mojang.authlib.GameProfile;
import io.github.kawaiicakes.homoflesh.entity.Homunculus;
import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Shadow
    public void broadcastSystemMessage(Component component, boolean bypass) {}

    @Shadow
    public void updateEntireScoreboard(ServerScoreboard scoreboard, ServerPlayer player) {}

    @Inject(
            method = "placeNewPlayer",
            at = @At("HEAD")
    )
    private void placeHomunculus(Connection pNetManager, ServerPlayer pPlayer, CallbackInfo ci) {
        if (pPlayer instanceof Homunculus homo) {
            GameProfile gameprofile = homo.getGameProfile();

            GameProfileCache gameprofilecache = this.server.getProfileCache();
            Optional<GameProfile> optional = gameprofilecache.get(gameprofile.getId());
            gameprofilecache.add(gameprofile);

            homo.setLevel(server.overworld());

            Homunculus.FakePlayerNetHandler packetHandler = homo.getConnection();
            packetHandler.setupHandlerForLogin();

            this.updateEntireScoreboard(this.server.overworld().getScoreboard(), pPlayer);

            MutableComponent mutablecomponent;
            String s = optional.map(GameProfile::getName).orElse(gameprofile.getName());
            if (homo.getGameProfile().getName().equalsIgnoreCase(s)) {
                mutablecomponent = Component.translatable("multiplayer.player.joined", pPlayer.getDisplayName());
            } else {
                mutablecomponent = Component.translatable("multiplayer.player.joined.renamed", pPlayer.getDisplayName(), s);
            }
            this.broadcastSystemMessage(mutablecomponent.withStyle(ChatFormatting.YELLOW), false);

            this.players.add(homo);
            this.playersByUUID.put(homo.getUUID(), homo);
            this.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, homo));
            server.overworld().addNewPlayer(homo);
        }
    }
}
