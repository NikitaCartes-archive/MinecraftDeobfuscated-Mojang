/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class TeleportCommand {
    private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(Component.translatable("commands.teleport.invalidPosition"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        LiteralCommandNode<CommandSourceStack> literalCommandNode = commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("teleport").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.argument("location", Vec3Argument.vec3()).executes(commandContext -> TeleportCommand.teleportToPos((CommandSourceStack)commandContext.getSource(), Collections.singleton(((CommandSourceStack)commandContext.getSource()).getEntityOrException()), ((CommandSourceStack)commandContext.getSource()).getLevel(), Vec3Argument.getCoordinates(commandContext, "location"), WorldCoordinates.current(), null)))).then(Commands.argument("destination", EntityArgument.entity()).executes(commandContext -> TeleportCommand.teleportToEntity((CommandSourceStack)commandContext.getSource(), Collections.singleton(((CommandSourceStack)commandContext.getSource()).getEntityOrException()), EntityArgument.getEntity(commandContext, "destination"))))).then(((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.entities()).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("location", Vec3Argument.vec3()).executes(commandContext -> TeleportCommand.teleportToPos((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets"), ((CommandSourceStack)commandContext.getSource()).getLevel(), Vec3Argument.getCoordinates(commandContext, "location"), null, null))).then(Commands.argument("rotation", RotationArgument.rotation()).executes(commandContext -> TeleportCommand.teleportToPos((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets"), ((CommandSourceStack)commandContext.getSource()).getLevel(), Vec3Argument.getCoordinates(commandContext, "location"), RotationArgument.getRotation(commandContext, "rotation"), null)))).then(((LiteralArgumentBuilder)Commands.literal("facing").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("entity").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("facingEntity", EntityArgument.entity()).executes(commandContext -> TeleportCommand.teleportToPos((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets"), ((CommandSourceStack)commandContext.getSource()).getLevel(), Vec3Argument.getCoordinates(commandContext, "location"), null, new LookAt(EntityArgument.getEntity(commandContext, "facingEntity"), EntityAnchorArgument.Anchor.FEET)))).then(Commands.argument("facingAnchor", EntityAnchorArgument.anchor()).executes(commandContext -> TeleportCommand.teleportToPos((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets"), ((CommandSourceStack)commandContext.getSource()).getLevel(), Vec3Argument.getCoordinates(commandContext, "location"), null, new LookAt(EntityArgument.getEntity(commandContext, "facingEntity"), EntityAnchorArgument.getAnchor(commandContext, "facingAnchor")))))))).then(Commands.argument("facingLocation", Vec3Argument.vec3()).executes(commandContext -> TeleportCommand.teleportToPos((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets"), ((CommandSourceStack)commandContext.getSource()).getLevel(), Vec3Argument.getCoordinates(commandContext, "location"), null, new LookAt(Vec3Argument.getVec3(commandContext, "facingLocation")))))))).then(Commands.argument("destination", EntityArgument.entity()).executes(commandContext -> TeleportCommand.teleportToEntity((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets"), EntityArgument.getEntity(commandContext, "destination"))))));
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("tp").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).redirect(literalCommandNode));
    }

    private static int teleportToEntity(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, Entity entity) throws CommandSyntaxException {
        for (Entity entity2 : collection) {
            TeleportCommand.performTeleport(commandSourceStack, entity2, (ServerLevel)entity.level, entity.getX(), entity.getY(), entity.getZ(), EnumSet.noneOf(ClientboundPlayerPositionPacket.RelativeArgument.class), entity.getYRot(), entity.getXRot(), null);
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(Component.translatable("commands.teleport.success.entity.single", collection.iterator().next().getDisplayName(), entity.getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(Component.translatable("commands.teleport.success.entity.multiple", collection.size(), entity.getDisplayName()), true);
        }
        return collection.size();
    }

    private static int teleportToPos(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, ServerLevel serverLevel, Coordinates coordinates, @Nullable Coordinates coordinates2, @Nullable LookAt lookAt) throws CommandSyntaxException {
        Vec3 vec3 = coordinates.getPosition(commandSourceStack);
        Vec2 vec2 = coordinates2 == null ? null : coordinates2.getRotation(commandSourceStack);
        EnumSet<ClientboundPlayerPositionPacket.RelativeArgument> set = EnumSet.noneOf(ClientboundPlayerPositionPacket.RelativeArgument.class);
        if (coordinates.isXRelative()) {
            set.add(ClientboundPlayerPositionPacket.RelativeArgument.X);
        }
        if (coordinates.isYRelative()) {
            set.add(ClientboundPlayerPositionPacket.RelativeArgument.Y);
        }
        if (coordinates.isZRelative()) {
            set.add(ClientboundPlayerPositionPacket.RelativeArgument.Z);
        }
        if (coordinates2 == null) {
            set.add(ClientboundPlayerPositionPacket.RelativeArgument.X_ROT);
            set.add(ClientboundPlayerPositionPacket.RelativeArgument.Y_ROT);
        } else {
            if (coordinates2.isXRelative()) {
                set.add(ClientboundPlayerPositionPacket.RelativeArgument.X_ROT);
            }
            if (coordinates2.isYRelative()) {
                set.add(ClientboundPlayerPositionPacket.RelativeArgument.Y_ROT);
            }
        }
        for (Entity entity : collection) {
            if (coordinates2 == null) {
                TeleportCommand.performTeleport(commandSourceStack, entity, serverLevel, vec3.x, vec3.y, vec3.z, set, entity.getYRot(), entity.getXRot(), lookAt);
                continue;
            }
            TeleportCommand.performTeleport(commandSourceStack, entity, serverLevel, vec3.x, vec3.y, vec3.z, set, vec2.y, vec2.x, lookAt);
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(Component.translatable("commands.teleport.success.location.single", collection.iterator().next().getDisplayName(), TeleportCommand.formatDouble(vec3.x), TeleportCommand.formatDouble(vec3.y), TeleportCommand.formatDouble(vec3.z)), true);
        } else {
            commandSourceStack.sendSuccess(Component.translatable("commands.teleport.success.location.multiple", collection.size(), TeleportCommand.formatDouble(vec3.x), TeleportCommand.formatDouble(vec3.y), TeleportCommand.formatDouble(vec3.z)), true);
        }
        return collection.size();
    }

    private static String formatDouble(double d) {
        return String.format(Locale.ROOT, "%f", d);
    }

    private static void performTeleport(CommandSourceStack commandSourceStack, Entity entity, ServerLevel serverLevel, double d, double e, double f, Set<ClientboundPlayerPositionPacket.RelativeArgument> set, float g, float h, @Nullable LookAt lookAt) throws CommandSyntaxException {
        BlockPos blockPos = new BlockPos(d, e, f);
        if (!Level.isInSpawnableBounds(blockPos)) {
            throw INVALID_POSITION.create();
        }
        float i = Mth.wrapDegrees(g);
        float j = Mth.wrapDegrees(h);
        if (entity instanceof ServerPlayer) {
            ChunkPos chunkPos = new ChunkPos(new BlockPos(d, e, f));
            serverLevel.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkPos, 1, entity.getId());
            entity.stopRiding();
            if (((ServerPlayer)entity).isSleeping()) {
                ((ServerPlayer)entity).stopSleepInBed(true, true);
            }
            if (serverLevel == entity.level) {
                ((ServerPlayer)entity).connection.teleport(d, e, f, i, j, set);
            } else {
                ((ServerPlayer)entity).teleportTo(serverLevel, d, e, f, i, j);
            }
            entity.setYHeadRot(i);
        } else {
            float k = Mth.clamp(j, -90.0f, 90.0f);
            if (serverLevel == entity.level) {
                entity.moveTo(d, e, f, i, k);
                entity.setYHeadRot(i);
            } else {
                entity.unRide();
                Entity entity2 = entity;
                entity = entity2.getType().create(serverLevel);
                if (entity != null) {
                    entity.restoreFrom(entity2);
                    entity.moveTo(d, e, f, i, k);
                    entity.setYHeadRot(i);
                    entity2.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
                    serverLevel.addDuringTeleport(entity);
                } else {
                    return;
                }
            }
        }
        if (lookAt != null) {
            lookAt.perform(commandSourceStack, entity);
        }
        if (!(entity instanceof LivingEntity) || !((LivingEntity)entity).isFallFlying()) {
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0, 0.0, 1.0));
            entity.setOnGround(true);
        }
        if (entity instanceof PathfinderMob) {
            ((PathfinderMob)entity).getNavigation().stop();
        }
    }

    static class LookAt {
        private final Vec3 position;
        private final Entity entity;
        private final EntityAnchorArgument.Anchor anchor;

        public LookAt(Entity entity, EntityAnchorArgument.Anchor anchor) {
            this.entity = entity;
            this.anchor = anchor;
            this.position = anchor.apply(entity);
        }

        public LookAt(Vec3 vec3) {
            this.entity = null;
            this.position = vec3;
            this.anchor = null;
        }

        public void perform(CommandSourceStack commandSourceStack, Entity entity) {
            if (this.entity != null) {
                if (entity instanceof ServerPlayer) {
                    ((ServerPlayer)entity).lookAt(commandSourceStack.getAnchor(), this.entity, this.anchor);
                } else {
                    entity.lookAt(commandSourceStack.getAnchor(), this.position);
                }
            } else {
                entity.lookAt(commandSourceStack.getAnchor(), this.position);
            }
        }
    }
}

