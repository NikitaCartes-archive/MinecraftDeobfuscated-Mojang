/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionLibrary;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.Nullable;

public class ServerFunctionManager {
    private static final Component NO_RECURSIVE_TRACES = new TranslatableComponent("commands.debug.function.noRecursion");
    private static final ResourceLocation TICK_FUNCTION_TAG = new ResourceLocation("tick");
    private static final ResourceLocation LOAD_FUNCTION_TAG = new ResourceLocation("load");
    private final MinecraftServer server;
    @Nullable
    private ExecutionContext context;
    private final List<CommandFunction> ticking = Lists.newArrayList();
    private boolean postReload;
    private ServerFunctionLibrary library;

    public ServerFunctionManager(MinecraftServer minecraftServer, ServerFunctionLibrary serverFunctionLibrary) {
        this.server = minecraftServer;
        this.library = serverFunctionLibrary;
        this.postReload(serverFunctionLibrary);
    }

    public int getCommandLimit() {
        return this.server.getGameRules().getInt(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH);
    }

    public CommandDispatcher<CommandSourceStack> getDispatcher() {
        return this.server.getCommands().getDispatcher();
    }

    public void tick() {
        this.executeTagFunctions(this.ticking, TICK_FUNCTION_TAG);
        if (this.postReload) {
            this.postReload = false;
            List<CommandFunction> collection = this.library.getTags().getTagOrEmpty(LOAD_FUNCTION_TAG).getValues();
            this.executeTagFunctions(collection, LOAD_FUNCTION_TAG);
        }
    }

    private void executeTagFunctions(Collection<CommandFunction> collection, ResourceLocation resourceLocation) {
        this.server.getProfiler().push(resourceLocation::toString);
        for (CommandFunction commandFunction : collection) {
            this.execute(commandFunction, this.getGameLoopSender());
        }
        this.server.getProfiler().pop();
    }

    public int execute(CommandFunction commandFunction, CommandSourceStack commandSourceStack) {
        return this.execute(commandFunction, commandSourceStack, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int execute(CommandFunction commandFunction, CommandSourceStack commandSourceStack, @Nullable TraceCallbacks traceCallbacks) {
        if (this.context != null) {
            if (traceCallbacks != null) {
                this.context.reportError(NO_RECURSIVE_TRACES.getString());
                return 0;
            }
            this.context.delayFunctionCall(commandFunction, commandSourceStack);
            return 0;
        }
        try {
            this.context = new ExecutionContext(traceCallbacks);
            int n = this.context.runTopCommand(commandFunction, commandSourceStack);
            return n;
        } finally {
            this.context = null;
        }
    }

    public void replaceLibrary(ServerFunctionLibrary serverFunctionLibrary) {
        this.library = serverFunctionLibrary;
        this.postReload(serverFunctionLibrary);
    }

    private void postReload(ServerFunctionLibrary serverFunctionLibrary) {
        this.ticking.clear();
        this.ticking.addAll(serverFunctionLibrary.getTags().getTagOrEmpty(TICK_FUNCTION_TAG).getValues());
        this.postReload = true;
    }

    public CommandSourceStack getGameLoopSender() {
        return this.server.createCommandSourceStack().withPermission(2).withSuppressedOutput();
    }

    public Optional<CommandFunction> get(ResourceLocation resourceLocation) {
        return this.library.getFunction(resourceLocation);
    }

    public Tag<CommandFunction> getTag(ResourceLocation resourceLocation) {
        return this.library.getTag(resourceLocation);
    }

    public Iterable<ResourceLocation> getFunctionNames() {
        return this.library.getFunctions().keySet();
    }

    public Iterable<ResourceLocation> getTagNames() {
        return this.library.getTags().getAvailableTags();
    }

    public static interface TraceCallbacks {
        public void onCommand(int var1, String var2);

        public void onReturn(int var1, String var2, int var3);

        public void onError(int var1, String var2);

        public void onCall(int var1, ResourceLocation var2, int var3);
    }

    class ExecutionContext {
        private int depth;
        @Nullable
        private final TraceCallbacks tracer;
        private final Deque<QueuedCommand> commandQueue = Queues.newArrayDeque();
        private final List<QueuedCommand> nestedCalls = Lists.newArrayList();

        private ExecutionContext(TraceCallbacks traceCallbacks) {
            this.tracer = traceCallbacks;
        }

        private void delayFunctionCall(CommandFunction commandFunction, CommandSourceStack commandSourceStack) {
            int i = ServerFunctionManager.this.getCommandLimit();
            if (this.commandQueue.size() + this.nestedCalls.size() < i) {
                this.nestedCalls.add(new QueuedCommand(commandSourceStack, this.depth, new CommandFunction.FunctionEntry(commandFunction)));
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private int runTopCommand(CommandFunction commandFunction, CommandSourceStack commandSourceStack) {
            int i = ServerFunctionManager.this.getCommandLimit();
            int j = 0;
            CommandFunction.Entry[] entrys = commandFunction.getEntries();
            for (int k = entrys.length - 1; k >= 0; --k) {
                this.commandQueue.push(new QueuedCommand(commandSourceStack, 0, entrys[k]));
            }
            while (!this.commandQueue.isEmpty()) {
                try {
                    QueuedCommand queuedCommand = this.commandQueue.removeFirst();
                    ServerFunctionManager.this.server.getProfiler().push(queuedCommand::toString);
                    this.depth = queuedCommand.depth;
                    queuedCommand.execute(ServerFunctionManager.this, this.commandQueue, i, this.tracer);
                    if (!this.nestedCalls.isEmpty()) {
                        Lists.reverse(this.nestedCalls).forEach(this.commandQueue::addFirst);
                        this.nestedCalls.clear();
                    }
                } finally {
                    ServerFunctionManager.this.server.getProfiler().pop();
                }
                if (++j < i) continue;
                return j;
            }
            return j;
        }

        public void reportError(String string) {
            if (this.tracer != null) {
                this.tracer.onError(this.depth, string);
            }
        }
    }

    public static class QueuedCommand {
        private final CommandSourceStack sender;
        private final int depth;
        private final CommandFunction.Entry entry;

        public QueuedCommand(CommandSourceStack commandSourceStack, int i, CommandFunction.Entry entry) {
            this.sender = commandSourceStack;
            this.depth = i;
            this.entry = entry;
        }

        public void execute(ServerFunctionManager serverFunctionManager, Deque<QueuedCommand> deque, int i, @Nullable TraceCallbacks traceCallbacks) {
            block4: {
                try {
                    this.entry.execute(serverFunctionManager, this.sender, deque, i, this.depth, traceCallbacks);
                } catch (CommandSyntaxException commandSyntaxException) {
                    if (traceCallbacks != null) {
                        traceCallbacks.onError(this.depth, commandSyntaxException.getRawMessage().getString());
                    }
                } catch (Exception exception) {
                    if (traceCallbacks == null) break block4;
                    traceCallbacks.onError(this.depth, exception.getMessage());
                }
            }
        }

        public String toString() {
            return this.entry.toString();
        }
    }
}

