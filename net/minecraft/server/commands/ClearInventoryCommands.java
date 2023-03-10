/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ClearInventoryCommands {
    private static final DynamicCommandExceptionType ERROR_SINGLE = new DynamicCommandExceptionType(object -> Component.translatable("clear.failed.single", object));
    private static final DynamicCommandExceptionType ERROR_MULTIPLE = new DynamicCommandExceptionType(object -> Component.translatable("clear.failed.multiple", object));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("clear").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).executes(commandContext -> ClearInventoryCommands.clearInventory((CommandSourceStack)commandContext.getSource(), Collections.singleton(((CommandSourceStack)commandContext.getSource()).getPlayerOrException()), itemStack -> true, -1))).then(((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).executes(commandContext -> ClearInventoryCommands.clearInventory((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), itemStack -> true, -1))).then(((RequiredArgumentBuilder)Commands.argument("item", ItemPredicateArgument.itemPredicate(commandBuildContext)).executes(commandContext -> ClearInventoryCommands.clearInventory((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), ItemPredicateArgument.getItemPredicate(commandContext, "item"), -1))).then(Commands.argument("maxCount", IntegerArgumentType.integer(0)).executes(commandContext -> ClearInventoryCommands.clearInventory((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), ItemPredicateArgument.getItemPredicate(commandContext, "item"), IntegerArgumentType.getInteger(commandContext, "maxCount")))))));
    }

    private static int clearInventory(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, Predicate<ItemStack> predicate, int i) throws CommandSyntaxException {
        int j = 0;
        for (ServerPlayer serverPlayer : collection) {
            j += serverPlayer.getInventory().clearOrCountMatchingItems(predicate, i, serverPlayer.inventoryMenu.getCraftSlots());
            serverPlayer.containerMenu.broadcastChanges();
            serverPlayer.inventoryMenu.slotsChanged(serverPlayer.getInventory());
        }
        if (j == 0) {
            if (collection.size() == 1) {
                throw ERROR_SINGLE.create(collection.iterator().next().getName());
            }
            throw ERROR_MULTIPLE.create(collection.size());
        }
        if (i == 0) {
            if (collection.size() == 1) {
                commandSourceStack.sendSuccess(Component.translatable("commands.clear.test.single", j, collection.iterator().next().getDisplayName()), true);
            } else {
                commandSourceStack.sendSuccess(Component.translatable("commands.clear.test.multiple", j, collection.size()), true);
            }
        } else if (collection.size() == 1) {
            commandSourceStack.sendSuccess(Component.translatable("commands.clear.success.single", j, collection.iterator().next().getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(Component.translatable("commands.clear.success.multiple", j, collection.size()), true);
        }
        return j;
    }
}

