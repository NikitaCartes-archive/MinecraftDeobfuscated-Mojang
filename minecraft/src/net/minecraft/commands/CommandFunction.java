package net.minecraft.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionManager;

public class CommandFunction {
	private final CommandFunction.Entry[] entries;
	private final ResourceLocation id;

	public CommandFunction(ResourceLocation resourceLocation, CommandFunction.Entry[] entrys) {
		this.id = resourceLocation;
		this.entries = entrys;
	}

	public ResourceLocation getId() {
		return this.id;
	}

	public CommandFunction.Entry[] getEntries() {
		return this.entries;
	}

	public static CommandFunction fromLines(ResourceLocation resourceLocation, ServerFunctionManager serverFunctionManager, List<String> list) {
		List<CommandFunction.Entry> list2 = Lists.<CommandFunction.Entry>newArrayListWithCapacity(list.size());

		for (int i = 0; i < list.size(); i++) {
			int j = i + 1;
			String string = ((String)list.get(i)).trim();
			StringReader stringReader = new StringReader(string);
			if (stringReader.canRead() && stringReader.peek() != '#') {
				if (stringReader.peek() == '/') {
					stringReader.skip();
					if (stringReader.peek() == '/') {
						throw new IllegalArgumentException("Unknown or invalid command '" + string + "' on line " + j + " (if you intended to make a comment, use '#' not '//')");
					}

					String string2 = stringReader.readUnquotedString();
					throw new IllegalArgumentException(
						"Unknown or invalid command '" + string + "' on line " + j + " (did you mean '" + string2 + "'? Do not use a preceding forwards slash.)"
					);
				}

				try {
					ParseResults<CommandSourceStack> parseResults = serverFunctionManager.getServer()
						.getCommands()
						.getDispatcher()
						.parse(stringReader, serverFunctionManager.getCompilationContext());
					if (parseResults.getReader().canRead()) {
						throw Commands.getParseException(parseResults);
					}

					list2.add(new CommandFunction.CommandEntry(parseResults));
				} catch (CommandSyntaxException var9) {
					throw new IllegalArgumentException("Whilst parsing command on line " + j + ": " + var9.getMessage());
				}
			}
		}

		return new CommandFunction(resourceLocation, (CommandFunction.Entry[])list2.toArray(new CommandFunction.Entry[0]));
	}

	public static class CacheableFunction {
		public static final CommandFunction.CacheableFunction NONE = new CommandFunction.CacheableFunction((ResourceLocation)null);
		@Nullable
		private final ResourceLocation id;
		private boolean resolved;
		private Optional<CommandFunction> function = Optional.empty();

		public CacheableFunction(@Nullable ResourceLocation resourceLocation) {
			this.id = resourceLocation;
		}

		public CacheableFunction(CommandFunction commandFunction) {
			this.resolved = true;
			this.id = null;
			this.function = Optional.of(commandFunction);
		}

		public Optional<CommandFunction> get(ServerFunctionManager serverFunctionManager) {
			if (!this.resolved) {
				if (this.id != null) {
					this.function = serverFunctionManager.get(this.id);
				}

				this.resolved = true;
			}

			return this.function;
		}

		@Nullable
		public ResourceLocation getId() {
			return (ResourceLocation)this.function.map(commandFunction -> commandFunction.id).orElse(this.id);
		}
	}

	public static class CommandEntry implements CommandFunction.Entry {
		private final ParseResults<CommandSourceStack> parse;

		public CommandEntry(ParseResults<CommandSourceStack> parseResults) {
			this.parse = parseResults;
		}

		@Override
		public void execute(
			ServerFunctionManager serverFunctionManager, CommandSourceStack commandSourceStack, ArrayDeque<ServerFunctionManager.QueuedCommand> arrayDeque, int i
		) throws CommandSyntaxException {
			serverFunctionManager.getDispatcher()
				.execute(new ParseResults<>(this.parse.getContext().withSource(commandSourceStack), this.parse.getReader(), this.parse.getExceptions()));
		}

		public String toString() {
			return this.parse.getReader().getString();
		}
	}

	public interface Entry {
		void execute(
			ServerFunctionManager serverFunctionManager, CommandSourceStack commandSourceStack, ArrayDeque<ServerFunctionManager.QueuedCommand> arrayDeque, int i
		) throws CommandSyntaxException;
	}

	public static class FunctionEntry implements CommandFunction.Entry {
		private final CommandFunction.CacheableFunction function;

		public FunctionEntry(CommandFunction commandFunction) {
			this.function = new CommandFunction.CacheableFunction(commandFunction);
		}

		@Override
		public void execute(
			ServerFunctionManager serverFunctionManager, CommandSourceStack commandSourceStack, ArrayDeque<ServerFunctionManager.QueuedCommand> arrayDeque, int i
		) {
			this.function.get(serverFunctionManager).ifPresent(commandFunction -> {
				CommandFunction.Entry[] entrys = commandFunction.getEntries();
				int j = i - arrayDeque.size();
				int k = Math.min(entrys.length, j);

				for (int l = k - 1; l >= 0; l--) {
					arrayDeque.addFirst(new ServerFunctionManager.QueuedCommand(serverFunctionManager, commandSourceStack, entrys[l]));
				}
			});
		}

		public String toString() {
			return "function " + this.function.getId();
		}
	}
}
