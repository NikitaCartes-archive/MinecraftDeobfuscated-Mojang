package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import org.apache.commons.lang3.mutable.MutableObject;

public class FunctionCommand {
	private static final DynamicCommandExceptionType ERROR_ARGUMENT_NOT_COMPOUND = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("commands.function.error.argument_not_compound", object)
	);
	public static final SuggestionProvider<CommandSourceStack> SUGGEST_FUNCTION = (commandContext, suggestionsBuilder) -> {
		ServerFunctionManager serverFunctionManager = commandContext.getSource().getServer().getFunctions();
		SharedSuggestionProvider.suggestResource(serverFunctionManager.getTagNames(), suggestionsBuilder, "#");
		return SharedSuggestionProvider.suggestResource(serverFunctionManager.getFunctionNames(), suggestionsBuilder);
	};

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder = Commands.literal("with");

		for (DataCommands.DataProvider dataProvider : DataCommands.SOURCE_PROVIDERS) {
			dataProvider.wrap(
				literalArgumentBuilder,
				argumentBuilder -> argumentBuilder.executes(
							commandContext -> runFunction(
									(CommandSourceStack)commandContext.getSource(), FunctionArgument.getFunctions(commandContext, "name"), dataProvider.access(commandContext).getData()
								)
						)
						.then(
							Commands.argument("path", NbtPathArgument.nbtPath())
								.executes(
									commandContext -> runFunction(
											commandContext.getSource(),
											FunctionArgument.getFunctions(commandContext, "name"),
											getArgumentTag(NbtPathArgument.getPath(commandContext, "path"), dataProvider.access(commandContext))
										)
								)
						)
			);
		}

		commandDispatcher.register(
			Commands.literal("function")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("name", FunctionArgument.functions())
						.suggests(SUGGEST_FUNCTION)
						.executes(commandContext -> runFunction(commandContext.getSource(), FunctionArgument.getFunctions(commandContext, "name"), null))
						.then(
							Commands.argument("arguments", CompoundTagArgument.compoundTag())
								.executes(
									commandContext -> runFunction(
											commandContext.getSource(), FunctionArgument.getFunctions(commandContext, "name"), CompoundTagArgument.getCompoundTag(commandContext, "arguments")
										)
								)
						)
						.then(literalArgumentBuilder)
				)
		);
	}

	private static CompoundTag getArgumentTag(NbtPathArgument.NbtPath nbtPath, DataAccessor dataAccessor) throws CommandSyntaxException {
		Tag tag = DataCommands.getSingleTag(nbtPath, dataAccessor);
		if (tag instanceof CompoundTag) {
			return (CompoundTag)tag;
		} else {
			throw ERROR_ARGUMENT_NOT_COMPOUND.create(tag.getType().getName());
		}
	}

	private static int runFunction(CommandSourceStack commandSourceStack, Collection<CommandFunction> collection, @Nullable CompoundTag compoundTag) {
		int i = 0;
		boolean bl = false;
		boolean bl2 = false;

		for (CommandFunction commandFunction : collection) {
			try {
				FunctionCommand.FunctionResult functionResult = runFunction(commandSourceStack, commandFunction, compoundTag);
				i += functionResult.value();
				bl |= functionResult.isReturn();
				bl2 = true;
			} catch (FunctionInstantiationException var9) {
				commandSourceStack.sendFailure(var9.messageComponent());
			}
		}

		if (bl2) {
			int j = i;
			if (collection.size() == 1) {
				if (bl) {
					commandSourceStack.sendSuccess(
						() -> Component.translatable(
								"commands.function.success.single.result", j, Component.translationArg(((CommandFunction)collection.iterator().next()).getId())
							),
						true
					);
				} else {
					commandSourceStack.sendSuccess(
						() -> Component.translatable("commands.function.success.single", j, Component.translationArg(((CommandFunction)collection.iterator().next()).getId())),
						true
					);
				}
			} else if (bl) {
				commandSourceStack.sendSuccess(() -> Component.translatable("commands.function.success.multiple.result", collection.size()), true);
			} else {
				commandSourceStack.sendSuccess(() -> Component.translatable("commands.function.success.multiple", j, collection.size()), true);
			}
		}

		return i;
	}

	public static FunctionCommand.FunctionResult runFunction(
		CommandSourceStack commandSourceStack, CommandFunction commandFunction, @Nullable CompoundTag compoundTag
	) throws FunctionInstantiationException {
		MutableObject<FunctionCommand.FunctionResult> mutableObject = new MutableObject<>();
		int i = commandSourceStack.getServer()
			.getFunctions()
			.execute(
				commandFunction,
				commandSourceStack.withSuppressedOutput()
					.withMaximumPermission(2)
					.withReturnValueConsumer(ix -> mutableObject.setValue(new FunctionCommand.FunctionResult(ix, true))),
				null,
				compoundTag
			);
		FunctionCommand.FunctionResult functionResult = mutableObject.getValue();
		return functionResult != null ? functionResult : new FunctionCommand.FunctionResult(i, false);
	}

	public static record FunctionResult(int value, boolean isReturn) {
	}
}
