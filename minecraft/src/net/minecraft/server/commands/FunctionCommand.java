package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.tasks.CallFunction;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import org.apache.commons.lang3.mutable.MutableInt;

public class FunctionCommand {
	private static final DynamicCommandExceptionType ERROR_ARGUMENT_NOT_COMPOUND = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("commands.function.error.argument_not_compound", object)
	);
	static final DynamicCommandExceptionType ERROR_NO_FUNCTIONS = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("commands.function.scheduled.no_functions", object)
	);
	private static final Dynamic2CommandExceptionType ERROR_FUNCTION_INSTANTATION_FAILURE = new Dynamic2CommandExceptionType(
		(object, object2) -> Component.translatableEscape("commands.function.instantiationFailure", object, object2)
	);
	public static final SuggestionProvider<CommandSourceStack> SUGGEST_FUNCTION = (commandContext, suggestionsBuilder) -> {
		ServerFunctionManager serverFunctionManager = commandContext.getSource().getServer().getFunctions();
		SharedSuggestionProvider.suggestResource(serverFunctionManager.getTagNames(), suggestionsBuilder, "#");
		return SharedSuggestionProvider.suggestResource(serverFunctionManager.getFunctionNames(), suggestionsBuilder);
	};
	static final FunctionCommand.Callbacks<CommandSourceStack> FULL_CONTEXT_CALLBACKS = new FunctionCommand.Callbacks<CommandSourceStack>() {
		public void signalResult(CommandSourceStack commandSourceStack, ResourceLocation resourceLocation, int i) {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.function.result", Component.translationArg(resourceLocation), i), true);
		}
	};

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder = Commands.literal("with");

		for (DataCommands.DataProvider dataProvider : DataCommands.SOURCE_PROVIDERS) {
			dataProvider.wrap(literalArgumentBuilder, argumentBuilder -> argumentBuilder.executes(new FunctionCommand.FunctionCustomExecutor() {
					@Override
					protected CompoundTag arguments(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
						return dataProvider.access(commandContext).getData();
					}
				}).then(Commands.argument("path", NbtPathArgument.nbtPath()).executes(new FunctionCommand.FunctionCustomExecutor() {
					@Override
					protected CompoundTag arguments(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
						return FunctionCommand.getArgumentTag(NbtPathArgument.getPath(commandContext, "path"), dataProvider.access(commandContext));
					}
				})));
		}

		commandDispatcher.register(
			Commands.literal("function")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(Commands.argument("name", FunctionArgument.functions()).suggests(SUGGEST_FUNCTION).executes(new FunctionCommand.FunctionCustomExecutor() {
					@Nullable
					@Override
					protected CompoundTag arguments(CommandContext<CommandSourceStack> commandContext) {
						return null;
					}
				}).then(Commands.argument("arguments", CompoundTagArgument.compoundTag()).executes(new FunctionCommand.FunctionCustomExecutor() {
					@Override
					protected CompoundTag arguments(CommandContext<CommandSourceStack> commandContext) {
						return CompoundTagArgument.getCompoundTag(commandContext, "arguments");
					}
				})).then(literalArgumentBuilder))
		);
	}

	static CompoundTag getArgumentTag(NbtPathArgument.NbtPath nbtPath, DataAccessor dataAccessor) throws CommandSyntaxException {
		Tag tag = DataCommands.getSingleTag(nbtPath, dataAccessor);
		if (tag instanceof CompoundTag) {
			return (CompoundTag)tag;
		} else {
			throw ERROR_ARGUMENT_NOT_COMPOUND.create(tag.getType().getName());
		}
	}

	public static CommandSourceStack modifySenderForExecution(CommandSourceStack commandSourceStack) {
		return commandSourceStack.withSuppressedOutput().withMaximumPermission(2);
	}

	public static <T extends ExecutionCommandSource<T>> void queueFunctions(
		Collection<CommandFunction<T>> collection,
		@Nullable CompoundTag compoundTag,
		T executionCommandSource,
		T executionCommandSource2,
		ExecutionControl<T> executionControl,
		FunctionCommand.Callbacks<T> callbacks
	) throws CommandSyntaxException {
		CommandDispatcher<T> commandDispatcher = executionCommandSource.dispatcher();
		MutableInt mutableInt = new MutableInt();

		for (CommandFunction<T> commandFunction : collection) {
			ResourceLocation resourceLocation = commandFunction.id();

			try {
				T executionCommandSource3 = executionCommandSource2.clearCallbacks().withReturnValueConsumer(i -> {
					int j = mutableInt.addAndGet(i);
					callbacks.signalResult(executionCommandSource, resourceLocation, j);
					executionCommandSource.storeResults(true, j);
				});
				InstantiatedFunction<T> instantiatedFunction = commandFunction.instantiate(compoundTag, commandDispatcher, executionCommandSource3);
				executionControl.queueNext(new CallFunction<>(instantiatedFunction).bind(executionCommandSource3));
			} catch (FunctionInstantiationException var13) {
				throw ERROR_FUNCTION_INSTANTATION_FAILURE.create(resourceLocation, var13.messageComponent());
			}
		}
	}

	public interface Callbacks<T> {
		void signalResult(T object, ResourceLocation resourceLocation, int i);
	}

	abstract static class FunctionCustomExecutor
		extends CustomCommandExecutor.WithErrorHandling<CommandSourceStack>
		implements CustomCommandExecutor.CommandAdapter<CommandSourceStack> {
		@Nullable
		protected abstract CompoundTag arguments(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException;

		public void runGuarded(
			CommandSourceStack commandSourceStack, ContextChain<CommandSourceStack> contextChain, boolean bl, ExecutionControl<CommandSourceStack> executionControl
		) throws CommandSyntaxException {
			CommandContext<CommandSourceStack> commandContext = contextChain.getTopContext().copyFor(commandSourceStack);
			Pair<ResourceLocation, Collection<CommandFunction<CommandSourceStack>>> pair = FunctionArgument.getFunctionOrTag(commandContext, "name")
				.mapSecond(either -> either.map(Collections::singleton, Function.identity()));
			Collection<CommandFunction<CommandSourceStack>> collection = pair.getSecond();
			if (collection.isEmpty()) {
				throw FunctionCommand.ERROR_NO_FUNCTIONS.create(Component.translationArg(pair.getFirst()));
			} else {
				CompoundTag compoundTag = this.arguments(commandContext);
				CommandSourceStack commandSourceStack2 = FunctionCommand.modifySenderForExecution(commandSourceStack);
				if (collection.size() == 1) {
					commandSourceStack.sendSuccess(
						() -> Component.translatable("commands.function.scheduled.single", Component.translationArg(((CommandFunction)collection.iterator().next()).id())), true
					);
				} else {
					commandSourceStack.sendSuccess(
						() -> Component.translatable(
								"commands.function.scheduled.multiple", ComponentUtils.formatList(collection.stream().map(CommandFunction::id).toList(), Component::translationArg)
							),
						true
					);
				}

				FunctionCommand.queueFunctions(collection, compoundTag, commandSourceStack, commandSourceStack2, executionControl, FunctionCommand.FULL_CONTEXT_CALLBACKS);
			}
		}

		protected void onError(CommandSyntaxException commandSyntaxException, CommandSourceStack commandSourceStack, boolean bl) {
			if (!bl) {
				commandSourceStack.sendFailure(ComponentUtils.fromMessage(commandSyntaxException.getRawMessage()));
			}
		}
	}
}
