package net.minecraft.commands;

import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.logging.LogUtils;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.RegistryAccess;
import net.minecraft.gametest.framework.TestCommand;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.server.commands.AdvancementCommands;
import net.minecraft.server.commands.AttributeCommand;
import net.minecraft.server.commands.BanIpCommands;
import net.minecraft.server.commands.BanListCommands;
import net.minecraft.server.commands.BanPlayerCommands;
import net.minecraft.server.commands.BossBarCommands;
import net.minecraft.server.commands.ClearInventoryCommands;
import net.minecraft.server.commands.CloneCommands;
import net.minecraft.server.commands.DataPackCommand;
import net.minecraft.server.commands.DeOpCommands;
import net.minecraft.server.commands.DebugCommand;
import net.minecraft.server.commands.DefaultGameModeCommands;
import net.minecraft.server.commands.DifficultyCommand;
import net.minecraft.server.commands.EffectCommands;
import net.minecraft.server.commands.EmoteCommands;
import net.minecraft.server.commands.EnchantCommand;
import net.minecraft.server.commands.ExecuteCommand;
import net.minecraft.server.commands.ExperienceCommand;
import net.minecraft.server.commands.FillCommand;
import net.minecraft.server.commands.ForceLoadCommand;
import net.minecraft.server.commands.FunctionCommand;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.server.commands.GameRuleCommand;
import net.minecraft.server.commands.GiveCommand;
import net.minecraft.server.commands.HelpCommand;
import net.minecraft.server.commands.ItemCommands;
import net.minecraft.server.commands.JfrCommand;
import net.minecraft.server.commands.KickCommand;
import net.minecraft.server.commands.KillCommand;
import net.minecraft.server.commands.ListPlayersCommand;
import net.minecraft.server.commands.LocateCommand;
import net.minecraft.server.commands.LootCommand;
import net.minecraft.server.commands.MsgCommand;
import net.minecraft.server.commands.OpCommand;
import net.minecraft.server.commands.PardonCommand;
import net.minecraft.server.commands.PardonIpCommand;
import net.minecraft.server.commands.ParticleCommand;
import net.minecraft.server.commands.PerfCommand;
import net.minecraft.server.commands.PlaceCommand;
import net.minecraft.server.commands.PlaySoundCommand;
import net.minecraft.server.commands.PublishCommand;
import net.minecraft.server.commands.RecipeCommand;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.commands.SaveAllCommand;
import net.minecraft.server.commands.SaveOffCommand;
import net.minecraft.server.commands.SaveOnCommand;
import net.minecraft.server.commands.SayCommand;
import net.minecraft.server.commands.ScheduleCommand;
import net.minecraft.server.commands.ScoreboardCommand;
import net.minecraft.server.commands.SeedCommand;
import net.minecraft.server.commands.SetBlockCommand;
import net.minecraft.server.commands.SetPlayerIdleTimeoutCommand;
import net.minecraft.server.commands.SetSpawnCommand;
import net.minecraft.server.commands.SetWorldSpawnCommand;
import net.minecraft.server.commands.SpectateCommand;
import net.minecraft.server.commands.SpreadPlayersCommand;
import net.minecraft.server.commands.StopCommand;
import net.minecraft.server.commands.StopSoundCommand;
import net.minecraft.server.commands.SummonCommand;
import net.minecraft.server.commands.TagCommand;
import net.minecraft.server.commands.TeamCommand;
import net.minecraft.server.commands.TeamMsgCommand;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.commands.TellRawCommand;
import net.minecraft.server.commands.TimeCommand;
import net.minecraft.server.commands.TitleCommand;
import net.minecraft.server.commands.TriggerCommand;
import net.minecraft.server.commands.WeatherCommand;
import net.minecraft.server.commands.WhitelistCommand;
import net.minecraft.server.commands.WorldBorderCommand;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class Commands {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final int LEVEL_ALL = 0;
	public static final int LEVEL_MODERATORS = 1;
	public static final int LEVEL_GAMEMASTERS = 2;
	public static final int LEVEL_ADMINS = 3;
	public static final int LEVEL_OWNERS = 4;
	private final CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();

	public Commands(Commands.CommandSelection commandSelection, CommandBuildContext commandBuildContext) {
		AdvancementCommands.register(this.dispatcher);
		AttributeCommand.register(this.dispatcher);
		ExecuteCommand.register(this.dispatcher, commandBuildContext);
		BossBarCommands.register(this.dispatcher);
		ClearInventoryCommands.register(this.dispatcher, commandBuildContext);
		CloneCommands.register(this.dispatcher, commandBuildContext);
		DataCommands.register(this.dispatcher);
		DataPackCommand.register(this.dispatcher);
		DebugCommand.register(this.dispatcher);
		DefaultGameModeCommands.register(this.dispatcher);
		DifficultyCommand.register(this.dispatcher);
		EffectCommands.register(this.dispatcher);
		EmoteCommands.register(this.dispatcher);
		EnchantCommand.register(this.dispatcher);
		ExperienceCommand.register(this.dispatcher);
		FillCommand.register(this.dispatcher, commandBuildContext);
		ForceLoadCommand.register(this.dispatcher);
		FunctionCommand.register(this.dispatcher);
		GameModeCommand.register(this.dispatcher);
		GameRuleCommand.register(this.dispatcher);
		GiveCommand.register(this.dispatcher, commandBuildContext);
		HelpCommand.register(this.dispatcher);
		ItemCommands.register(this.dispatcher, commandBuildContext);
		KickCommand.register(this.dispatcher);
		KillCommand.register(this.dispatcher);
		ListPlayersCommand.register(this.dispatcher);
		LocateCommand.register(this.dispatcher);
		LootCommand.register(this.dispatcher, commandBuildContext);
		MsgCommand.register(this.dispatcher);
		ParticleCommand.register(this.dispatcher);
		PlaceCommand.register(this.dispatcher);
		PlaySoundCommand.register(this.dispatcher);
		ReloadCommand.register(this.dispatcher);
		RecipeCommand.register(this.dispatcher);
		SayCommand.register(this.dispatcher);
		ScheduleCommand.register(this.dispatcher);
		ScoreboardCommand.register(this.dispatcher);
		SeedCommand.register(this.dispatcher, commandSelection != Commands.CommandSelection.INTEGRATED);
		SetBlockCommand.register(this.dispatcher, commandBuildContext);
		SetSpawnCommand.register(this.dispatcher);
		SetWorldSpawnCommand.register(this.dispatcher);
		SpectateCommand.register(this.dispatcher);
		SpreadPlayersCommand.register(this.dispatcher);
		StopSoundCommand.register(this.dispatcher);
		SummonCommand.register(this.dispatcher);
		TagCommand.register(this.dispatcher);
		TeamCommand.register(this.dispatcher);
		TeamMsgCommand.register(this.dispatcher);
		TeleportCommand.register(this.dispatcher);
		TellRawCommand.register(this.dispatcher);
		TimeCommand.register(this.dispatcher);
		TitleCommand.register(this.dispatcher);
		TriggerCommand.register(this.dispatcher);
		WeatherCommand.register(this.dispatcher);
		WorldBorderCommand.register(this.dispatcher);
		if (JvmProfiler.INSTANCE.isAvailable()) {
			JfrCommand.register(this.dispatcher);
		}

		if (SharedConstants.IS_RUNNING_IN_IDE) {
			TestCommand.register(this.dispatcher);
		}

		if (commandSelection.includeDedicated) {
			BanIpCommands.register(this.dispatcher);
			BanListCommands.register(this.dispatcher);
			BanPlayerCommands.register(this.dispatcher);
			DeOpCommands.register(this.dispatcher);
			OpCommand.register(this.dispatcher);
			PardonCommand.register(this.dispatcher);
			PardonIpCommand.register(this.dispatcher);
			PerfCommand.register(this.dispatcher);
			SaveAllCommand.register(this.dispatcher);
			SaveOffCommand.register(this.dispatcher);
			SaveOnCommand.register(this.dispatcher);
			SetPlayerIdleTimeoutCommand.register(this.dispatcher);
			StopCommand.register(this.dispatcher);
			WhitelistCommand.register(this.dispatcher);
		}

		if (commandSelection.includeIntegrated) {
			PublishCommand.register(this.dispatcher);
		}

		this.dispatcher.setConsumer((commandContext, bl, i) -> commandContext.getSource().onCommandComplete(commandContext, bl, i));
	}

	public int performPrefixedCommand(CommandSourceStack commandSourceStack, String string) {
		return this.performCommand(commandSourceStack, string.startsWith("/") ? string.substring(1) : string);
	}

	public int performCommand(CommandSourceStack commandSourceStack, String string) {
		StringReader stringReader = new StringReader(string);
		commandSourceStack.getServer().getProfiler().push((Supplier<String>)(() -> "/" + string));

		byte var20;
		try {
			return this.dispatcher.execute(stringReader, commandSourceStack);
		} catch (CommandRuntimeException var13) {
			commandSourceStack.sendFailure(var13.getComponent());
			return 0;
		} catch (CommandSyntaxException var14) {
			commandSourceStack.sendFailure(ComponentUtils.fromMessage(var14.getRawMessage()));
			if (var14.getInput() != null && var14.getCursor() >= 0) {
				int i = Math.min(var14.getInput().length(), var14.getCursor());
				MutableComponent mutableComponent = Component.empty()
					.withStyle(ChatFormatting.GRAY)
					.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + string)));
				if (i > 10) {
					mutableComponent.append("...");
				}

				mutableComponent.append(var14.getInput().substring(Math.max(0, i - 10), i));
				if (i < var14.getInput().length()) {
					Component component = Component.literal(var14.getInput().substring(i)).withStyle(ChatFormatting.RED, ChatFormatting.UNDERLINE);
					mutableComponent.append(component);
				}

				mutableComponent.append(Component.translatable("command.context.here").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
				commandSourceStack.sendFailure(mutableComponent);
			}

			return 0;
		} catch (Exception var15) {
			MutableComponent mutableComponent2 = Component.literal(var15.getMessage() == null ? var15.getClass().getName() : var15.getMessage());
			if (LOGGER.isDebugEnabled()) {
				LOGGER.error("Command exception: /{}", string, var15);
				StackTraceElement[] stackTraceElements = var15.getStackTrace();

				for (int j = 0; j < Math.min(stackTraceElements.length, 3); j++) {
					mutableComponent2.append("\n\n")
						.append(stackTraceElements[j].getMethodName())
						.append("\n ")
						.append(stackTraceElements[j].getFileName())
						.append(":")
						.append(String.valueOf(stackTraceElements[j].getLineNumber()));
				}
			}

			commandSourceStack.sendFailure(
				Component.translatable("command.failed").withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, mutableComponent2)))
			);
			if (SharedConstants.IS_RUNNING_IN_IDE) {
				commandSourceStack.sendFailure(Component.literal(Util.describeError(var15)));
				LOGGER.error("'/{}' threw an exception", string, var15);
			}

			var20 = 0;
		} finally {
			commandSourceStack.getServer().getProfiler().pop();
		}

		return var20;
	}

	public void sendCommands(ServerPlayer serverPlayer) {
		Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> map = Maps.<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>>newHashMap();
		RootCommandNode<SharedSuggestionProvider> rootCommandNode = new RootCommandNode<>();
		map.put(this.dispatcher.getRoot(), rootCommandNode);
		this.fillUsableCommands(this.dispatcher.getRoot(), rootCommandNode, serverPlayer.createCommandSourceStack(), map);
		serverPlayer.connection.send(new ClientboundCommandsPacket(rootCommandNode));
	}

	private void fillUsableCommands(
		CommandNode<CommandSourceStack> commandNode,
		CommandNode<SharedSuggestionProvider> commandNode2,
		CommandSourceStack commandSourceStack,
		Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> map
	) {
		for (CommandNode<CommandSourceStack> commandNode3 : commandNode.getChildren()) {
			if (commandNode3.canUse(commandSourceStack)) {
				ArgumentBuilder<SharedSuggestionProvider, ?> argumentBuilder = commandNode3.createBuilder();
				argumentBuilder.requires(sharedSuggestionProvider -> true);
				if (argumentBuilder.getCommand() != null) {
					argumentBuilder.executes(commandContext -> 0);
				}

				if (argumentBuilder instanceof RequiredArgumentBuilder) {
					RequiredArgumentBuilder<SharedSuggestionProvider, ?> requiredArgumentBuilder = (RequiredArgumentBuilder<SharedSuggestionProvider, ?>)argumentBuilder;
					if (requiredArgumentBuilder.getSuggestionsProvider() != null) {
						requiredArgumentBuilder.suggests(SuggestionProviders.safelySwap(requiredArgumentBuilder.getSuggestionsProvider()));
					}
				}

				if (argumentBuilder.getRedirect() != null) {
					argumentBuilder.redirect((CommandNode<SharedSuggestionProvider>)map.get(argumentBuilder.getRedirect()));
				}

				CommandNode<SharedSuggestionProvider> commandNode4 = argumentBuilder.build();
				map.put(commandNode3, commandNode4);
				commandNode2.addChild(commandNode4);
				if (!commandNode3.getChildren().isEmpty()) {
					this.fillUsableCommands(commandNode3, commandNode4, commandSourceStack, map);
				}
			}
		}
	}

	public static LiteralArgumentBuilder<CommandSourceStack> literal(String string) {
		return LiteralArgumentBuilder.literal(string);
	}

	public static <T> RequiredArgumentBuilder<CommandSourceStack, T> argument(String string, ArgumentType<T> argumentType) {
		return RequiredArgumentBuilder.argument(string, argumentType);
	}

	public static Predicate<String> createValidator(Commands.ParseFunction parseFunction) {
		return string -> {
			try {
				parseFunction.parse(new StringReader(string));
				return true;
			} catch (CommandSyntaxException var3) {
				return false;
			}
		};
	}

	public CommandDispatcher<CommandSourceStack> getDispatcher() {
		return this.dispatcher;
	}

	@Nullable
	public static <S> CommandSyntaxException getParseException(ParseResults<S> parseResults) {
		if (!parseResults.getReader().canRead()) {
			return null;
		} else if (parseResults.getExceptions().size() == 1) {
			return (CommandSyntaxException)parseResults.getExceptions().values().iterator().next();
		} else {
			return parseResults.getContext().getRange().isEmpty()
				? CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(parseResults.getReader())
				: CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(parseResults.getReader());
		}
	}

	public static void validate() {
		CommandBuildContext commandBuildContext = new CommandBuildContext((RegistryAccess)RegistryAccess.BUILTIN.get());
		commandBuildContext.missingTagAccessPolicy(CommandBuildContext.MissingTagAccessPolicy.RETURN_EMPTY);
		CommandDispatcher<CommandSourceStack> commandDispatcher = new Commands(Commands.CommandSelection.ALL, commandBuildContext).getDispatcher();
		RootCommandNode<CommandSourceStack> rootCommandNode = commandDispatcher.getRoot();
		commandDispatcher.findAmbiguities(
			(commandNode, commandNode2, commandNode3, collection) -> LOGGER.warn(
					"Ambiguity between arguments {} and {} with inputs: {}", commandDispatcher.getPath(commandNode2), commandDispatcher.getPath(commandNode3), collection
				)
		);
		Set<ArgumentType<?>> set = ArgumentUtils.findUsedArgumentTypes(rootCommandNode);
		Set<ArgumentType<?>> set2 = (Set<ArgumentType<?>>)set.stream()
			.filter(argumentType -> !ArgumentTypeInfos.isClassRecognized(argumentType.getClass()))
			.collect(Collectors.toSet());
		if (!set2.isEmpty()) {
			LOGGER.warn(
				"Missing type registration for following arguments:\n {}", set2.stream().map(argumentType -> "\t" + argumentType).collect(Collectors.joining(",\n"))
			);
			throw new IllegalStateException("Unregistered argument types");
		}
	}

	public static enum CommandSelection {
		ALL(true, true),
		DEDICATED(false, true),
		INTEGRATED(true, false);

		final boolean includeIntegrated;
		final boolean includeDedicated;

		private CommandSelection(boolean bl, boolean bl2) {
			this.includeIntegrated = bl;
			this.includeDedicated = bl2;
		}
	}

	@FunctionalInterface
	public interface ParseFunction {
		void parse(StringReader stringReader) throws CommandSyntaxException;
	}
}
