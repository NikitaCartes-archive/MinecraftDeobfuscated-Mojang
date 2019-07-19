/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.server.packs.resources.SimpleResource;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ServerFunctionManager
implements ResourceManagerReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation TICK_FUNCTION_TAG = new ResourceLocation("tick");
    private static final ResourceLocation LOAD_FUNCTION_TAG = new ResourceLocation("load");
    public static final int PATH_PREFIX_LENGTH = "functions/".length();
    public static final int PATH_SUFFIX_LENGTH = ".mcfunction".length();
    private final MinecraftServer server;
    private final Map<ResourceLocation, CommandFunction> functions = Maps.newHashMap();
    private boolean isInFunction;
    private final ArrayDeque<QueuedCommand> commandQueue = new ArrayDeque();
    private final List<QueuedCommand> nestedCalls = Lists.newArrayList();
    private final TagCollection<CommandFunction> tags = new TagCollection(this::get, "tags/functions", true, "function");
    private final List<CommandFunction> ticking = Lists.newArrayList();
    private boolean postReload;

    public ServerFunctionManager(MinecraftServer minecraftServer) {
        this.server = minecraftServer;
    }

    public Optional<CommandFunction> get(ResourceLocation resourceLocation) {
        return Optional.ofNullable(this.functions.get(resourceLocation));
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public int getCommandLimit() {
        return this.server.getGameRules().getInt(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH);
    }

    public Map<ResourceLocation, CommandFunction> getFunctions() {
        return this.functions;
    }

    public CommandDispatcher<CommandSourceStack> getDispatcher() {
        return this.server.getCommands().getDispatcher();
    }

    public void tick() {
        this.server.getProfiler().push(TICK_FUNCTION_TAG::toString);
        for (CommandFunction commandFunction : this.ticking) {
            this.execute(commandFunction, this.getGameLoopSender());
        }
        this.server.getProfiler().pop();
        if (this.postReload) {
            this.postReload = false;
            Collection<CommandFunction> collection = this.getTags().getTagOrEmpty(LOAD_FUNCTION_TAG).getValues();
            this.server.getProfiler().push(LOAD_FUNCTION_TAG::toString);
            for (CommandFunction commandFunction2 : collection) {
                this.execute(commandFunction2, this.getGameLoopSender());
            }
            this.server.getProfiler().pop();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int execute(CommandFunction commandFunction, CommandSourceStack commandSourceStack) {
        int i = this.getCommandLimit();
        if (this.isInFunction) {
            if (this.commandQueue.size() + this.nestedCalls.size() < i) {
                this.nestedCalls.add(new QueuedCommand(this, commandSourceStack, new CommandFunction.FunctionEntry(commandFunction)));
            }
            return 0;
        }
        try {
            this.isInFunction = true;
            int j = 0;
            CommandFunction.Entry[] entrys = commandFunction.getEntries();
            for (int k = entrys.length - 1; k >= 0; --k) {
                this.commandQueue.push(new QueuedCommand(this, commandSourceStack, entrys[k]));
            }
            while (!this.commandQueue.isEmpty()) {
                try {
                    QueuedCommand queuedCommand = this.commandQueue.removeFirst();
                    this.server.getProfiler().push(queuedCommand::toString);
                    queuedCommand.execute(this.commandQueue, i);
                    if (!this.nestedCalls.isEmpty()) {
                        Lists.reverse(this.nestedCalls).forEach(this.commandQueue::addFirst);
                        this.nestedCalls.clear();
                    }
                } finally {
                    this.server.getProfiler().pop();
                }
                if (++j < i) continue;
                int n = j;
                return n;
            }
            int n = j;
            return n;
        } finally {
            this.commandQueue.clear();
            this.nestedCalls.clear();
            this.isInFunction = false;
        }
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.functions.clear();
        this.ticking.clear();
        Collection<ResourceLocation> collection = resourceManager.listResources("functions", string -> string.endsWith(".mcfunction"));
        ArrayList<CompletionStage> list2 = Lists.newArrayList();
        for (ResourceLocation resourceLocation : collection) {
            String string2 = resourceLocation.getPath();
            ResourceLocation resourceLocation2 = new ResourceLocation(resourceLocation.getNamespace(), string2.substring(PATH_PREFIX_LENGTH, string2.length() - PATH_SUFFIX_LENGTH));
            list2.add(((CompletableFuture)CompletableFuture.supplyAsync(() -> ServerFunctionManager.readLinesAsync(resourceManager, resourceLocation), SimpleResource.IO_EXECUTOR).thenApplyAsync(list -> CommandFunction.fromLines(resourceLocation2, this, list), this.server.getBackgroundTaskExecutor())).handle((commandFunction, throwable) -> this.addFunction((CommandFunction)commandFunction, (Throwable)throwable, resourceLocation)));
        }
        CompletableFuture.allOf(list2.toArray(new CompletableFuture[0])).join();
        if (!this.functions.isEmpty()) {
            LOGGER.info("Loaded {} custom command functions", (Object)this.functions.size());
        }
        this.tags.load(this.tags.prepare(resourceManager, this.server.getBackgroundTaskExecutor()).join());
        this.ticking.addAll(this.tags.getTagOrEmpty(TICK_FUNCTION_TAG).getValues());
        this.postReload = true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Nullable
    private CommandFunction addFunction(CommandFunction commandFunction, @Nullable Throwable throwable, ResourceLocation resourceLocation) {
        if (throwable != null) {
            LOGGER.error("Couldn't load function at {}", (Object)resourceLocation, (Object)throwable);
            return null;
        }
        Map<ResourceLocation, CommandFunction> map = this.functions;
        synchronized (map) {
            this.functions.put(commandFunction.getId(), commandFunction);
        }
        return commandFunction;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static List<String> readLinesAsync(ResourceManager resourceManager, ResourceLocation resourceLocation) {
        try (Resource resource = resourceManager.getResource(resourceLocation);){
            List<String> list = IOUtils.readLines(resource.getInputStream(), StandardCharsets.UTF_8);
            return list;
        } catch (IOException iOException) {
            throw new CompletionException(iOException);
        }
    }

    public CommandSourceStack getGameLoopSender() {
        return this.server.createCommandSourceStack().withPermission(2).withSuppressedOutput();
    }

    public CommandSourceStack getCompilationContext() {
        return new CommandSourceStack(CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, null, this.server.getFunctionCompilationLevel(), "", new TextComponent(""), this.server, null);
    }

    public TagCollection<CommandFunction> getTags() {
        return this.tags;
    }

    public static class QueuedCommand {
        private final ServerFunctionManager manager;
        private final CommandSourceStack sender;
        private final CommandFunction.Entry entry;

        public QueuedCommand(ServerFunctionManager serverFunctionManager, CommandSourceStack commandSourceStack, CommandFunction.Entry entry) {
            this.manager = serverFunctionManager;
            this.sender = commandSourceStack;
            this.entry = entry;
        }

        public void execute(ArrayDeque<QueuedCommand> arrayDeque, int i) {
            try {
                this.entry.execute(this.manager, this.sender, arrayDeque, i);
            } catch (Throwable throwable) {
                // empty catch block
            }
        }

        public String toString() {
            return this.entry.toString();
        }
    }
}

