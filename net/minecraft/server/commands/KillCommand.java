/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class KillCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("kill").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).executes(commandContext -> KillCommand.kill((CommandSourceStack)commandContext.getSource(), ImmutableList.of(((CommandSourceStack)commandContext.getSource()).getEntityOrException())))).then(Commands.argument("targets", EntityArgument.entities()).executes(commandContext -> KillCommand.kill((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets")))));
    }

    private static int kill(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection) {
        for (Entity entity : collection) {
            entity.kill();
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(Component.translatable("commands.kill.success.single", collection.iterator().next().getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(Component.translatable("commands.kill.success.multiple", collection.size()), true);
        }
        return collection.size();
    }
}

