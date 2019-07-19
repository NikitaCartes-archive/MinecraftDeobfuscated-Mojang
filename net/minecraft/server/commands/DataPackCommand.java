/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.UnopenedPack;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;

public class DataPackCommand {
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_PACK = new DynamicCommandExceptionType(object -> new TranslatableComponent("commands.datapack.unknown", object));
    private static final DynamicCommandExceptionType ERROR_PACK_ALREADY_ENABLED = new DynamicCommandExceptionType(object -> new TranslatableComponent("commands.datapack.enable.failed", object));
    private static final DynamicCommandExceptionType ERROR_PACK_ALREADY_DISABLED = new DynamicCommandExceptionType(object -> new TranslatableComponent("commands.datapack.disable.failed", object));
    private static final SuggestionProvider<CommandSourceStack> SELECTED_PACKS = (commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(((CommandSourceStack)commandContext.getSource()).getServer().getPackRepository().getSelected().stream().map(UnopenedPack::getId).map(StringArgumentType::escapeIfRequired), suggestionsBuilder);
    private static final SuggestionProvider<CommandSourceStack> AVAILABLE_PACKS = (commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(((CommandSourceStack)commandContext.getSource()).getServer().getPackRepository().getUnselected().stream().map(UnopenedPack::getId).map(StringArgumentType::escapeIfRequired), suggestionsBuilder);

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("datapack").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.literal("enable").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("name", StringArgumentType.string()).suggests(AVAILABLE_PACKS).executes(commandContext -> DataPackCommand.enablePack((CommandSourceStack)commandContext.getSource(), DataPackCommand.getPack(commandContext, "name", true), (list, unopenedPack2) -> unopenedPack2.getDefaultPosition().insert(list, unopenedPack2, unopenedPack -> unopenedPack, false)))).then(Commands.literal("after").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("existing", StringArgumentType.string()).suggests(SELECTED_PACKS).executes(commandContext -> DataPackCommand.enablePack((CommandSourceStack)commandContext.getSource(), DataPackCommand.getPack(commandContext, "name", true), (list, unopenedPack) -> list.add(list.indexOf(DataPackCommand.getPack(commandContext, "existing", false)) + 1, unopenedPack)))))).then(Commands.literal("before").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("existing", StringArgumentType.string()).suggests(SELECTED_PACKS).executes(commandContext -> DataPackCommand.enablePack((CommandSourceStack)commandContext.getSource(), DataPackCommand.getPack(commandContext, "name", true), (list, unopenedPack) -> list.add(list.indexOf(DataPackCommand.getPack(commandContext, "existing", false)), unopenedPack)))))).then(Commands.literal("last").executes(commandContext -> DataPackCommand.enablePack((CommandSourceStack)commandContext.getSource(), DataPackCommand.getPack(commandContext, "name", true), List::add)))).then(Commands.literal("first").executes(commandContext -> DataPackCommand.enablePack((CommandSourceStack)commandContext.getSource(), DataPackCommand.getPack(commandContext, "name", true), (list, unopenedPack) -> list.add(0, unopenedPack))))))).then(Commands.literal("disable").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("name", StringArgumentType.string()).suggests(SELECTED_PACKS).executes(commandContext -> DataPackCommand.disablePack((CommandSourceStack)commandContext.getSource(), DataPackCommand.getPack(commandContext, "name", false)))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("list").executes(commandContext -> DataPackCommand.listPacks((CommandSourceStack)commandContext.getSource()))).then(Commands.literal("available").executes(commandContext -> DataPackCommand.listAvailablePacks((CommandSourceStack)commandContext.getSource())))).then(Commands.literal("enabled").executes(commandContext -> DataPackCommand.listEnabledPacks((CommandSourceStack)commandContext.getSource())))));
    }

    private static int enablePack(CommandSourceStack commandSourceStack, UnopenedPack unopenedPack2, Inserter inserter) throws CommandSyntaxException {
        PackRepository<UnopenedPack> packRepository = commandSourceStack.getServer().getPackRepository();
        ArrayList<UnopenedPack> list = Lists.newArrayList(packRepository.getSelected());
        inserter.apply(list, unopenedPack2);
        packRepository.setSelected(list);
        LevelData levelData = commandSourceStack.getServer().getLevel(DimensionType.OVERWORLD).getLevelData();
        levelData.getEnabledDataPacks().clear();
        packRepository.getSelected().forEach(unopenedPack -> levelData.getEnabledDataPacks().add(unopenedPack.getId()));
        levelData.getDisabledDataPacks().remove(unopenedPack2.getId());
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.datapack.enable.success", unopenedPack2.getChatLink(true)), true);
        commandSourceStack.getServer().reloadResources();
        return packRepository.getSelected().size();
    }

    private static int disablePack(CommandSourceStack commandSourceStack, UnopenedPack unopenedPack2) {
        PackRepository<UnopenedPack> packRepository = commandSourceStack.getServer().getPackRepository();
        ArrayList<UnopenedPack> list = Lists.newArrayList(packRepository.getSelected());
        list.remove(unopenedPack2);
        packRepository.setSelected(list);
        LevelData levelData = commandSourceStack.getServer().getLevel(DimensionType.OVERWORLD).getLevelData();
        levelData.getEnabledDataPacks().clear();
        packRepository.getSelected().forEach(unopenedPack -> levelData.getEnabledDataPacks().add(unopenedPack.getId()));
        levelData.getDisabledDataPacks().add(unopenedPack2.getId());
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.datapack.disable.success", unopenedPack2.getChatLink(true)), true);
        commandSourceStack.getServer().reloadResources();
        return packRepository.getSelected().size();
    }

    private static int listPacks(CommandSourceStack commandSourceStack) {
        return DataPackCommand.listEnabledPacks(commandSourceStack) + DataPackCommand.listAvailablePacks(commandSourceStack);
    }

    private static int listAvailablePacks(CommandSourceStack commandSourceStack) {
        PackRepository<UnopenedPack> packRepository = commandSourceStack.getServer().getPackRepository();
        if (packRepository.getUnselected().isEmpty()) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.datapack.list.available.none", new Object[0]), false);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.datapack.list.available.success", packRepository.getUnselected().size(), ComponentUtils.formatList(packRepository.getUnselected(), unopenedPack -> unopenedPack.getChatLink(false))), false);
        }
        return packRepository.getUnselected().size();
    }

    private static int listEnabledPacks(CommandSourceStack commandSourceStack) {
        PackRepository<UnopenedPack> packRepository = commandSourceStack.getServer().getPackRepository();
        if (packRepository.getSelected().isEmpty()) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.datapack.list.enabled.none", new Object[0]), false);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.datapack.list.enabled.success", packRepository.getSelected().size(), ComponentUtils.formatList(packRepository.getSelected(), unopenedPack -> unopenedPack.getChatLink(true))), false);
        }
        return packRepository.getSelected().size();
    }

    private static UnopenedPack getPack(CommandContext<CommandSourceStack> commandContext, String string, boolean bl) throws CommandSyntaxException {
        String string2 = StringArgumentType.getString(commandContext, string);
        PackRepository<UnopenedPack> packRepository = commandContext.getSource().getServer().getPackRepository();
        UnopenedPack unopenedPack = packRepository.getPack(string2);
        if (unopenedPack == null) {
            throw ERROR_UNKNOWN_PACK.create(string2);
        }
        boolean bl2 = packRepository.getSelected().contains(unopenedPack);
        if (bl && bl2) {
            throw ERROR_PACK_ALREADY_ENABLED.create(string2);
        }
        if (!bl && !bl2) {
            throw ERROR_PACK_ALREADY_DISABLED.create(string2);
        }
        return unopenedPack;
    }

    static interface Inserter {
        public void apply(List<UnopenedPack> var1, UnopenedPack var2) throws CommandSyntaxException;
    }
}

