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
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.UnopenedPack;

public class DataPackCommand {
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_PACK = new DynamicCommandExceptionType(object -> new TranslatableComponent("commands.datapack.unknown", object));
    private static final DynamicCommandExceptionType ERROR_PACK_ALREADY_ENABLED = new DynamicCommandExceptionType(object -> new TranslatableComponent("commands.datapack.enable.failed", object));
    private static final DynamicCommandExceptionType ERROR_PACK_ALREADY_DISABLED = new DynamicCommandExceptionType(object -> new TranslatableComponent("commands.datapack.disable.failed", object));
    private static final SuggestionProvider<CommandSourceStack> SELECTED_PACKS = (commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(((CommandSourceStack)commandContext.getSource()).getServer().getPackRepository().getSelectedIds().stream().map(StringArgumentType::escapeIfRequired), suggestionsBuilder);
    private static final SuggestionProvider<CommandSourceStack> AVAILABLE_PACKS = (commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(((CommandSourceStack)commandContext.getSource()).getServer().getPackRepository().getAvailableIds().stream().map(StringArgumentType::escapeIfRequired), suggestionsBuilder);

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("datapack").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.literal("enable").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("name", StringArgumentType.string()).suggests(AVAILABLE_PACKS).executes(commandContext -> DataPackCommand.enablePack((CommandSourceStack)commandContext.getSource(), DataPackCommand.getPack(commandContext, "name", true), (list, unopenedPack2) -> unopenedPack2.getDefaultPosition().insert(list, unopenedPack2, unopenedPack -> unopenedPack, false)))).then(Commands.literal("after").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("existing", StringArgumentType.string()).suggests(SELECTED_PACKS).executes(commandContext -> DataPackCommand.enablePack((CommandSourceStack)commandContext.getSource(), DataPackCommand.getPack(commandContext, "name", true), (list, unopenedPack) -> list.add(list.indexOf(DataPackCommand.getPack(commandContext, "existing", false)) + 1, unopenedPack)))))).then(Commands.literal("before").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("existing", StringArgumentType.string()).suggests(SELECTED_PACKS).executes(commandContext -> DataPackCommand.enablePack((CommandSourceStack)commandContext.getSource(), DataPackCommand.getPack(commandContext, "name", true), (list, unopenedPack) -> list.add(list.indexOf(DataPackCommand.getPack(commandContext, "existing", false)), unopenedPack)))))).then(Commands.literal("last").executes(commandContext -> DataPackCommand.enablePack((CommandSourceStack)commandContext.getSource(), DataPackCommand.getPack(commandContext, "name", true), List::add)))).then(Commands.literal("first").executes(commandContext -> DataPackCommand.enablePack((CommandSourceStack)commandContext.getSource(), DataPackCommand.getPack(commandContext, "name", true), (list, unopenedPack) -> list.add(0, unopenedPack))))))).then(Commands.literal("disable").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("name", StringArgumentType.string()).suggests(SELECTED_PACKS).executes(commandContext -> DataPackCommand.disablePack((CommandSourceStack)commandContext.getSource(), DataPackCommand.getPack(commandContext, "name", false)))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("list").executes(commandContext -> DataPackCommand.listPacks((CommandSourceStack)commandContext.getSource()))).then(Commands.literal("available").executes(commandContext -> DataPackCommand.listAvailablePacks((CommandSourceStack)commandContext.getSource())))).then(Commands.literal("enabled").executes(commandContext -> DataPackCommand.listEnabledPacks((CommandSourceStack)commandContext.getSource())))));
    }

    private static int enablePack(CommandSourceStack commandSourceStack, UnopenedPack unopenedPack, Inserter inserter) throws CommandSyntaxException {
        PackRepository<UnopenedPack> packRepository = commandSourceStack.getServer().getPackRepository();
        ArrayList<UnopenedPack> list = Lists.newArrayList(packRepository.getSelectedPacks());
        inserter.apply(list, unopenedPack);
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.datapack.enable.success", unopenedPack.getChatLink(true)), true);
        ReloadCommand.reloadPacks(list.stream().map(UnopenedPack::getId).collect(Collectors.toList()), commandSourceStack);
        return list.size();
    }

    private static int disablePack(CommandSourceStack commandSourceStack, UnopenedPack unopenedPack) {
        PackRepository<UnopenedPack> packRepository = commandSourceStack.getServer().getPackRepository();
        ArrayList<UnopenedPack> list = Lists.newArrayList(packRepository.getSelectedPacks());
        list.remove(unopenedPack);
        ReloadCommand.reloadPacks(list.stream().map(UnopenedPack::getId).collect(Collectors.toList()), commandSourceStack);
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.datapack.disable.success", unopenedPack.getChatLink(true)), true);
        return list.size();
    }

    private static int listPacks(CommandSourceStack commandSourceStack) {
        return DataPackCommand.listEnabledPacks(commandSourceStack) + DataPackCommand.listAvailablePacks(commandSourceStack);
    }

    private static int listAvailablePacks(CommandSourceStack commandSourceStack) {
        PackRepository<UnopenedPack> packRepository = commandSourceStack.getServer().getPackRepository();
        packRepository.reload();
        Collection<UnopenedPack> collection = packRepository.getSelectedPacks();
        Collection<UnopenedPack> collection2 = packRepository.getAvailablePacks();
        List list = collection2.stream().filter(unopenedPack -> !collection.contains(unopenedPack)).collect(Collectors.toList());
        if (list.isEmpty()) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.datapack.list.available.none"), false);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.datapack.list.available.success", list.size(), ComponentUtils.formatList(list, unopenedPack -> unopenedPack.getChatLink(false))), false);
        }
        return list.size();
    }

    private static int listEnabledPacks(CommandSourceStack commandSourceStack) {
        PackRepository<UnopenedPack> packRepository = commandSourceStack.getServer().getPackRepository();
        packRepository.reload();
        Collection<UnopenedPack> collection = packRepository.getSelectedPacks();
        if (collection.isEmpty()) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.datapack.list.enabled.none"), false);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.datapack.list.enabled.success", collection.size(), ComponentUtils.formatList(collection, unopenedPack -> unopenedPack.getChatLink(true))), false);
        }
        return collection.size();
    }

    private static UnopenedPack getPack(CommandContext<CommandSourceStack> commandContext, String string, boolean bl) throws CommandSyntaxException {
        String string2 = StringArgumentType.getString(commandContext, string);
        PackRepository<UnopenedPack> packRepository = commandContext.getSource().getServer().getPackRepository();
        UnopenedPack unopenedPack = packRepository.getPack(string2);
        if (unopenedPack == null) {
            throw ERROR_UNKNOWN_PACK.create(string2);
        }
        boolean bl2 = packRepository.getSelectedPacks().contains(unopenedPack);
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

