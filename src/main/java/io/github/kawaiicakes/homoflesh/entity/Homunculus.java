package io.github.kawaiicakes.homoflesh.entity;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.internal.BrandingControl;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class Homunculus extends ServerPlayer implements NeutralMob {
    public FakePlayerNetHandler getConnection() {
        return (FakePlayerNetHandler) this.connection;
    }

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
    public static class FakePlayerNetHandler extends ServerGamePacketListenerImpl {
        private static Connection DUMMY_CONNECTION = new Connection(PacketFlow.CLIENTBOUND);

        private FakePlayerNetHandler(MinecraftServer server, ServerPlayer player) {
            super(server, DUMMY_CONNECTION, player);
        }

        /**
         * This aims to replicate what is sent from the client to the server during login in <code>PlayerList</code>.
         * Done to hopefully avoid any weird side effects of pushing through with an already hacky implementation.
         */
        public void setupHandlerForLogin() {
            spoofClientHandleLoginReply();
        }

        @Override
        public void tick() {
            super.tick();
        }

        // trying to avoid adding extraneous calls related purely to client shit, even if stuff is sent to the server
        private void spoofClientHandleLoginReply() {
            // TODO: fire player login event?
            // TODO: broadcast options?
            // TODO: send MC registry packets??
            // FIXME: come back here later. the stuff affecting the local player on the client (e.g. ClientboundSetHeldItem) is suspicious
            this.handleCustomPayload(new ServerboundCustomPayloadPacket(ServerboundCustomPayloadPacket.BRAND, new FriendlyByteBuf(Unpooled.buffer()).writeUtf(BrandingControl.getClientBranding())));
            // TODO: fire on datapack sync?
            this.server.getPlayerList().sendPlayerPermissionLevel(this.player);
            this.player.getStats().markAllDirty(); // necessary?
        }

        @Override
        public void teleport(double pX, double pY, double pZ, float pYaw, float pPitch, Set<ClientboundPlayerPositionPacket.RelativeArgument> pRelativeSet, boolean pDismountVehicle) {
            double deltaX = pX != this.player.xOld ? pX - this.player.xOld : 0;
            double deltaY = pY != this.player.yOld ? pY - this.player.yOld : 0;
            double deltaZ = pZ != this.player.zOld ? pZ - this.player.zOld : 0;

            this.player.setDeltaMovement(deltaX, deltaY, deltaZ);
            this.player.absMoveTo(pX, pY, pZ, pYaw, pPitch);

            this.player.xOld = pX;
            this.player.yOld = pY;
            this.player.zOld = pZ;
            this.player.xo = pX;
            this.player.yo = pY;
            this.player.zo = pZ;
        }

        @Override
        public void handleMovePlayer(ServerboundMovePlayerPacket pPacket) {
            PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());

            ServerLevel serverlevel = this.player.getLevel();
            if (!this.player.wonGame) {
                if (this.tickCount == 0) {
                    this.resetPosition();
                }

                if (this.awaitingPositionFromClient != null) {
                    if (this.tickCount - this.awaitingTeleportTime > 20) {
                        this.awaitingTeleportTime = this.tickCount;
                        this.teleport(this.awaitingPositionFromClient.x, this.awaitingPositionFromClient.y, this.awaitingPositionFromClient.z, this.player.getYRot(), this.player.getXRot());
                    }

                } else {
                    this.awaitingTeleportTime = this.tickCount;
                    double d0 = clampHorizontal(pPacket.getX(this.player.getX()));
                    double d1 = clampVertical(pPacket.getY(this.player.getY()));
                    double d2 = clampHorizontal(pPacket.getZ(this.player.getZ()));
                    float f = Mth.wrapDegrees(pPacket.getYRot(this.player.getYRot()));
                    float f1 = Mth.wrapDegrees(pPacket.getXRot(this.player.getXRot()));
                    if (this.player.isPassenger()) {
                        this.player.absMoveTo(this.player.getX(), this.player.getY(), this.player.getZ(), f, f1);
                        this.player.getLevel().getChunkSource().move(this.player);
                    } else {
                        double d3 = this.player.getX();
                        double d4 = this.player.getY();
                        double d5 = this.player.getZ();
                        double d6 = this.player.getY();
                        double d7 = d0 - this.firstGoodX;
                        double d8 = d1 - this.firstGoodY;
                        double d9 = d2 - this.firstGoodZ;
                        double d10 = this.player.getDeltaMovement().lengthSqr();
                        double d11 = d7 * d7 + d8 * d8 + d9 * d9;
                        if (this.player.isSleeping()) {
                            if (d11 > 1.0D) {
                                this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), f, f1);
                            }

                        } else {
                            ++this.receivedMovePacketCount;
                            int i = this.receivedMovePacketCount - this.knownMovePacketCount;
                            if (i > 5) {
                                LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", this.player.getName().getString(), i);
                                i = 1;
                            }

                            if (!this.player.isChangingDimension() && (!this.player.getLevel().getGameRules().getBoolean(GameRules.RULE_DISABLE_ELYTRA_MOVEMENT_CHECK) || !this.player.isFallFlying())) {
                                float f2 = this.player.isFallFlying() ? 300.0F : 100.0F;
                                if (d11 - d10 > (double)(f2 * (float)i) && !this.isSingleplayerOwner()) {
                                    LOGGER.warn("{} moved too quickly! {},{},{}", this.player.getName().getString(), d7, d8, d9);
                                    this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getYRot(), this.player.getXRot());
                                    return;
                                }
                            }

                            AABB aabb = this.player.getBoundingBox();
                            d7 = d0 - this.lastGoodX;
                            d8 = d1 - this.lastGoodY;
                            d9 = d2 - this.lastGoodZ;
                            boolean flag = d8 > 0.0D;
                            if (this.player.isOnGround() && !pPacket.isOnGround() && flag) {
                                this.player.jumpFromGround();
                            }

                            boolean flag1 = this.player.verticalCollisionBelow;
                            this.player.move(MoverType.PLAYER, new Vec3(d7, d8, d9));
                            d7 = d0 - this.player.getX();
                            d8 = d1 - this.player.getY();
                            if (d8 > -0.5D || d8 < 0.5D) {
                                d8 = 0.0D;
                            }

                            d9 = d2 - this.player.getZ();
                            d11 = d7 * d7 + d8 * d8 + d9 * d9;
                            boolean flag2 = false;
                            if (!this.player.isChangingDimension() && d11 > 0.0625D && !this.player.isSleeping() && !this.player.gameMode.isCreative() && this.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
                                flag2 = true;
                                LOGGER.warn("{} moved wrongly!", this.player.getName().getString());
                            }

                            this.player.absMoveTo(d0, d1, d2, f, f1);
                            if (this.player.noPhysics || this.player.isSleeping() || (!flag2 || !serverlevel.noCollision(this.player, aabb)) && !this.isPlayerCollidingWithAnythingNew(serverlevel, aabb)) {
                                this.clientIsFloating = d8 >= -0.03125D && !flag1 && this.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR && !this.server.isFlightAllowed() && !this.player.getAbilities().mayfly && !this.player.hasEffect(MobEffects.LEVITATION) && !this.player.isFallFlying() && !this.player.isAutoSpinAttack() && this.noBlocksAround(this.player);
                                this.player.getLevel().getChunkSource().move(this.player);
                                this.player.doCheckFallDamage(this.player.getY() - d6, pPacket.isOnGround());
                                this.player.setOnGround(pPacket.isOnGround());
                                if (flag) {
                                    this.player.resetFallDistance();
                                }

                                this.player.checkMovementStatistics(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5);
                                this.lastGoodX = this.player.getX();
                                this.lastGoodY = this.player.getY();
                                this.lastGoodZ = this.player.getZ();
                            } else {
                                this.teleport(d3, d4, d5, f, f1);
                            }
                        }
                    }
                }
            }
        }
    }
}
