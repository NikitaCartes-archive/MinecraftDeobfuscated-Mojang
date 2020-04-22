package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.UnopenedPack;
import net.minecraft.world.level.storage.WorldData;

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
			commandContext.getSource().getServer().getPackRepository().getSelected().stream().map(UnopenedPack::getId).map(StringArgumentType::escapeIfRequired),
			suggestionsBuilder
		);
	private static final SuggestionProvider<CommandSourceStack> AVAILABLE_PACKS = (commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(
			commandContext.getSource().getServer().getPackRepository().getUnselected().stream().map(UnopenedPack::getId).map(StringArgumentType::escapeIfRequired),
			suggestionsBuilder
		);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("datapack")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.literal("enable")
						.then(
							Commands.argument("name", StringArgumentType.string())
								.suggests(AVAILABLE_PACKS)
								.executes(
									commandContext -> enablePack(
											commandContext.getSource(),
											getPack(commandContext, "name", true),
											(list, unopenedPack) -> unopenedPack.getDefaultPosition().insert(list, unopenedPack, unopenedPackx -> unopenedPackx, false)
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
															(list, unopenedPack) -> list.add(list.indexOf(getPack(commandContext, "existing", false)) + 1, unopenedPack)
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
															(list, unopenedPack) -> list.add(list.indexOf(getPack(commandContext, "existing", false)), unopenedPack)
														)
												)
										)
								)
								.then(Commands.literal("last").executes(commandContext -> enablePack(commandContext.getSource(), getPack(commandContext, "name", true), List::add)))
								.then(
									Commands.literal("first")
										.executes(
											commandContext -> enablePack(commandContext.getSource(), getPack(commandContext, "name", true), (list, unopenedPack) -> list.add(0, unopenedPack))
										)
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

	private static int enablePack(CommandSourceStack commandSourceStack, UnopenedPack unopenedPack, DataPackCommand.Inserter inserter) throws CommandSyntaxException {
		PackRepository<UnopenedPack> packRepository = commandSourceStack.getServer().getPackRepository();
		List<UnopenedPack> list = Lists.<UnopenedPack>newArrayList(packRepository.getSelected());
		inserter.apply(list, unopenedPack);
		packRepository.setSelected(list);
		WorldData worldData = commandSourceStack.getServer().getWorldData();
		worldData.getEnabledDataPacks().clear();
		packRepository.getSelected().forEach(unopenedPackx -> worldData.getEnabledDataPacks().add(unopenedPackx.getId()));
		worldData.getDisabledDataPacks().remove(unopenedPack.getId());
		commandSourceStack.sendSuccess(new TranslatableComponent("commands.datapack.enable.success", unopenedPack.getChatLink(true)), true);
		commandSourceStack.getServer().reloadResources();
		return packRepository.getSelected().size();
	}

	private static int disablePack(CommandSourceStack commandSourceStack, UnopenedPack unopenedPack) {
		PackRepository<UnopenedPack> packRepository = commandSourceStack.getServer().getPackRepository();
		List<UnopenedPack> list = Lists.<UnopenedPack>newArrayList(packRepository.getSelected());
		list.remove(unopenedPack);
		packRepository.setSelected(list);
		WorldData worldData = commandSourceStack.getServer().getWorldData();
		worldData.getEnabledDataPacks().clear();
		packRepository.getSelected().forEach(unopenedPackx -> worldData.getEnabledDataPacks().add(unopenedPackx.getId()));
		worldData.getDisabledDataPacks().add(unopenedPack.getId());
		commandSourceStack.sendSuccess(new TranslatableComponent("commands.datapack.disable.success", unopenedPack.getChatLink(true)), true);
		commandSourceStack.getServer().reloadResources();
		return packRepository.getSelected().size();
	}

	private static int listPacks(CommandSourceStack commandSourceStack) {
		return listEnabledPacks(commandSourceStack) + listAvailablePacks(commandSourceStack);
	}

	private static int listAvailablePacks(CommandSourceStack commandSourceStack) {
		PackRepository<UnopenedPack> packRepository = commandSourceStack.getServer().getPackRepository();
		if (packRepository.getUnselected().isEmpty()) {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.datapack.list.available.none"), false);
		} else {
			commandSourceStack.sendSuccess(
				new TranslatableComponent(
					"commands.datapack.list.available.success",
					packRepository.getUnselected().size(),
					ComponentUtils.formatList(packRepository.getUnselected(), unopenedPack -> unopenedPack.getChatLink(false))
				),
				false
			);
		}

		return packRepository.getUnselected().size();
	}

	private static int listEnabledPacks(CommandSourceStack commandSourceStack) {
		PackRepository<UnopenedPack> packRepository = commandSourceStack.getServer().getPackRepository();
		if (packRepository.getSelected().isEmpty()) {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.datapack.list.enabled.none"), false);
		} else {
			commandSourceStack.sendSuccess(
				new TranslatableComponent(
					"commands.datapack.list.enabled.success",
					packRepository.getSelected().size(),
					ComponentUtils.formatList(packRepository.getSelected(), unopenedPack -> unopenedPack.getChatLink(true))
				),
				false
			);
		}

		return packRepository.getSelected().size();
	}

	private static UnopenedPack getPack(CommandContext<CommandSourceStack> commandContext, String string, boolean bl) throws CommandSyntaxException {
		String string2 = StringArgumentType.getString(commandContext, string);
		PackRepository<UnopenedPack> packRepository = commandContext.getSource().getServer().getPackRepository();
		UnopenedPack unopenedPack = packRepository.getPack(string2);
		if (unopenedPack == null) {
			throw ERROR_UNKNOWN_PACK.create(string2);
		} else {
			boolean bl2 = packRepository.getSelected().contains(unopenedPack);
			if (bl && bl2) {
				throw ERROR_PACK_ALREADY_ENABLED.create(string2);
			} else if (!bl && !bl2) {
				throw ERROR_PACK_ALREADY_DISABLED.create(string2);
			} else {
				return unopenedPack;
			}
		}
	}

	interface Inserter {
		void apply(List<UnopenedPack> list, UnopenedPack unopenedPack) throws CommandSyntaxException;
	}
}
