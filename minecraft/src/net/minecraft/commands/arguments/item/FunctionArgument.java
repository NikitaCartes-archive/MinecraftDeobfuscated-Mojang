package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class FunctionArgument implements ArgumentType<FunctionArgument.Result> {
	private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "#foo");
	private static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("arguments.function.tag.unknown", object)
	);
	private static final DynamicCommandExceptionType ERROR_UNKNOWN_FUNCTION = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("arguments.function.unknown", object)
	);

	public static FunctionArgument functions() {
		return new FunctionArgument();
	}

	public FunctionArgument.Result parse(StringReader stringReader) throws CommandSyntaxException {
		if (stringReader.canRead() && stringReader.peek() == '#') {
			stringReader.skip();
			final ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
			return new FunctionArgument.Result() {
				@Override
				public Collection<CommandFunction<CommandSourceStack>> create(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
					return FunctionArgument.getFunctionTag(commandContext, resourceLocation);
				}

				@Override
				public Pair<ResourceLocation, Either<CommandFunction<CommandSourceStack>, Collection<CommandFunction<CommandSourceStack>>>> unwrap(
					CommandContext<CommandSourceStack> commandContext
				) throws CommandSyntaxException {
					return Pair.of(resourceLocation, Either.right(FunctionArgument.getFunctionTag(commandContext, resourceLocation)));
				}

				@Override
				public Pair<ResourceLocation, Collection<CommandFunction<CommandSourceStack>>> unwrapToCollection(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
					return Pair.of(resourceLocation, FunctionArgument.getFunctionTag(commandContext, resourceLocation));
				}
			};
		} else {
			final ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
			return new FunctionArgument.Result() {
				@Override
				public Collection<CommandFunction<CommandSourceStack>> create(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
					return Collections.singleton(FunctionArgument.getFunction(commandContext, resourceLocation));
				}

				@Override
				public Pair<ResourceLocation, Either<CommandFunction<CommandSourceStack>, Collection<CommandFunction<CommandSourceStack>>>> unwrap(
					CommandContext<CommandSourceStack> commandContext
				) throws CommandSyntaxException {
					return Pair.of(resourceLocation, Either.left(FunctionArgument.getFunction(commandContext, resourceLocation)));
				}

				@Override
				public Pair<ResourceLocation, Collection<CommandFunction<CommandSourceStack>>> unwrapToCollection(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
					return Pair.of(resourceLocation, Collections.singleton(FunctionArgument.getFunction(commandContext, resourceLocation)));
				}
			};
		}
	}

	static CommandFunction<CommandSourceStack> getFunction(CommandContext<CommandSourceStack> commandContext, ResourceLocation resourceLocation) throws CommandSyntaxException {
		return (CommandFunction<CommandSourceStack>)commandContext.getSource()
			.getServer()
			.getFunctions()
			.get(resourceLocation)
			.orElseThrow(() -> ERROR_UNKNOWN_FUNCTION.create(resourceLocation.toString()));
	}

	static Collection<CommandFunction<CommandSourceStack>> getFunctionTag(CommandContext<CommandSourceStack> commandContext, ResourceLocation resourceLocation) throws CommandSyntaxException {
		Collection<CommandFunction<CommandSourceStack>> collection = commandContext.getSource().getServer().getFunctions().getTag(resourceLocation);
		if (collection == null) {
			throw ERROR_UNKNOWN_TAG.create(resourceLocation.toString());
		} else {
			return collection;
		}
	}

	public static Collection<CommandFunction<CommandSourceStack>> getFunctions(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		return commandContext.<FunctionArgument.Result>getArgument(string, FunctionArgument.Result.class).create(commandContext);
	}

	public static Pair<ResourceLocation, Either<CommandFunction<CommandSourceStack>, Collection<CommandFunction<CommandSourceStack>>>> getFunctionOrTag(
		CommandContext<CommandSourceStack> commandContext, String string
	) throws CommandSyntaxException {
		return commandContext.<FunctionArgument.Result>getArgument(string, FunctionArgument.Result.class).unwrap(commandContext);
	}

	public static Pair<ResourceLocation, Collection<CommandFunction<CommandSourceStack>>> getFunctionCollection(
		CommandContext<CommandSourceStack> commandContext, String string
	) throws CommandSyntaxException {
		return commandContext.<FunctionArgument.Result>getArgument(string, FunctionArgument.Result.class).unwrapToCollection(commandContext);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	public interface Result {
		Collection<CommandFunction<CommandSourceStack>> create(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException;

		Pair<ResourceLocation, Either<CommandFunction<CommandSourceStack>, Collection<CommandFunction<CommandSourceStack>>>> unwrap(
			CommandContext<CommandSourceStack> commandContext
		) throws CommandSyntaxException;

		Pair<ResourceLocation, Collection<CommandFunction<CommandSourceStack>>> unwrapToCollection(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException;
	}
}
