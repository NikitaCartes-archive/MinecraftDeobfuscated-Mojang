package net.minecraft.commands.functions;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.Commands;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.execution.tasks.BuildContexts;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public interface CommandFunction<T> {
	ResourceLocation id();

	InstantiatedFunction<T> instantiate(@Nullable CompoundTag compoundTag, CommandDispatcher<T> commandDispatcher) throws FunctionInstantiationException;

	private static boolean shouldConcatenateNextLine(CharSequence charSequence) {
		int i = charSequence.length();
		return i > 0 && charSequence.charAt(i - 1) == '\\';
	}

	static <T extends ExecutionCommandSource<T>> CommandFunction<T> fromLines(
		ResourceLocation resourceLocation, CommandDispatcher<T> commandDispatcher, T executionCommandSource, List<String> list
	) {
		FunctionBuilder<T> functionBuilder = new FunctionBuilder<>();

		for (int i = 0; i < list.size(); i++) {
			int j = i + 1;
			String string = ((String)list.get(i)).trim();
			String string3;
			if (shouldConcatenateNextLine(string)) {
				StringBuilder stringBuilder = new StringBuilder(string);

				do {
					if (++i == list.size()) {
						throw new IllegalArgumentException("Line continuation at end of file");
					}

					stringBuilder.deleteCharAt(stringBuilder.length() - 1);
					String string2 = ((String)list.get(i)).trim();
					stringBuilder.append(string2);
				} while (shouldConcatenateNextLine(stringBuilder));

				string3 = stringBuilder.toString();
			} else {
				string3 = string;
			}

			StringReader stringReader = new StringReader(string3);
			if (stringReader.canRead() && stringReader.peek() != '#') {
				if (stringReader.peek() == '/') {
					stringReader.skip();
					if (stringReader.peek() == '/') {
						throw new IllegalArgumentException("Unknown or invalid command '" + string3 + "' on line " + j + " (if you intended to make a comment, use '#' not '//')");
					}

					String string2 = stringReader.readUnquotedString();
					throw new IllegalArgumentException(
						"Unknown or invalid command '" + string3 + "' on line " + j + " (did you mean '" + string2 + "'? Do not use a preceding forwards slash.)"
					);
				}

				if (stringReader.peek() == '$') {
					functionBuilder.addMacro(string3.substring(1), j, executionCommandSource);
				} else {
					try {
						functionBuilder.addCommand(parseCommand(commandDispatcher, executionCommandSource, stringReader));
					} catch (CommandSyntaxException var11) {
						throw new IllegalArgumentException("Whilst parsing command on line " + j + ": " + var11.getMessage());
					}
				}
			}
		}

		return functionBuilder.build(resourceLocation);
	}

	static <T extends ExecutionCommandSource<T>> UnboundEntryAction<T> parseCommand(
		CommandDispatcher<T> commandDispatcher, T executionCommandSource, StringReader stringReader
	) throws CommandSyntaxException {
		ParseResults<T> parseResults = commandDispatcher.parse(stringReader, executionCommandSource);
		Commands.validateParseResults(parseResults);
		Optional<ContextChain<T>> optional = ContextChain.tryFlatten(parseResults.getContext().build(stringReader.getString()));
		if (optional.isEmpty()) {
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(parseResults.getReader());
		} else {
			return new BuildContexts.Unbound<>(stringReader.getString(), (ContextChain<T>)optional.get());
		}
	}
}
