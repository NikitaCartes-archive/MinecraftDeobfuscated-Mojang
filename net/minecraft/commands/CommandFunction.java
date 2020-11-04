/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionManager;
import org.jetbrains.annotations.Nullable;

public class CommandFunction {
    private final Entry[] entries;
    private final ResourceLocation id;

    public CommandFunction(ResourceLocation resourceLocation, Entry[] entrys) {
        this.id = resourceLocation;
        this.entries = entrys;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public Entry[] getEntries() {
        return this.entries;
    }

    public static CommandFunction fromLines(ResourceLocation resourceLocation, CommandDispatcher<CommandSourceStack> commandDispatcher, CommandSourceStack commandSourceStack, List<String> list) {
        ArrayList<CommandEntry> list2 = Lists.newArrayListWithCapacity(list.size());
        for (int i = 0; i < list.size(); ++i) {
            int j = i + 1;
            String string = list.get(i).trim();
            StringReader stringReader = new StringReader(string);
            if (!stringReader.canRead() || stringReader.peek() == '#') continue;
            if (stringReader.peek() == '/') {
                stringReader.skip();
                if (stringReader.peek() == '/') {
                    throw new IllegalArgumentException("Unknown or invalid command '" + string + "' on line " + j + " (if you intended to make a comment, use '#' not '//')");
                }
                String string2 = stringReader.readUnquotedString();
                throw new IllegalArgumentException("Unknown or invalid command '" + string + "' on line " + j + " (did you mean '" + string2 + "'? Do not use a preceding forwards slash.)");
            }
            try {
                ParseResults<CommandSourceStack> parseResults = commandDispatcher.parse(stringReader, commandSourceStack);
                if (parseResults.getReader().canRead()) {
                    throw Commands.getParseException(parseResults);
                }
                list2.add(new CommandEntry(parseResults));
                continue;
            } catch (CommandSyntaxException commandSyntaxException) {
                throw new IllegalArgumentException("Whilst parsing command on line " + j + ": " + commandSyntaxException.getMessage());
            }
        }
        return new CommandFunction(resourceLocation, list2.toArray(new Entry[0]));
    }

    public static class CacheableFunction {
        public static final CacheableFunction NONE = new CacheableFunction((ResourceLocation)null);
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
            return this.function.map(commandFunction -> ((CommandFunction)commandFunction).id).orElse(this.id);
        }
    }

    public static class FunctionEntry
    implements Entry {
        private final CacheableFunction function;

        public FunctionEntry(CommandFunction commandFunction) {
            this.function = new CacheableFunction(commandFunction);
        }

        @Override
        public void execute(ServerFunctionManager serverFunctionManager, CommandSourceStack commandSourceStack, Deque<ServerFunctionManager.QueuedCommand> deque, int i) {
            this.function.get(serverFunctionManager).ifPresent(commandFunction -> {
                Entry[] entrys = commandFunction.getEntries();
                int j = i - deque.size();
                int k = Math.min(entrys.length, j);
                for (int l = k - 1; l >= 0; --l) {
                    deque.addFirst(new ServerFunctionManager.QueuedCommand(serverFunctionManager, commandSourceStack, entrys[l]));
                }
            });
        }

        public String toString() {
            return "function " + this.function.getId();
        }
    }

    public static class CommandEntry
    implements Entry {
        private final ParseResults<CommandSourceStack> parse;

        public CommandEntry(ParseResults<CommandSourceStack> parseResults) {
            this.parse = parseResults;
        }

        @Override
        public void execute(ServerFunctionManager serverFunctionManager, CommandSourceStack commandSourceStack, Deque<ServerFunctionManager.QueuedCommand> deque, int i) throws CommandSyntaxException {
            serverFunctionManager.getDispatcher().execute(new ParseResults<CommandSourceStack>(this.parse.getContext().withSource(commandSourceStack), this.parse.getReader(), this.parse.getExceptions()));
        }

        public String toString() {
            return this.parse.getReader().getString();
        }
    }

    public static interface Entry {
        public void execute(ServerFunctionManager var1, CommandSourceStack var2, Deque<ServerFunctionManager.QueuedCommand> var3, int var4) throws CommandSyntaxException;
    }
}

