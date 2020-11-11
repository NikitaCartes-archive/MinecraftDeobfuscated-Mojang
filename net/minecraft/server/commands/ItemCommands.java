/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.ItemModifierManager;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ItemCommands {
    static final Dynamic3CommandExceptionType ERROR_TARGET_NOT_A_CONTAINER = new Dynamic3CommandExceptionType((object, object2, object3) -> new TranslatableComponent("commands.item.target.not_a_container", object, object2, object3));
    private static final Dynamic3CommandExceptionType ERROR_SOURCE_NOT_A_CONTAINER = new Dynamic3CommandExceptionType((object, object2, object3) -> new TranslatableComponent("commands.item.source.not_a_container", object, object2, object3));
    static final DynamicCommandExceptionType ERROR_TARGET_INAPPLICABLE_SLOT = new DynamicCommandExceptionType(object -> new TranslatableComponent("commands.item.target.no_such_slot", object));
    private static final DynamicCommandExceptionType ERROR_SOURCE_INAPPLICABLE_SLOT = new DynamicCommandExceptionType(object -> new TranslatableComponent("commands.item.source.no_such_slot", object));
    private static final DynamicCommandExceptionType ERROR_TARGET_NO_CHANGES = new DynamicCommandExceptionType(object -> new TranslatableComponent("commands.item.target.no_changes", object));
    private static final Dynamic2CommandExceptionType ERROR_TARGET_NO_CHANGES_KNOWN_ITEM = new Dynamic2CommandExceptionType((object, object2) -> new TranslatableComponent("commands.item.target.no_changed.known_item", object, object2));
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_MODIFIER = (commandContext, suggestionsBuilder) -> {
        ItemModifierManager itemModifierManager = ((CommandSourceStack)commandContext.getSource()).getServer().getItemModifierManager();
        return SharedSuggestionProvider.suggestResource(itemModifierManager.getKeys(), suggestionsBuilder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("item").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.literal("block").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("pos", BlockPosArgument.blockPos()).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("slot", SlotArgument.slot()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("replace").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("item", ItemArgument.item()).executes(commandContext -> ItemCommands.setBlockItem((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "pos"), SlotArgument.getSlot(commandContext, "slot"), ItemArgument.getItem(commandContext, "item").createItemStack(1, false)))).then(Commands.argument("count", IntegerArgumentType.integer(1, 64)).executes(commandContext -> ItemCommands.setBlockItem((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "pos"), SlotArgument.getSlot(commandContext, "slot"), ItemArgument.getItem(commandContext, "item").createItemStack(IntegerArgumentType.getInteger(commandContext, "count"), true))))))).then(Commands.literal("modify").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("modifier", ResourceLocationArgument.id()).suggests(SUGGEST_MODIFIER).executes(commandContext -> ItemCommands.modifyBlockItem((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "pos"), SlotArgument.getSlot(commandContext, "slot"), ResourceLocationArgument.getItemModifier(commandContext, "modifier")))))).then(((LiteralArgumentBuilder)Commands.literal("copy").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("block").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("source", BlockPosArgument.blockPos()).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("sourceSlot", SlotArgument.slot()).executes(commandContext -> ItemCommands.blockToBlock((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "source"), SlotArgument.getSlot(commandContext, "sourceSlot"), BlockPosArgument.getLoadedBlockPos(commandContext, "pos"), SlotArgument.getSlot(commandContext, "slot")))).then(Commands.argument("modifier", ResourceLocationArgument.id()).suggests(SUGGEST_MODIFIER).executes(commandContext -> ItemCommands.blockToBlock((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "source"), SlotArgument.getSlot(commandContext, "sourceSlot"), BlockPosArgument.getLoadedBlockPos(commandContext, "pos"), SlotArgument.getSlot(commandContext, "slot"), ResourceLocationArgument.getItemModifier(commandContext, "modifier")))))))).then(Commands.literal("entity").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("source", EntityArgument.entity()).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("sourceSlot", SlotArgument.slot()).executes(commandContext -> ItemCommands.entityToBlock((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity(commandContext, "source"), SlotArgument.getSlot(commandContext, "sourceSlot"), BlockPosArgument.getLoadedBlockPos(commandContext, "pos"), SlotArgument.getSlot(commandContext, "slot")))).then(Commands.argument("modifier", ResourceLocationArgument.id()).suggests(SUGGEST_MODIFIER).executes(commandContext -> ItemCommands.entityToBlock((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity(commandContext, "source"), SlotArgument.getSlot(commandContext, "sourceSlot"), BlockPosArgument.getLoadedBlockPos(commandContext, "pos"), SlotArgument.getSlot(commandContext, "slot"), ResourceLocationArgument.getItemModifier(commandContext, "modifier")))))))))))).then(Commands.literal("entity").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", EntityArgument.entities()).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("slot", SlotArgument.slot()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("replace").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("item", ItemArgument.item()).executes(commandContext -> ItemCommands.setEntityItem((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets"), SlotArgument.getSlot(commandContext, "slot"), ItemArgument.getItem(commandContext, "item").createItemStack(1, false)))).then(Commands.argument("count", IntegerArgumentType.integer(1, 64)).executes(commandContext -> ItemCommands.setEntityItem((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets"), SlotArgument.getSlot(commandContext, "slot"), ItemArgument.getItem(commandContext, "item").createItemStack(IntegerArgumentType.getInteger(commandContext, "count"), true))))))).then(Commands.literal("modify").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("modifier", ResourceLocationArgument.id()).suggests(SUGGEST_MODIFIER).executes(commandContext -> ItemCommands.modifyEntityItem((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets"), SlotArgument.getSlot(commandContext, "slot"), ResourceLocationArgument.getItemModifier(commandContext, "modifier")))))).then(((LiteralArgumentBuilder)Commands.literal("copy").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("block").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("source", BlockPosArgument.blockPos()).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("sourceSlot", SlotArgument.slot()).executes(commandContext -> ItemCommands.blockToEntities((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "source"), SlotArgument.getSlot(commandContext, "sourceSlot"), EntityArgument.getEntities(commandContext, "targets"), SlotArgument.getSlot(commandContext, "slot")))).then(Commands.argument("modifier", ResourceLocationArgument.id()).suggests(SUGGEST_MODIFIER).executes(commandContext -> ItemCommands.blockToEntities((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "source"), SlotArgument.getSlot(commandContext, "sourceSlot"), EntityArgument.getEntities(commandContext, "targets"), SlotArgument.getSlot(commandContext, "slot"), ResourceLocationArgument.getItemModifier(commandContext, "modifier")))))))).then(Commands.literal("entity").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("source", EntityArgument.entity()).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("sourceSlot", SlotArgument.slot()).executes(commandContext -> ItemCommands.entityToEntities((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity(commandContext, "source"), SlotArgument.getSlot(commandContext, "sourceSlot"), EntityArgument.getEntities(commandContext, "targets"), SlotArgument.getSlot(commandContext, "slot")))).then(Commands.argument("modifier", ResourceLocationArgument.id()).suggests(SUGGEST_MODIFIER).executes(commandContext -> ItemCommands.entityToEntities((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity(commandContext, "source"), SlotArgument.getSlot(commandContext, "sourceSlot"), EntityArgument.getEntities(commandContext, "targets"), SlotArgument.getSlot(commandContext, "slot"), ResourceLocationArgument.getItemModifier(commandContext, "modifier"))))))))))));
    }

    private static int modifyBlockItem(CommandSourceStack commandSourceStack, BlockPos blockPos, int i, LootItemFunction lootItemFunction) throws CommandSyntaxException {
        Container container = ItemCommands.getContainer(commandSourceStack, blockPos, ERROR_TARGET_NOT_A_CONTAINER);
        if (i < 0 || i >= container.getContainerSize()) {
            throw ERROR_TARGET_INAPPLICABLE_SLOT.create(i);
        }
        ItemStack itemStack = ItemCommands.applyModifier(commandSourceStack, lootItemFunction, container.getItem(i));
        container.setItem(i, itemStack);
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.item.block.set.success", blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack.getDisplayName()), true);
        return 1;
    }

    private static int modifyEntityItem(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, int i, LootItemFunction lootItemFunction) throws CommandSyntaxException {
        HashMap<Entity, ItemStack> map = Maps.newHashMapWithExpectedSize(collection.size());
        for (Entity entity : collection) {
            SlotAccess slotAccess;
            if (entity instanceof ServerPlayer) {
                ((ServerPlayer)entity).inventoryMenu.broadcastChanges();
            }
            if ((slotAccess = entity.getSlot(i)) == SlotAccess.NULL) continue;
            ItemStack itemStack = ItemCommands.applyModifier(commandSourceStack, lootItemFunction, slotAccess.get());
            map.put(entity, itemStack);
            if (!(entity instanceof ServerPlayer)) continue;
            ((ServerPlayer)entity).inventoryMenu.broadcastChanges();
        }
        if (map.isEmpty()) {
            throw ERROR_TARGET_NO_CHANGES.create(i);
        }
        if (map.size() == 1) {
            Map.Entry entry = map.entrySet().iterator().next();
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.item.entity.set.success.single", ((Entity)entry.getKey()).getDisplayName(), ((ItemStack)entry.getValue()).getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.item.entity.set.success.multiple", map.size()), true);
        }
        return map.size();
    }

    private static int setBlockItem(CommandSourceStack commandSourceStack, BlockPos blockPos, int i, ItemStack itemStack) throws CommandSyntaxException {
        Container container = ItemCommands.getContainer(commandSourceStack, blockPos, ERROR_TARGET_NOT_A_CONTAINER);
        if (i < 0 || i >= container.getContainerSize()) {
            throw ERROR_TARGET_INAPPLICABLE_SLOT.create(i);
        }
        container.setItem(i, itemStack);
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.item.block.set.success", blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack.getDisplayName()), true);
        return 1;
    }

    private static Container getContainer(CommandSourceStack commandSourceStack, BlockPos blockPos, Dynamic3CommandExceptionType dynamic3CommandExceptionType) throws CommandSyntaxException {
        BlockEntity blockEntity = commandSourceStack.getLevel().getBlockEntity(blockPos);
        if (!(blockEntity instanceof Container)) {
            throw dynamic3CommandExceptionType.create(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }
        return (Container)((Object)blockEntity);
    }

    private static int setEntityItem(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, int i, ItemStack itemStack) throws CommandSyntaxException {
        ArrayList<Entity> list = Lists.newArrayListWithCapacity(collection.size());
        for (Entity entity : collection) {
            SlotAccess slotAccess;
            if (entity instanceof ServerPlayer) {
                ((ServerPlayer)entity).inventoryMenu.broadcastChanges();
            }
            if ((slotAccess = entity.getSlot(i)) == SlotAccess.NULL || !slotAccess.set(itemStack.copy())) continue;
            list.add(entity);
            if (!(entity instanceof ServerPlayer)) continue;
            ((ServerPlayer)entity).inventoryMenu.broadcastChanges();
        }
        if (list.isEmpty()) {
            throw ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create(itemStack.getDisplayName(), i);
        }
        if (list.size() == 1) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.item.entity.set.success.single", ((Entity)list.iterator().next()).getDisplayName(), itemStack.getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.item.entity.set.success.multiple", list.size(), itemStack.getDisplayName()), true);
        }
        return list.size();
    }

    private static int blockToEntities(CommandSourceStack commandSourceStack, BlockPos blockPos, int i, Collection<? extends Entity> collection, int j) throws CommandSyntaxException {
        return ItemCommands.setEntityItem(commandSourceStack, collection, j, ItemCommands.getBlockItem(commandSourceStack, blockPos, i));
    }

    private static int blockToEntities(CommandSourceStack commandSourceStack, BlockPos blockPos, int i, Collection<? extends Entity> collection, int j, LootItemFunction lootItemFunction) throws CommandSyntaxException {
        return ItemCommands.setEntityItem(commandSourceStack, collection, j, ItemCommands.applyModifier(commandSourceStack, lootItemFunction, ItemCommands.getBlockItem(commandSourceStack, blockPos, i)));
    }

    private static int blockToBlock(CommandSourceStack commandSourceStack, BlockPos blockPos, int i, BlockPos blockPos2, int j) throws CommandSyntaxException {
        return ItemCommands.setBlockItem(commandSourceStack, blockPos2, j, ItemCommands.getBlockItem(commandSourceStack, blockPos, i));
    }

    private static int blockToBlock(CommandSourceStack commandSourceStack, BlockPos blockPos, int i, BlockPos blockPos2, int j, LootItemFunction lootItemFunction) throws CommandSyntaxException {
        return ItemCommands.setBlockItem(commandSourceStack, blockPos2, j, ItemCommands.applyModifier(commandSourceStack, lootItemFunction, ItemCommands.getBlockItem(commandSourceStack, blockPos, i)));
    }

    private static int entityToBlock(CommandSourceStack commandSourceStack, Entity entity, int i, BlockPos blockPos, int j) throws CommandSyntaxException {
        return ItemCommands.setBlockItem(commandSourceStack, blockPos, j, ItemCommands.getEntityItem(entity, i));
    }

    private static int entityToBlock(CommandSourceStack commandSourceStack, Entity entity, int i, BlockPos blockPos, int j, LootItemFunction lootItemFunction) throws CommandSyntaxException {
        return ItemCommands.setBlockItem(commandSourceStack, blockPos, j, ItemCommands.applyModifier(commandSourceStack, lootItemFunction, ItemCommands.getEntityItem(entity, i)));
    }

    private static int entityToEntities(CommandSourceStack commandSourceStack, Entity entity, int i, Collection<? extends Entity> collection, int j) throws CommandSyntaxException {
        return ItemCommands.setEntityItem(commandSourceStack, collection, j, ItemCommands.getEntityItem(entity, i));
    }

    private static int entityToEntities(CommandSourceStack commandSourceStack, Entity entity, int i, Collection<? extends Entity> collection, int j, LootItemFunction lootItemFunction) throws CommandSyntaxException {
        return ItemCommands.setEntityItem(commandSourceStack, collection, j, ItemCommands.applyModifier(commandSourceStack, lootItemFunction, ItemCommands.getEntityItem(entity, i)));
    }

    private static ItemStack applyModifier(CommandSourceStack commandSourceStack, LootItemFunction lootItemFunction, ItemStack itemStack) {
        ServerLevel serverLevel = commandSourceStack.getLevel();
        LootContext.Builder builder = new LootContext.Builder(serverLevel).withParameter(LootContextParams.ORIGIN, commandSourceStack.getPosition()).withOptionalParameter(LootContextParams.THIS_ENTITY, commandSourceStack.getEntity());
        return (ItemStack)lootItemFunction.apply(itemStack, builder.create(LootContextParamSets.COMMAND));
    }

    private static ItemStack getEntityItem(Entity entity, int i) throws CommandSyntaxException {
        SlotAccess slotAccess = entity.getSlot(i);
        if (slotAccess == SlotAccess.NULL) {
            throw ERROR_SOURCE_INAPPLICABLE_SLOT.create(i);
        }
        return slotAccess.get().copy();
    }

    private static ItemStack getBlockItem(CommandSourceStack commandSourceStack, BlockPos blockPos, int i) throws CommandSyntaxException {
        Container container = ItemCommands.getContainer(commandSourceStack, blockPos, ERROR_SOURCE_NOT_A_CONTAINER);
        if (i < 0 || i >= container.getContainerSize()) {
            throw ERROR_SOURCE_INAPPLICABLE_SLOT.create(i);
        }
        return container.getItem(i).copy();
    }
}

