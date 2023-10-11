package io.github.kawaiicakes.homoflesh;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.kawaiicakes.homoflesh.networking.SpoofedClient;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

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

            new SpoofedClient(level.getServer());

            // FakePlayer CAMELIA = new FakePlayer(player.getLevel(), new GameProfile(UUID.fromString("7d9c612a-813e-4610-8d7e-46a65376aae0"), "axolotlite"));

            // player.getLevel().getServer().getPlayerList().placeNewPlayer(CAMELIA.connection.getConnection(), CAMELIA);
            // level.addNewPlayer();
            return 1;
        }
    }
}
