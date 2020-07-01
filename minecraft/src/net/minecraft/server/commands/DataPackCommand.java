package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;

public class DataPackCommand {
	private static final DynamicCommandExceptionType ERROR_UNKNOWN_PACK = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.datapack.unknown", object)
	);
	private static final DynamicCommandExceptionType ERROR_PACK_ALREADY_ENABLED = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.datapack.enable.failed", object)
	);
	private static final DynamicCommandExceptionType ERROR_PACK_ALREADY_DISABLED = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.datapack.disable.failed", object)
	);
	private static final SuggestionProvider<CommandSourceStack> SELECTED_PACKS = (commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(
			commandContext.getSource().getServer().getPackRepository().getSelectedIds().stream().map(StringArgumentType::escapeIfRequired), suggestionsBuilder
		);
	private static final SuggestionProvider<CommandSourceStack> UNSELECTED_PACKS = (commandContext, suggestionsBuilder) -> {
		PackRepository packRepository = commandContext.getSource().getServer().getPackRepository();
		Collection<String> collection = packRepository.getSelectedIds();
		return SharedSuggestionProvider.suggest(
			packRepository.getAvailableIds().stream().filter(string -> !collection.contains(string)).map(StringArgumentType::escapeIfRequired), suggestionsBuilder
		);
	};

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("datapack")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.literal("enable")
						.then(
							Commands.argument("name", StringArgumentType.string())
								.suggests(UNSELECTED_PACKS)
								.executes(
									commandContext -> enablePack(
											commandContext.getSource(),
											getPack(commandContext, "name", true),
											(list, pack) -> pack.getDefaultPosition().insert(list, pack, packx -> packx, false)
										)
								)
								.then(
									Commands.literal("after")
										.then(
											Commands.argument("existing", StringArgumentType.string())
												.suggests(SELECTED_PACKS)
												.executes(
													commandContext -> enablePack(
															commandContext.getSource(),
															getPack(commandContext, "name", true),
															(list, pack) -> list.add(list.indexOf(getPack(commandContext, "existing", false)) + 1, pack)
														)
												)
										)
								)
								.then(
									Commands.literal("before")
										.then(
											Commands.argument("existing", StringArgumentType.string())
												.suggests(SELECTED_PACKS)
												.executes(
													commandContext -> enablePack(
															commandContext.getSource(),
															getPack(commandContext, "name", true),
															(list, pack) -> list.add(list.indexOf(getPack(commandContext, "existing", false)), pack)
														)
												)
										)
								)
								.then(Commands.literal("last").executes(commandContext -> enablePack(commandContext.getSource(), getPack(commandContext, "name", true), List::add)))
								.then(
									Commands.literal("first")
										.executes(commandContext -> enablePack(commandContext.getSource(), getPack(commandContext, "name", true), (list, pack) -> list.add(0, pack)))
								)
						)
				)
				.then(
					Commands.literal("disable")
						.then(
							Commands.argument("name", StringArgumentType.string())
								.suggests(SELECTED_PACKS)
								.executes(commandContext -> disablePack(commandContext.getSource(), getPack(commandContext, "name", false)))
						)
				)
				.then(
					Commands.literal("list")
						.executes(commandContext -> listPacks(commandContext.getSource()))
						.then(Commands.literal("available").executes(commandContext -> listAvailablePacks(commandContext.getSource())))
						.then(Commands.literal("enabled").executes(commandContext -> listEnabledPacks(commandContext.getSource())))
				)
		);
	}

	private static int enablePack(CommandSourceStack commandSourceStack, Pack pack, DataPackCommand.Inserter inserter) throws CommandSyntaxException {
		PackRepository packRepository = commandSourceStack.getServer().getPackRepository();
		List<Pack> list = Lists.<Pack>newArrayList(packRepository.getSelectedPacks());
		inserter.apply(list, pack);
		commandSourceStack.sendSuccess(new TranslatableComponent("commands.datapack.modify.enable", pack.getChatLink(true)), true);
		ReloadCommand.reloadPacks((Collection<String>)list.stream().map(Pack::getId).collect(Collectors.toList()), commandSourceStack);
		return list.size();
	}

	private static int disablePack(CommandSourceStack commandSourceStack, Pack pack) {
		PackRepository packRepository = commandSourceStack.getServer().getPackRepository();
		List<Pack> list = Lists.<Pack>newArrayList(packRepository.getSelectedPacks());
		list.remove(pack);
		commandSourceStack.sendSuccess(new TranslatableComponent("commands.datapack.modify.disable", pack.getChatLink(true)), true);
		ReloadCommand.reloadPacks((Collection<String>)list.stream().map(Pack::getId).collect(Collectors.toList()), commandSourceStack);
		return list.size();
	}

	private static int listPacks(CommandSourceStack commandSourceStack) {
		return listEnabledPacks(commandSourceStack) + listAvailablePacks(commandSourceStack);
	}

	private static int listAvailablePacks(CommandSourceStack commandSourceStack) {
		PackRepository packRepository = commandSourceStack.getServer().getPackRepository();
		packRepository.reload();
		Collection<? extends Pack> collection = packRepository.getSelectedPacks();
		Collection<? extends Pack> collection2 = packRepository.getAvailablePacks();
		List<Pack> list = (List<Pack>)collection2.stream().filter(pack -> !collection.contains(pack)).collect(Collectors.toList());
		if (list.isEmpty()) {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.datapack.list.available.none"), false);
		} else {
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.datapack.list.available.success", list.size(), ComponentUtils.formatList(list, pack -> pack.getChatLink(false))), false
			);
		}

		return list.size();
	}

	private static int listEnabledPacks(CommandSourceStack commandSourceStack) {
		PackRepository packRepository = commandSourceStack.getServer().getPackRepository();
		packRepository.reload();
		Collection<? extends Pack> collection = packRepository.getSelectedPacks();
		if (collection.isEmpty()) {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.datapack.list.enabled.none"), false);
		} else {
			commandSourceStack.sendSuccess(
				new TranslatableComponent(
					"commands.datapack.list.enabled.success", collection.size(), ComponentUtils.formatList(collection, pack -> pack.getChatLink(true))
				),
				false
			);
		}

		return collection.size();
	}

	private static Pack getPack(CommandContext<CommandSourceStack> commandContext, String string, boolean bl) throws CommandSyntaxException {
		String string2 = StringArgumentType.getString(commandContext, string);
		PackRepository packRepository = commandContext.getSource().getServer().getPackRepository();
		Pack pack = packRepository.getPack(string2);
		if (pack == null) {
			throw ERROR_UNKNOWN_PACK.create(string2);
		} else {
			boolean bl2 = packRepository.getSelectedPacks().contains(pack);
			if (bl && bl2) {
				throw ERROR_PACK_ALREADY_ENABLED.create(string2);
			} else if (!bl && !bl2) {
				throw ERROR_PACK_ALREADY_DISABLED.create(string2);
			} else {
				return pack;
			}
		}
	}

	interface Inserter {
		void apply(List<Pack> list, Pack pack) throws CommandSyntaxException;
	}
}
