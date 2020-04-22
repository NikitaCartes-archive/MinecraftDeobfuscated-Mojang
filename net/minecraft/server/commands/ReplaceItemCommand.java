/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ReplaceItemCommand {
    public static final SimpleCommandExceptionType ERROR_NOT_A_CONTAINER = new SimpleCommandExceptionType(new TranslatableComponent("commands.replaceitem.block.failed"));
    public static final DynamicCommandExceptionType ERROR_INAPPLICABLE_SLOT = new DynamicCommandExceptionType(object -> new TranslatableComponent("commands.replaceitem.slot.inapplicable", object));
    public static final Dynamic2CommandExceptionType ERROR_ENTITY_SLOT = new Dynamic2CommandExceptionType((object, object2) -> new TranslatableComponent("commands.replaceitem.entity.failed", object, object2));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("replaceitem").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.literal("block").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("pos", BlockPosArgument.blockPos()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("slot", SlotArgument.slot()).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("item", ItemArgument.item()).executes(commandContext -> ReplaceItemCommand.setBlockItem((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "pos"), SlotArgument.getSlot(commandContext, "slot"), ItemArgument.getItem(commandContext, "item").createItemStack(1, false)))).then(Commands.argument("count", IntegerArgumentType.integer(1, 64)).executes(commandContext -> ReplaceItemCommand.setBlockItem((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "pos"), SlotArgument.getSlot(commandContext, "slot"), ItemArgument.getItem(commandContext, "item").createItemStack(IntegerArgumentType.getInteger(commandContext, "count"), true))))))))).then(Commands.literal("entity").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", EntityArgument.entities()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("slot", SlotArgument.slot()).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("item", ItemArgument.item()).executes(commandContext -> ReplaceItemCommand.setEntityItem((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets"), SlotArgument.getSlot(commandContext, "slot"), ItemArgument.getItem(commandContext, "item").createItemStack(1, false)))).then(Commands.argument("count", IntegerArgumentType.integer(1, 64)).executes(commandContext -> ReplaceItemCommand.setEntityItem((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets"), SlotArgument.getSlot(commandContext, "slot"), ItemArgument.getItem(commandContext, "item").createItemStack(IntegerArgumentType.getInteger(commandContext, "count"), true)))))))));
    }

    private static int setBlockItem(CommandSourceStack commandSourceStack, BlockPos blockPos, int i, ItemStack itemStack) throws CommandSyntaxException {
        BlockEntity blockEntity = commandSourceStack.getLevel().getBlockEntity(blockPos);
        if (!(blockEntity instanceof Container)) {
            throw ERROR_NOT_A_CONTAINER.create();
        }
        Container container = (Container)((Object)blockEntity);
        if (i < 0 || i >= container.getContainerSize()) {
            throw ERROR_INAPPLICABLE_SLOT.create(i);
        }
        container.setItem(i, itemStack);
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.replaceitem.block.success", blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack.getDisplayName()), true);
        return 1;
    }

    private static int setEntityItem(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, int i, ItemStack itemStack) throws CommandSyntaxException {
        ArrayList<Entity> list = Lists.newArrayListWithCapacity(collection.size());
        for (Entity entity : collection) {
            if (entity instanceof ServerPlayer) {
                ((ServerPlayer)entity).inventoryMenu.broadcastChanges();
            }
            if (!entity.setSlot(i, itemStack.copy())) continue;
            list.add(entity);
            if (!(entity instanceof ServerPlayer)) continue;
            ((ServerPlayer)entity).inventoryMenu.broadcastChanges();
        }
        if (list.isEmpty()) {
            throw ERROR_ENTITY_SLOT.create(itemStack.getDisplayName(), i);
        }
        if (list.size() == 1) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.replaceitem.entity.success.single", ((Entity)list.iterator().next()).getDisplayName(), itemStack.getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.replaceitem.entity.success.multiple", list.size(), itemStack.getDisplayName()), true);
        }
        return list.size();
    }
}

