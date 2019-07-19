/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundSetSpawnPositionPacket;

public class SetWorldSpawnCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("setworldspawn").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).executes(commandContext -> SetWorldSpawnCommand.setSpawn((CommandSourceStack)commandContext.getSource(), new BlockPos(((CommandSourceStack)commandContext.getSource()).getPosition())))).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(commandContext -> SetWorldSpawnCommand.setSpawn((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getOrLoadBlockPos(commandContext, "pos")))));
    }

    private static int setSpawn(CommandSourceStack commandSourceStack, BlockPos blockPos) {
        commandSourceStack.getLevel().setSpawnPos(blockPos);
        commandSourceStack.getServer().getPlayerList().broadcastAll(new ClientboundSetSpawnPositionPacket(blockPos));
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.setworldspawn.success", blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
        return 1;
    }
}

