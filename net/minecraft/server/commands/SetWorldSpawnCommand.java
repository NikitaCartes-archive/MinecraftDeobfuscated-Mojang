/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.AngleArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class SetWorldSpawnCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("setworldspawn").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).executes(commandContext -> SetWorldSpawnCommand.setSpawn((CommandSourceStack)commandContext.getSource(), BlockPos.containing(((CommandSourceStack)commandContext.getSource()).getPosition()), 0.0f))).then(((RequiredArgumentBuilder)Commands.argument("pos", BlockPosArgument.blockPos()).executes(commandContext -> SetWorldSpawnCommand.setSpawn((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getSpawnablePos(commandContext, "pos"), 0.0f))).then(Commands.argument("angle", AngleArgument.angle()).executes(commandContext -> SetWorldSpawnCommand.setSpawn((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getSpawnablePos(commandContext, "pos"), AngleArgument.getAngle(commandContext, "angle"))))));
    }

    private static int setSpawn(CommandSourceStack commandSourceStack, BlockPos blockPos, float f) {
        commandSourceStack.getLevel().setDefaultSpawnPos(blockPos, f);
        commandSourceStack.sendSuccess(Component.translatable("commands.setworldspawn.success", blockPos.getX(), blockPos.getY(), blockPos.getZ(), Float.valueOf(f)), true);
        return 1;
    }
}

