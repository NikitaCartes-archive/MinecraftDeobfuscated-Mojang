package net.minecraft.commands;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionManager;

public class CommandFunction {
	private final CommandFunction.Entry[] entries;
	final ResourceLocation id;

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

	public CommandFunction instantiate(
		@Nullable CompoundTag compoundTag, CommandDispatcher<CommandSourceStack> commandDispatcher, CommandSourceStack commandSourceStack
	) throws FunctionInstantiationException {
		return this;
	}

	private static boolean shouldConcatenateNextLine(CharSequence charSequence) {
		int i = charSequence.length();
		return i > 0 && charSequence.charAt(i - 1) == '\\';
	}

	public static CommandFunction fromLines(
		ResourceLocation resourceLocation, CommandDispatcher<CommandSourceStack> commandDispatcher, CommandSourceStack commandSourceStack, List<String> list
	) {
		List<CommandFunction.Entry> list2 = new ArrayList(list.size());
		Set<String> set = new ObjectArraySet<>();

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
					CommandFunction.MacroEntry macroEntry = decomposeMacro(string3.substring(1), j);
					list2.add(macroEntry);
					set.addAll(macroEntry.parameters());
				} else {
					try {
						ParseResults<CommandSourceStack> parseResults = commandDispatcher.parse(stringReader, commandSourceStack);
						if (parseResults.getReader().canRead()) {
							throw Commands.getParseException(parseResults);
						}

						list2.add(new CommandFunction.CommandEntry(parseResults));
					} catch (CommandSyntaxException var12) {
						throw new IllegalArgumentException("Whilst parsing command on line " + j + ": " + var12.getMessage());
					}
				}
			}
		}

		return (CommandFunction)(set.isEmpty()
			? new CommandFunction(resourceLocation, (CommandFunction.Entry[])list2.toArray(CommandFunction.Entry[]::new))
			: new CommandFunction.CommandMacro(resourceLocation, (CommandFunction.Entry[])list2.toArray(CommandFunction.Entry[]::new), List.copyOf(set)));
	}

	@VisibleForTesting
	public static CommandFunction.MacroEntry decomposeMacro(String string, int i) {
		Builder<String> builder = ImmutableList.builder();
		Builder<String> builder2 = ImmutableList.builder();
		int j = string.length();
		int k = 0;
		int l = string.indexOf(36);

		while (l != -1) {
			if (l != j - 1 && string.charAt(l + 1) == '(') {
				builder.add(string.substring(k, l));
				int m = string.indexOf(41, l + 1);
				if (m == -1) {
					throw new IllegalArgumentException("Unterminated macro variable in macro '" + string + "' on line " + i);
				}

				String string2 = string.substring(l + 2, m);
				if (!isValidVariableName(string2)) {
					throw new IllegalArgumentException("Invalid macro variable name '" + string2 + "' on line " + i);
				}

				builder2.add(string2);
				k = m + 1;
				l = string.indexOf(36, k);
			} else {
				l = string.indexOf(36, l + 1);
			}
		}

		if (k == 0) {
			throw new IllegalArgumentException("Macro without variables on line " + i);
		} else {
			if (k != j) {
				builder.add(string.substring(k));
			}

			return new CommandFunction.MacroEntry(builder.build(), builder2.build());
		}
	}

	private static boolean isValidVariableName(String string) {
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if (!Character.isLetterOrDigit(c) && c != '_') {
				return false;
			}
		}

		return true;
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
			ServerFunctionManager serverFunctionManager,
			CommandSourceStack commandSourceStack,
			Deque<ServerFunctionManager.QueuedCommand> deque,
			int i,
			int j,
			@Nullable ServerFunctionManager.TraceCallbacks traceCallbacks
		) throws CommandSyntaxException {
			if (traceCallbacks != null) {
				String string = this.parse.getReader().getString();
				traceCallbacks.onCommand(j, string);
				int k = this.execute(serverFunctionManager, commandSourceStack);
				traceCallbacks.onReturn(j, string, k);
			} else {
				this.execute(serverFunctionManager, commandSourceStack);
			}
		}

		private int execute(ServerFunctionManager serverFunctionManager, CommandSourceStack commandSourceStack) throws CommandSyntaxException {
			return serverFunctionManager.getDispatcher().execute(Commands.mapSource(this.parse, commandSourceStack2 -> commandSourceStack));
		}

		public String toString() {
			return this.parse.getReader().getString();
		}
	}

	static class CommandMacro extends CommandFunction {
		private final List<String> parameters;
		private static final int MAX_CACHE_ENTRIES = 8;
		private final Object2ObjectLinkedOpenHashMap<List<String>, CommandFunction> cache = new Object2ObjectLinkedOpenHashMap<>(8, 0.25F);

		public CommandMacro(ResourceLocation resourceLocation, CommandFunction.Entry[] entrys, List<String> list) {
			super(resourceLocation, entrys);
			this.parameters = list;
		}

		@Override
		public CommandFunction instantiate(
			@Nullable CompoundTag compoundTag, CommandDispatcher<CommandSourceStack> commandDispatcher, CommandSourceStack commandSourceStack
		) throws FunctionInstantiationException {
			if (compoundTag == null) {
				throw new FunctionInstantiationException(Component.translatable("commands.function.error.missing_arguments", this.getId()));
			} else {
				List<String> list = new ArrayList(this.parameters.size());

				for (String string : this.parameters) {
					if (!compoundTag.contains(string)) {
						throw new FunctionInstantiationException(Component.translatable("commands.function.error.missing_argument", this.getId(), string));
					}

					list.add(stringify(compoundTag.get(string)));
				}

				CommandFunction commandFunction = this.cache.getAndMoveToLast(list);
				if (commandFunction != null) {
					return commandFunction;
				} else {
					if (this.cache.size() >= 8) {
						this.cache.removeFirst();
					}

					CommandFunction commandFunction2 = this.substituteAndParse(list, commandDispatcher, commandSourceStack);
					if (commandFunction2 != null) {
						this.cache.put(list, commandFunction2);
					}

					return commandFunction2;
				}
			}
		}

		private static String stringify(Tag tag) {
			if (tag instanceof FloatTag floatTag) {
				return String.valueOf(floatTag.getAsFloat());
			} else if (tag instanceof DoubleTag doubleTag) {
				return String.valueOf(doubleTag.getAsDouble());
			} else if (tag instanceof ByteTag byteTag) {
				return String.valueOf(byteTag.getAsByte());
			} else if (tag instanceof ShortTag shortTag) {
				return String.valueOf(shortTag.getAsShort());
			} else {
				return tag instanceof LongTag longTag ? String.valueOf(longTag.getAsLong()) : tag.getAsString();
			}
		}

		private CommandFunction substituteAndParse(List<String> list, CommandDispatcher<CommandSourceStack> commandDispatcher, CommandSourceStack commandSourceStack) throws FunctionInstantiationException {
			CommandFunction.Entry[] entrys = this.getEntries();
			CommandFunction.Entry[] entrys2 = new CommandFunction.Entry[entrys.length];

			for (int i = 0; i < entrys.length; i++) {
				CommandFunction.Entry entry = entrys[i];
				if (!(entry instanceof CommandFunction.MacroEntry macroEntry)) {
					entrys2[i] = entry;
				} else {
					List<String> list2 = macroEntry.parameters();
					List<String> list3 = new ArrayList(list2.size());

					for (String string : list2) {
						list3.add((String)list.get(this.parameters.indexOf(string)));
					}

					String string2 = macroEntry.substitute(list3);

					try {
						ParseResults<CommandSourceStack> parseResults = commandDispatcher.parse(string2, commandSourceStack);
						if (parseResults.getReader().canRead()) {
							throw Commands.getParseException(parseResults);
						}

						entrys2[i] = new CommandFunction.CommandEntry(parseResults);
					} catch (CommandSyntaxException var13) {
						throw new FunctionInstantiationException(Component.translatable("commands.function.error.parse", this.getId(), string2, var13.getMessage()));
					}
				}
			}

			ResourceLocation resourceLocation = this.getId();
			return new CommandFunction(new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath() + "/" + list.hashCode()), entrys2);
		}
	}

	@FunctionalInterface
	public interface Entry {
		void execute(
			ServerFunctionManager serverFunctionManager,
			CommandSourceStack commandSourceStack,
			Deque<ServerFunctionManager.QueuedCommand> deque,
			int i,
			int j,
			@Nullable ServerFunctionManager.TraceCallbacks traceCallbacks
		) throws CommandSyntaxException;
	}

	public static class FunctionEntry implements CommandFunction.Entry {
		private final CommandFunction.CacheableFunction function;

		public FunctionEntry(CommandFunction commandFunction) {
			this.function = new CommandFunction.CacheableFunction(commandFunction);
		}

		@Override
		public void execute(
			ServerFunctionManager serverFunctionManager,
			CommandSourceStack commandSourceStack,
			Deque<ServerFunctionManager.QueuedCommand> deque,
			int i,
			int j,
			@Nullable ServerFunctionManager.TraceCallbacks traceCallbacks
		) {
			Util.ifElse(this.function.get(serverFunctionManager), commandFunction -> {
				CommandFunction.Entry[] entrys = commandFunction.getEntries();
				if (traceCallbacks != null) {
					traceCallbacks.onCall(j, commandFunction.getId(), entrys.length);
				}

				int k = i - deque.size();
				int l = Math.min(entrys.length, k);

				for (int m = l - 1; m >= 0; m--) {
					deque.addFirst(new ServerFunctionManager.QueuedCommand(commandSourceStack, j + 1, entrys[m]));
				}
			}, () -> {
				if (traceCallbacks != null) {
					traceCallbacks.onCall(j, this.function.getId(), -1);
				}
			});
		}

		public String toString() {
			return "function " + this.function.getId();
		}
	}

	public static class MacroEntry implements CommandFunction.Entry {
		private final List<String> segments;
		private final List<String> parameters;

		public MacroEntry(List<String> list, List<String> list2) {
			this.segments = list;
			this.parameters = list2;
		}

		public List<String> parameters() {
			return this.parameters;
		}

		public String substitute(List<String> list) {
			StringBuilder stringBuilder = new StringBuilder();

			for (int i = 0; i < this.parameters.size(); i++) {
				stringBuilder.append((String)this.segments.get(i)).append((String)list.get(i));
			}

			if (this.segments.size() > this.parameters.size()) {
				stringBuilder.append((String)this.segments.get(this.segments.size() - 1));
			}

			return stringBuilder.toString();
		}

		@Override
		public void execute(
			ServerFunctionManager serverFunctionManager,
			CommandSourceStack commandSourceStack,
			Deque<ServerFunctionManager.QueuedCommand> deque,
			int i,
			int j,
			@Nullable ServerFunctionManager.TraceCallbacks traceCallbacks
		) throws CommandSyntaxException {
			throw new IllegalStateException("Tried to execute an uninstantiated macro");
		}
	}
}
