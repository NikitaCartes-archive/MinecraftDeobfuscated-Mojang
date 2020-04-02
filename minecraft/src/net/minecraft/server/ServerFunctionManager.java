package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.server.packs.resources.SimpleResource;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerFunctionManager implements ResourceManagerReloadListener {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ResourceLocation TICK_FUNCTION_TAG = new ResourceLocation("tick");
	private static final ResourceLocation LOAD_FUNCTION_TAG = new ResourceLocation("load");
	public static final int PATH_PREFIX_LENGTH = "functions/".length();
	public static final int PATH_SUFFIX_LENGTH = ".mcfunction".length();
	private final MinecraftServer server;
	private final Map<ResourceLocation, CommandFunction> functions = Maps.<ResourceLocation, CommandFunction>newHashMap();
	private boolean isInFunction;
	private final ArrayDeque<ServerFunctionManager.QueuedCommand> commandQueue = new ArrayDeque();
	private final List<ServerFunctionManager.QueuedCommand> nestedCalls = Lists.<ServerFunctionManager.QueuedCommand>newArrayList();
	private final TagCollection<CommandFunction> tags = new TagCollection<>(this::get, "tags/functions", "function");
	private final List<CommandFunction> ticking = Lists.<CommandFunction>newArrayList();
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

	public int execute(CommandFunction commandFunction, CommandSourceStack commandSourceStack) {
		int i = this.getCommandLimit();
		if (this.isInFunction) {
			if (this.commandQueue.size() + this.nestedCalls.size() < i) {
				this.nestedCalls.add(new ServerFunctionManager.QueuedCommand(this, commandSourceStack, new CommandFunction.FunctionEntry(commandFunction)));
			}

			return 0;
		} else {
			int var16;
			try {
				this.isInFunction = true;
				int j = 0;
				CommandFunction.Entry[] entrys = commandFunction.getEntries();

				for (int k = entrys.length - 1; k >= 0; k--) {
					this.commandQueue.push(new ServerFunctionManager.QueuedCommand(this, commandSourceStack, entrys[k]));
				}

				do {
					if (this.commandQueue.isEmpty()) {
						return j;
					}

					try {
						ServerFunctionManager.QueuedCommand queuedCommand = (ServerFunctionManager.QueuedCommand)this.commandQueue.removeFirst();
						this.server.getProfiler().push(queuedCommand::toString);
						queuedCommand.execute(this.commandQueue, i);
						if (!this.nestedCalls.isEmpty()) {
							Lists.reverse(this.nestedCalls).forEach(this.commandQueue::addFirst);
							this.nestedCalls.clear();
						}
					} finally {
						this.server.getProfiler().pop();
					}
				} while (++j < i);

				var16 = j;
			} finally {
				this.commandQueue.clear();
				this.nestedCalls.clear();
				this.isInFunction = false;
			}

			return var16;
		}
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		this.functions.clear();
		this.ticking.clear();
		Collection<ResourceLocation> collection = resourceManager.listResources("functions", stringx -> stringx.endsWith(".mcfunction"));
		List<CompletableFuture<CommandFunction>> list = Lists.<CompletableFuture<CommandFunction>>newArrayList();

		for (ResourceLocation resourceLocation : collection) {
			String string = resourceLocation.getPath();
			ResourceLocation resourceLocation2 = new ResourceLocation(
				resourceLocation.getNamespace(), string.substring(PATH_PREFIX_LENGTH, string.length() - PATH_SUFFIX_LENGTH)
			);
			list.add(
				CompletableFuture.supplyAsync(() -> readLinesAsync(resourceManager, resourceLocation), SimpleResource.IO_EXECUTOR)
					.thenApplyAsync(listx -> CommandFunction.fromLines(resourceLocation2, this, listx), this.server.getBackgroundTaskExecutor())
					.handle((commandFunction, throwable) -> this.addFunction(commandFunction, throwable, resourceLocation))
			);
		}

		CompletableFuture.allOf((CompletableFuture[])list.toArray(new CompletableFuture[0])).join();
		if (!this.functions.isEmpty()) {
			LOGGER.info("Loaded {} custom command functions", this.functions.size());
		}

		this.tags.load((Map<ResourceLocation, Tag.Builder>)this.tags.prepare(resourceManager, this.server.getBackgroundTaskExecutor()).join());
		this.ticking.addAll(this.tags.getTagOrEmpty(TICK_FUNCTION_TAG).getValues());
		this.postReload = true;
	}

	@Nullable
	private CommandFunction addFunction(CommandFunction commandFunction, @Nullable Throwable throwable, ResourceLocation resourceLocation) {
		if (throwable != null) {
			LOGGER.error("Couldn't load function at {}", resourceLocation, throwable);
			return null;
		} else {
			synchronized (this.functions) {
				this.functions.put(commandFunction.getId(), commandFunction);
				return commandFunction;
			}
		}
	}

	private static List<String> readLinesAsync(ResourceManager resourceManager, ResourceLocation resourceLocation) {
		try {
			Resource resource = resourceManager.getResource(resourceLocation);
			Throwable var3 = null;

			List var4;
			try {
				var4 = IOUtils.readLines(resource.getInputStream(), StandardCharsets.UTF_8);
			} catch (Throwable var14) {
				var3 = var14;
				throw var14;
			} finally {
				if (resource != null) {
					if (var3 != null) {
						try {
							resource.close();
						} catch (Throwable var13) {
							var3.addSuppressed(var13);
						}
					} else {
						resource.close();
					}
				}
			}

			return var4;
		} catch (IOException var16) {
			throw new CompletionException(var16);
		}
	}

	public CommandSourceStack getGameLoopSender() {
		return this.server.createCommandSourceStack().withPermission(2).withSuppressedOutput();
	}

	public CommandSourceStack getCompilationContext() {
		return new CommandSourceStack(
			CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, null, this.server.getFunctionCompilationLevel(), "", new TextComponent(""), this.server, null
		);
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

		public void execute(ArrayDeque<ServerFunctionManager.QueuedCommand> arrayDeque, int i) {
			try {
				this.entry.execute(this.manager, this.sender, arrayDeque, i);
			} catch (Throwable var4) {
			}
		}

		public String toString() {
			return this.entry.toString();
		}
	}
}
