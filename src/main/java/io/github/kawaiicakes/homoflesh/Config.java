package io.github.kawaiicakes.homoflesh;

import net.minecraftforge.common.ForgeConfigSpec;

// TODO: internationalize
// TODO: investigate if this config is able to be reloaded...
/**
 * I'm writing this shit so the existence of states of certain values which necessarily mean others must be false/have
 * no effect are reflected in the getter methods. Hopefully I avoid weird stupid bugs this way.
 */
@SuppressWarnings("FieldCanBeLocal")
public class Config {
    public static ForgeConfigSpec CONFIG;

    // HOMUNCULI ENTITY SETTINGS
    private static ForgeConfigSpec.BooleanValue ALL_HOMUNCULI_ARE_INVULNERABLE, HOMUNCULI_WANDER;
    private static ForgeConfigSpec.IntValue VISION_RANGE;

    // SLEEP SETTINGS
    private static ForgeConfigSpec.BooleanValue HOMUNCULI_CAN_SLEEP, HOMUNCULI_SLEEP_ON_DISCONNECT, HOMUNCULI_MAY_BE_ROUSED;
    private static ForgeConfigSpec.DoubleValue TIME_HOMUNCULI_SLEEP;

    // ATTACK SETTINGS
    private static ForgeConfigSpec.BooleanValue HOMUNCULI_ATTACK_HOSTILES, ROUSED_BY_HOSTILES, ROUSES_WHEN_HOSTILES_NEAR, HOMUNCULI_ANGER_BEATS_SLEEP;
    private static ForgeConfigSpec.DoubleValue TIME_HOMUNCULI_GRUDGE;

    // ENTITY GETTERS
    public static boolean entityImmunityIsEnabled() {
        return ALL_HOMUNCULI_ARE_INVULNERABLE.get();
    }
    public static boolean entityWanders() {
        return HOMUNCULI_WANDER.get();
    }
    public static int entityVisionRangeBlocks() {
        return VISION_RANGE.get();
    }

    // SLEEP GETTERS
    public static boolean sleepIsEnabled() {
        return HOMUNCULI_CAN_SLEEP.get();
    }
    public static boolean sleepsOnDisconnect() {
        return HOMUNCULI_CAN_SLEEP.get() ? HOMUNCULI_SLEEP_ON_DISCONNECT.get() : false;
    }
    public static boolean mayBeRoused() {
        return HOMUNCULI_CAN_SLEEP.get() ? HOMUNCULI_MAY_BE_ROUSED.get() : false;
    }
    public static double returnsToSleepAfterSeconds() {
        return HOMUNCULI_CAN_SLEEP.get() ? (TIME_HOMUNCULI_SLEEP.get() * 20) : -1;
    }

    // ATTACK GETTERS
    public static boolean attacksHostiles() {
        return HOMUNCULI_ATTACK_HOSTILES.get();
    }
    public static boolean rousesWhenHurt() {
        return HOMUNCULI_MAY_BE_ROUSED.get() ? ROUSED_BY_HOSTILES.get() : false;
    }
    public static boolean rousesWhenHostilesNear() {
        return HOMUNCULI_MAY_BE_ROUSED.get() ? ROUSES_WHEN_HOSTILES_NEAR.get() : false;
    }
    public static boolean cannotSleepWhileAngry() {
        return HOMUNCULI_ANGER_BEATS_SLEEP.get();
    }
    public static double holdsGrudgeForSeconds() {
        return HOMUNCULI_ATTACK_HOSTILES.get() ? (TIME_HOMUNCULI_GRUDGE.get() * 20) : -1;
    }

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("test").push("Disconnected player entity settings");

        ALL_HOMUNCULI_ARE_INVULNERABLE = builder
                .define("All homunculi are invincible.", false);
        HOMUNCULI_WANDER = builder
                .define("Homunculi walk about. If false, homunculi will attempt to stay or return to the same area.", false);
        VISION_RANGE = builder
                .defineInRange("How far homunculi can see in blocks.", 20, 2, 64);
        // TODO future looting setting
        // TODO future configurable list of invulnerable players?
        // TODO viewable list of players who have been spotted by the homunculus?

        builder.pop();
        builder.push("Disconnected player sleep settings");

        HOMUNCULI_CAN_SLEEP = builder
                .define("Disconnected players have the ability to sleep.", true);
        HOMUNCULI_SLEEP_ON_DISCONNECT = builder
                .define("Players immediately sleep on disconnect.", true);
        HOMUNCULI_MAY_BE_ROUSED = builder
                .define("Sleeping players may be roused. You may right-click to do so.", true);

        TIME_HOMUNCULI_SLEEP = builder
                .defineInRange("The time in seconds until roused players go back to sleep.", 40.00, 1.00, Double.MAX_VALUE);

        builder.pop();
        builder.push("Disconnected player attack settings");

        HOMUNCULI_ATTACK_HOSTILES = builder
                .define("Homunculi retaliate against attackers.", true);
        ROUSED_BY_HOSTILES = builder
                .define("Homunculi rouse when attacked.", true);
        ROUSES_WHEN_HOSTILES_NEAR = builder
                .define("Nearby hostiles rouse homunculi.", true);
        HOMUNCULI_ANGER_BEATS_SLEEP = builder
                .define("Homunculi cannot sleep if angry.", true);

        TIME_HOMUNCULI_GRUDGE = builder
                .defineInRange("The time in seconds homunculi remain angered.", 20.00, 1.00, Double.MAX_VALUE);

        builder.pop();

        CONFIG = builder.build();
    }
}
