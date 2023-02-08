/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class RideCommand {
    private static final DynamicCommandExceptionType ERROR_NOT_RIDING = new DynamicCommandExceptionType(object -> Component.translatable("commands.ride.not_riding", object));
    private static final Dynamic2CommandExceptionType ERROR_ALREADY_RIDING = new Dynamic2CommandExceptionType((object, object2) -> Component.translatable("commands.ride.already_riding", object, object2));
    private static final Dynamic2CommandExceptionType ERROR_MOUNT_FAILED = new Dynamic2CommandExceptionType((object, object2) -> Component.translatable("commands.ride.mount.failure.generic", object, object2));
    private static final SimpleCommandExceptionType ERROR_MOUNTING_PLAYER = new SimpleCommandExceptionType(Component.translatable("commands.ride.mount.failure.cant_ride_players"));
    private static final SimpleCommandExceptionType ERROR_MOUNTING_LOOP = new SimpleCommandExceptionType(Component.translatable("commands.ride.mount.failure.loop"));
    private static final SimpleCommandExceptionType ERROR_WRONG_DIMENSION = new SimpleCommandExceptionType(Component.translatable("commands.ride.mount.failure.wrong_dimension"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("ride").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(((RequiredArgumentBuilder)Commands.argument("target", EntityArgument.entity()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("mount").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("vehicle", EntityArgument.entity()).executes(commandContext -> RideCommand.mount((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity(commandContext, "target"), EntityArgument.getEntity(commandContext, "vehicle")))))).then(Commands.literal("dismount").executes(commandContext -> RideCommand.dismount((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity(commandContext, "target"))))));
    }

    private static int mount(CommandSourceStack commandSourceStack, Entity entity, Entity entity22) throws CommandSyntaxException {
        Entity entity3 = entity.getVehicle();
        if (entity3 != null) {
            throw ERROR_ALREADY_RIDING.create(entity.getDisplayName(), entity3.getDisplayName());
        }
        if (entity22.getType() == EntityType.PLAYER) {
            throw ERROR_MOUNTING_PLAYER.create();
        }
        if (entity.getSelfAndPassengers().anyMatch(entity2 -> entity2 == entity22)) {
            throw ERROR_MOUNTING_LOOP.create();
        }
        if (entity.getLevel() != entity22.getLevel()) {
            throw ERROR_WRONG_DIMENSION.create();
        }
        if (!entity.startRiding(entity22, true)) {
            throw ERROR_MOUNT_FAILED.create(entity.getDisplayName(), entity22.getDisplayName());
        }
        commandSourceStack.sendSuccess(Component.translatable("commands.ride.mount.success", entity.getDisplayName(), entity22.getDisplayName()), true);
        return 1;
    }

    private static int dismount(CommandSourceStack commandSourceStack, Entity entity) throws CommandSyntaxException {
        Entity entity2 = entity.getVehicle();
        if (entity2 == null) {
            throw ERROR_NOT_RIDING.create(entity.getDisplayName());
        }
        entity.stopRiding();
        commandSourceStack.sendSuccess(Component.translatable("commands.ride.dismount.success", entity.getDisplayName(), entity2.getDisplayName()), true);
        return 1;
    }
}

