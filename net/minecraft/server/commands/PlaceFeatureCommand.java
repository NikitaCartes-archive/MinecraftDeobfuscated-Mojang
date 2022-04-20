/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class PlaceFeatureCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.placefeature.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("placefeature").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(((RequiredArgumentBuilder)Commands.argument("feature", ResourceKeyArgument.key(Registry.CONFIGURED_FEATURE_REGISTRY)).executes(commandContext -> PlaceFeatureCommand.placeFeature((CommandSourceStack)commandContext.getSource(), ResourceKeyArgument.getConfiguredFeature(commandContext, "feature"), new BlockPos(((CommandSourceStack)commandContext.getSource()).getPosition())))).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(commandContext -> PlaceFeatureCommand.placeFeature((CommandSourceStack)commandContext.getSource(), ResourceKeyArgument.getConfiguredFeature(commandContext, "feature"), BlockPosArgument.getLoadedBlockPos(commandContext, "pos"))))));
    }

    public static int placeFeature(CommandSourceStack commandSourceStack, Holder<ConfiguredFeature<?, ?>> holder, BlockPos blockPos) throws CommandSyntaxException {
        ServerLevel serverLevel = commandSourceStack.getLevel();
        ConfiguredFeature<?, ?> configuredFeature = holder.value();
        if (!configuredFeature.place(serverLevel, serverLevel.getChunkSource().getGenerator(), serverLevel.getRandom(), blockPos)) {
            throw ERROR_FAILED.create();
        }
        String string = holder.unwrapKey().map(resourceKey -> resourceKey.location().toString()).orElse("[unregistered]");
        commandSourceStack.sendSuccess(Component.translatable("commands.placefeature.success", string, blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
        return 1;
    }
}

