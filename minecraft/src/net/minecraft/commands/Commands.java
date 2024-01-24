package net.minecraft.commands;

import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.logging.LogUtils;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.gametest.framework.TestCommand;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.AdvancementCommands;
import net.minecraft.server.commands.AttributeCommand;
import net.minecraft.server.commands.BanIpCommands;
import net.minecraft.server.commands.BanListCommands;
import net.minecraft.server.commands.BanPlayerCommands;
import net.minecraft.server.commands.BossBarCommands;
import net.minecraft.server.commands.ClearInventoryCommands;
import net.minecraft.server.commands.CloneCommands;
import net.minecraft.server.commands.DamageCommand;
import net.minecraft.server.commands.DataPackCommand;
import net.minecraft.server.commands.DeOpCommands;
import net.minecraft.server.commands.DebugCommand;
import net.minecraft.server.commands.DebugConfigCommand;
import net.minecraft.server.commands.DebugMobSpawningCommand;
import net.minecraft.server.commands.DebugPathCommand;
import net.minecraft.server.commands.DefaultGameModeCommands;
import net.minecraft.server.commands.DifficultyCommand;
import net.minecraft.server.commands.EffectCommands;
import net.minecraft.server.commands.EmoteCommands;
import net.minecraft.server.commands.EnchantCommand;
import net.minecraft.server.commands.ExecuteCommand;
import net.minecraft.server.commands.ExperienceCommand;
import net.minecraft.server.commands.FillBiomeCommand;
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
import net.minecraft.server.commands.RaidCommand;
import net.minecraft.server.commands.RandomCommand;
import net.minecraft.server.commands.RecipeCommand;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.commands.ResetChunksCommand;
import net.minecraft.server.commands.ReturnCommand;
import net.minecraft.server.commands.RideCommand;
import net.minecraft.server.commands.SaveAllCommand;
import net.minecraft.server.commands.SaveOffCommand;
import net.minecraft.server.commands.SaveOnCommand;
import net.minecraft.server.commands.SayCommand;
import net.minecraft.server.commands.ScheduleCommand;
import net.minecraft.server.commands.ScoreboardCommand;
import net.minecraft.server.commands.SeedCommand;
import net.minecraft.server.commands.ServerPackCommand;
import net.minecraft.server.commands.SetBlockCommand;
import net.minecraft.server.commands.SetPlayerIdleTimeoutCommand;
import net.minecraft.server.commands.SetSpawnCommand;
import net.minecraft.server.commands.SetWorldSpawnCommand;
import net.minecraft.server.commands.SpawnArmorTrimsCommand;
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
import net.minecraft.server.commands.TickCommand;
import net.minecraft.server.commands.TimeCommand;
import net.minecraft.server.commands.TitleCommand;
import net.minecraft.server.commands.TransferCommand;
import net.minecraft.server.commands.TriggerCommand;
import net.minecraft.server.commands.WardenSpawnTrackerCommand;
import net.minecraft.server.commands.WeatherCommand;
import net.minecraft.server.commands.WhitelistCommand;
import net.minecraft.server.commands.WorldBorderCommand;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.world.level.GameRules;
import org.slf4j.Logger;

public class Commands {
	private static final ThreadLocal<ExecutionContext<CommandSourceStack>> CURRENT_EXECUTION_CONTEXT = new ThreadLocal();
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final int LEVEL_ALL = 0;
	public static final int LEVEL_MODERATORS = 1;
	public static final int LEVEL_GAMEMASTERS = 2;
	public static final int LEVEL_ADMINS = 3;
	public static final int LEVEL_OWNERS = 4;
	private final CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();

	public Commands(Commands.CommandSelection commandSelection, CommandBuildContext commandBuildContext) {
		AdvancementCommands.register(this.dispatcher);
		AttributeCommand.register(this.dispatcher, commandBuildContext);
		ExecuteCommand.register(this.dispatcher, commandBuildContext);
		BossBarCommands.register(this.dispatcher);
		ClearInventoryCommands.register(this.dispatcher, commandBuildContext);
		CloneCommands.register(this.dispatcher, commandBuildContext);
		DamageCommand.register(this.dispatcher, commandBuildContext);
		DataCommands.register(this.dispatcher);
		DataPackCommand.register(this.dispatcher);
		DebugCommand.register(this.dispatcher);
		DefaultGameModeCommands.register(this.dispatcher);
		DifficultyCommand.register(this.dispatcher);
		EffectCommands.register(this.dispatcher, commandBuildContext);
		EmoteCommands.register(this.dispatcher);
		EnchantCommand.register(this.dispatcher, commandBuildContext);
		ExperienceCommand.register(this.dispatcher);
		FillCommand.register(this.dispatcher, commandBuildContext);
		FillBiomeCommand.register(this.dispatcher, commandBuildContext);
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
		LocateCommand.register(this.dispatcher, commandBuildContext);
		LootCommand.register(this.dispatcher, commandBuildContext);
		MsgCommand.register(this.dispatcher);
		ParticleCommand.register(this.dispatcher, commandBuildContext);
		PlaceCommand.register(this.dispatcher);
		PlaySoundCommand.register(this.dispatcher);
		RandomCommand.register(this.dispatcher);
		ReloadCommand.register(this.dispatcher);
		RecipeCommand.register(this.dispatcher);
		ReturnCommand.register(this.dispatcher);
		RideCommand.register(this.dispatcher);
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
		SummonCommand.register(this.dispatcher, commandBuildContext);
		TagCommand.register(this.dispatcher);
		TeamCommand.register(this.dispatcher);
		TeamMsgCommand.register(this.dispatcher);
		TeleportCommand.register(this.dispatcher);
		TellRawCommand.register(this.dispatcher);
		TickCommand.register(this.dispatcher);
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
			ResetChunksCommand.register(this.dispatcher);
			RaidCommand.register(this.dispatcher);
			DebugPathCommand.register(this.dispatcher);
			DebugMobSpawningCommand.register(this.dispatcher);
			WardenSpawnTrackerCommand.register(this.dispatcher);
			SpawnArmorTrimsCommand.register(this.dispatcher);
			ServerPackCommand.register(this.dispatcher);
			if (commandSelection.includeDedicated) {
				DebugConfigCommand.register(this.dispatcher);
			}
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
			TransferCommand.register(this.dispatcher);
			WhitelistCommand.register(this.dispatcher);
		}

		if (commandSelection.includeIntegrated) {
			PublishCommand.register(this.dispatcher);
		}

		this.dispatcher.setConsumer(ExecutionCommandSource.resultConsumer());
	}

	public static <S> ParseResults<S> mapSource(ParseResults<S> parseResults, UnaryOperator<S> unaryOperator) {
		CommandContextBuilder<S> commandContextBuilder = parseResults.getContext();
		CommandContextBuilder<S> commandContextBuilder2 = commandContextBuilder.withSource((S)unaryOperator.apply(commandContextBuilder.getSource()));
		return new ParseResults<>(commandContextBuilder2, parseResults.getReader(), parseResults.getExceptions());
	}

	public void performPrefixedCommand(CommandSourceStack commandSourceStack, String string) {
		string = string.startsWith("/") ? string.substring(1) : string;
		this.performCommand(this.dispatcher.parse(string, commandSourceStack), string);
	}

	public void performCommand(ParseResults<CommandSourceStack> parseResults, String string) {
		CommandSourceStack commandSourceStack = parseResults.getContext().getSource();
		commandSourceStack.getServer().getProfiler().push((Supplier<String>)(() -> "/" + string));
		ContextChain<CommandSourceStack> contextChain = finishParsing(parseResults, string, commandSourceStack);

		try {
			if (contextChain != null) {
				executeCommandInContext(
					commandSourceStack,
					executionContext -> ExecutionContext.queueInitialCommandExecution(executionContext, string, contextChain, commandSourceStack, CommandResultCallback.EMPTY)
				);
			}
		} catch (Exception var12) {
			MutableComponent mutableComponent = Component.literal(var12.getMessage() == null ? var12.getClass().getName() : var12.getMessage());
			if (LOGGER.isDebugEnabled()) {
				LOGGER.error("Command exception: /{}", string, var12);
				StackTraceElement[] stackTraceElements = var12.getStackTrace();

				for (int i = 0; i < Math.min(stackTraceElements.length, 3); i++) {
					mutableComponent.append("\n\n")
						.append(stackTraceElements[i].getMethodName())
						.append("\n ")
						.append(stackTraceElements[i].getFileName())
						.append(":")
						.append(String.valueOf(stackTraceElements[i].getLineNumber()));
				}
			}

			commandSourceStack.sendFailure(
				Component.translatable("command.failed").withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, mutableComponent)))
			);
			if (SharedConstants.IS_RUNNING_IN_IDE) {
				commandSourceStack.sendFailure(Component.literal(Util.describeError(var12)));
				LOGGER.error("'/{}' threw an exception", string, var12);
			}
		} finally {
			commandSourceStack.getServer().getProfiler().pop();
		}
	}

	@Nullable
	private static ContextChain<CommandSourceStack> finishParsing(
		ParseResults<CommandSourceStack> parseResults, String string, CommandSourceStack commandSourceStack
	) {
		try {
			validateParseResults(parseResults);
			return (ContextChain<CommandSourceStack>)ContextChain.tryFlatten(parseResults.getContext().build(string))
				.orElseThrow(() -> CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(parseResults.getReader()));
		} catch (CommandSyntaxException var7) {
			commandSourceStack.sendFailure(ComponentUtils.fromMessage(var7.getRawMessage()));
			if (var7.getInput() != null && var7.getCursor() >= 0) {
				int i = Math.min(var7.getInput().length(), var7.getCursor());
				MutableComponent mutableComponent = Component.empty()
					.withStyle(ChatFormatting.GRAY)
					.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + string)));
				if (i > 10) {
					mutableComponent.append(CommonComponents.ELLIPSIS);
				}

				mutableComponent.append(var7.getInput().substring(Math.max(0, i - 10), i));
				if (i < var7.getInput().length()) {
					Component component = Component.literal(var7.getInput().substring(i)).withStyle(ChatFormatting.RED, ChatFormatting.UNDERLINE);
					mutableComponent.append(component);
				}

				mutableComponent.append(Component.translatable("command.context.here").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
				commandSourceStack.sendFailure(mutableComponent);
			}

			return null;
		}
	}

	public static void executeCommandInContext(CommandSourceStack commandSourceStack, Consumer<ExecutionContext<CommandSourceStack>> consumer) {
		MinecraftServer minecraftServer = commandSourceStack.getServer();
		ExecutionContext<CommandSourceStack> executionContext = (ExecutionContext<CommandSourceStack>)CURRENT_EXECUTION_CONTEXT.get();
		boolean bl = executionContext == null;
		if (bl) {
			int i = Math.max(1, minecraftServer.getGameRules().getInt(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH));
			int j = minecraftServer.getGameRules().getInt(GameRules.RULE_MAX_COMMAND_FORK_COUNT);

			try (ExecutionContext<CommandSourceStack> executionContext2 = new ExecutionContext<>(i, j, minecraftServer.getProfiler())) {
				CURRENT_EXECUTION_CONTEXT.set(executionContext2);
				consumer.accept(executionContext2);
				executionContext2.runCommandQueue();
			} finally {
				CURRENT_EXECUTION_CONTEXT.set(null);
			}
		} else {
			consumer.accept(executionContext);
		}
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

	public static <S> void validateParseResults(ParseResults<S> parseResults) throws CommandSyntaxException {
		CommandSyntaxException commandSyntaxException = getParseException(parseResults);
		if (commandSyntaxException != null) {
			throw commandSyntaxException;
		}
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

	public static CommandBuildContext createValidationContext(HolderLookup.Provider provider) {
		return new CommandBuildContext() {
			@Override
			public <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<T>> resourceKey) {
				final HolderLookup.RegistryLookup<T> registryLookup = provider.lookupOrThrow(resourceKey);
				return new HolderLookup.Delegate<T>(registryLookup) {
					@Override
					public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
						return Optional.of(this.getOrThrow(tagKey));
					}

					@Override
					public HolderSet.Named<T> getOrThrow(TagKey<T> tagKey) {
						Optional<HolderSet.Named<T>> optional = registryLookup.get(tagKey);
						return (HolderSet.Named<T>)optional.orElseGet(() -> HolderSet.emptyNamed(registryLookup, tagKey));
					}
				};
			}
		};
	}

	public static void validate() {
		CommandBuildContext commandBuildContext = createValidationContext(VanillaRegistries.createLookup());
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
