/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.dimension.DimensionType;

public class SetSpawnCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("spawnpoint").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).executes(commandContext -> SetSpawnCommand.setSpawn((CommandSourceStack)commandContext.getSource(), Collections.singleton(((CommandSourceStack)commandContext.getSource()).getPlayerOrException()), new BlockPos(((CommandSourceStack)commandContext.getSource()).getPosition())))).then(((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).executes(commandContext -> SetSpawnCommand.setSpawn((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), new BlockPos(((CommandSourceStack)commandContext.getSource()).getPosition())))).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(commandContext -> SetSpawnCommand.setSpawn((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), BlockPosArgument.getOrLoadBlockPos(commandContext, "pos"))))));
    }

    private static int setSpawn(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, BlockPos blockPos) {
        DimensionType dimensionType = commandSourceStack.getLevel().dimensionType();
        for (ServerPlayer serverPlayer : collection) {
            serverPlayer.setRespawnPosition(dimensionType, blockPos, true, false);
        }
        String string = DimensionType.getName(dimensionType).toString();
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.spawnpoint.success.single", blockPos.getX(), blockPos.getY(), blockPos.getZ(), string, collection.iterator().next().getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.spawnpoint.success.multiple", blockPos.getX(), blockPos.getY(), blockPos.getZ(), string, collection.size()), true);
        }
        return collection.size();
    }
}

