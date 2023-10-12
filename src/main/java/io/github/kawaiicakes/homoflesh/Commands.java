package io.github.kawaiicakes.homoflesh;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.kawaiicakes.homoflesh.entity.Homunculus;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import java.util.UUID;

public class Commands {

    public static class SPAWN {
        public SPAWN(CommandDispatcher<CommandSourceStack> dispatcher) {
            dispatcher.register(net.minecraft.commands.Commands.literal("civ").executes((command) -> {
                try {
                    return spawn(command.getSource());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        private int spawn(CommandSourceStack source) throws CommandSyntaxException, InterruptedException {
            ServerPlayer player = source.getPlayerOrException();
            ServerLevel level = player.getLevel();
            PlayerList playerList = level.getServer().getPlayerList();

            // Homunculus CAMELIA = new Homunculus(level, new GameProfile(UUID.fromString("7d9c612a-813e-4610-8d7e-46a65376aae0"), "axolotlite"));

            // CAMELIA.setLevel(level);
            // playerList.getPlayers().add(CAMELIA);
            // playerList.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, CAMELIA));
            // level.addNewPlayer(CAMELIA);

            // new SpoofedClient(level.getServer());

            Homunculus CAMELIA = new Homunculus(player.getLevel(), new GameProfile(UUID.fromString("7d9c612a-813e-4610-8d7e-46a65376aae0"), "axolotlite"));
            playerList.placeNewPlayer(CAMELIA.connection.connection, CAMELIA);
            return 1;
        }
    }
}
