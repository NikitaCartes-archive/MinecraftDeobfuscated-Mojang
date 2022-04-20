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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;

public class DataPackCommand {
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_PACK = new DynamicCommandExceptionType(object -> Component.translatable("commands.datapack.unknown", object));
    private static final DynamicCommandExceptionType ERROR_PACK_ALREADY_ENABLED = new DynamicCommandExceptionType(object -> Component.translatable("commands.datapack.enable.failed", object));
    private static final DynamicCommandExceptionType ERROR_PACK_ALREADY_DISABLED = new DynamicCommandExceptionType(object -> Component.translatable("commands.datapack.disable.failed", object));
    private static final SuggestionProvider<CommandSourceStack> SELECTED_PACKS = (commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(((CommandSourceStack)commandContext.getSource()).getServer().getPackRepository().getSelectedIds().stream().map(StringArgumentType::escapeIfRequired), suggestionsBuilder);
    private static final SuggestionProvider<CommandSourceStack> UNSELECTED_PACKS = (commandContext, suggestionsBuilder) -> {
        PackRepository packRepository = ((CommandSourceStack)commandContext.getSource()).getServer().getPackRepository();
        Collection<String> collection = packRepository.getSelectedIds();
        return SharedSuggestionProvider.suggest(packRepository.getAvailableIds().stream().filter(string -> !collection.contains(string)).map(StringArgumentType::escapeIfRequired), suggestionsBuilder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("datapack").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.literal("enable").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("name", StringArgumentType.string()).suggests(UNSELECTED_PACKS).executes(commandContext -> DataPackCommand.enablePack((CommandSourceStack)commandContext.getSource(), DataPackCommand.getPack(commandContext, "name", true), (list, pack2) -> pack2.getDefaultPosition().insert(list, pack2, pack -> pack, false)))).then(Commands.literal("after").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("existing", StringArgumentType.string()).suggests(SELECTED_PACKS).executes(commandContext -> DataPackCommand.enablePack((CommandSourceStack)commandContext.getSource(), DataPackCommand.getPack(commandContext, "name", true), (list, pack) -> list.add(list.indexOf(DataPackCommand.getPack(commandContext, "existing", false)) + 1, pack)))))).then(Commands.literal("before").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("existing", StringArgumentType.string()).suggests(SELECTED_PACKS).executes(commandContext -> DataPackCommand.enablePack((CommandSourceStack)commandContext.getSource(), DataPackCommand.getPack(commandContext, "name", true), (list, pack) -> list.add(list.indexOf(DataPackCommand.getPack(commandContext, "existing", false)), pack)))))).then(Commands.literal("last").executes(commandContext -> DataPackCommand.enablePack((CommandSourceStack)commandContext.getSource(), DataPackCommand.getPack(commandContext, "name", true), List::add)))).then(Commands.literal("first").executes(commandContext -> DataPackCommand.enablePack((CommandSourceStack)commandContext.getSource(), DataPackCommand.getPack(commandContext, "name", true), (list, pack) -> list.add(0, pack))))))).then(Commands.literal("disable").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("name", StringArgumentType.string()).suggests(SELECTED_PACKS).executes(commandContext -> DataPackCommand.disablePack((CommandSourceStack)commandContext.getSource(), DataPackCommand.getPack(commandContext, "name", false)))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("list").executes(commandContext -> DataPackCommand.listPacks((CommandSourceStack)commandContext.getSource()))).then(Commands.literal("available").executes(commandContext -> DataPackCommand.listAvailablePacks((CommandSourceStack)commandContext.getSource())))).then(Commands.literal("enabled").executes(commandContext -> DataPackCommand.listEnabledPacks((CommandSourceStack)commandContext.getSource())))));
    }

    private static int enablePack(CommandSourceStack commandSourceStack, Pack pack, Inserter inserter) throws CommandSyntaxException {
        PackRepository packRepository = commandSourceStack.getServer().getPackRepository();
        ArrayList<Pack> list = Lists.newArrayList(packRepository.getSelectedPacks());
        inserter.apply(list, pack);
        commandSourceStack.sendSuccess(Component.translatable("commands.datapack.modify.enable", pack.getChatLink(true)), true);
        ReloadCommand.reloadPacks(list.stream().map(Pack::getId).collect(Collectors.toList()), commandSourceStack);
        return list.size();
    }

    private static int disablePack(CommandSourceStack commandSourceStack, Pack pack) {
        PackRepository packRepository = commandSourceStack.getServer().getPackRepository();
        ArrayList<Pack> list = Lists.newArrayList(packRepository.getSelectedPacks());
        list.remove(pack);
        commandSourceStack.sendSuccess(Component.translatable("commands.datapack.modify.disable", pack.getChatLink(true)), true);
        ReloadCommand.reloadPacks(list.stream().map(Pack::getId).collect(Collectors.toList()), commandSourceStack);
        return list.size();
    }

    private static int listPacks(CommandSourceStack commandSourceStack) {
        return DataPackCommand.listEnabledPacks(commandSourceStack) + DataPackCommand.listAvailablePacks(commandSourceStack);
    }

    private static int listAvailablePacks(CommandSourceStack commandSourceStack) {
        PackRepository packRepository = commandSourceStack.getServer().getPackRepository();
        packRepository.reload();
        Collection<Pack> collection = packRepository.getSelectedPacks();
        Collection<Pack> collection2 = packRepository.getAvailablePacks();
        List list = collection2.stream().filter(pack -> !collection.contains(pack)).collect(Collectors.toList());
        if (list.isEmpty()) {
            commandSourceStack.sendSuccess(Component.translatable("commands.datapack.list.available.none"), false);
        } else {
            commandSourceStack.sendSuccess(Component.translatable("commands.datapack.list.available.success", list.size(), ComponentUtils.formatList(list, pack -> pack.getChatLink(false))), false);
        }
        return list.size();
    }

    private static int listEnabledPacks(CommandSourceStack commandSourceStack) {
        PackRepository packRepository = commandSourceStack.getServer().getPackRepository();
        packRepository.reload();
        Collection<Pack> collection = packRepository.getSelectedPacks();
        if (collection.isEmpty()) {
            commandSourceStack.sendSuccess(Component.translatable("commands.datapack.list.enabled.none"), false);
        } else {
            commandSourceStack.sendSuccess(Component.translatable("commands.datapack.list.enabled.success", collection.size(), ComponentUtils.formatList(collection, pack -> pack.getChatLink(true))), false);
        }
        return collection.size();
    }

    private static Pack getPack(CommandContext<CommandSourceStack> commandContext, String string, boolean bl) throws CommandSyntaxException {
        String string2 = StringArgumentType.getString(commandContext, string);
        PackRepository packRepository = commandContext.getSource().getServer().getPackRepository();
        Pack pack = packRepository.getPack(string2);
        if (pack == null) {
            throw ERROR_UNKNOWN_PACK.create(string2);
        }
        boolean bl2 = packRepository.getSelectedPacks().contains(pack);
        if (bl && bl2) {
            throw ERROR_PACK_ALREADY_ENABLED.create(string2);
        }
        if (!bl && !bl2) {
            throw ERROR_PACK_ALREADY_DISABLED.create(string2);
        }
        return pack;
    }

    static interface Inserter {
        public void apply(List<Pack> var1, Pack var2) throws CommandSyntaxException;
    }
}

