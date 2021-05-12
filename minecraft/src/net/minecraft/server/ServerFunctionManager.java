package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.GameRules;

public class ServerFunctionManager {
	private static final Component NO_RECURSIVE_TRACES = new TranslatableComponent("commands.debug.function.noRecursion");
	private static final ResourceLocation TICK_FUNCTION_TAG = new ResourceLocation("tick");
	private static final ResourceLocation LOAD_FUNCTION_TAG = new ResourceLocation("load");
	final MinecraftServer server;
	@Nullable
	private ServerFunctionManager.ExecutionContext context;
	private final List<CommandFunction> ticking = Lists.<CommandFunction>newArrayList();
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
			Collection<CommandFunction> collection = this.library.getTags().getTagOrEmpty(LOAD_FUNCTION_TAG).getValues();
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

	public int execute(CommandFunction commandFunction, CommandSourceStack commandSourceStack, @Nullable ServerFunctionManager.TraceCallbacks traceCallbacks) {
		if (this.context != null) {
			if (traceCallbacks != null) {
				this.context.reportError(NO_RECURSIVE_TRACES.getString());
				return 0;
			} else {
				this.context.delayFunctionCall(commandFunction, commandSourceStack);
				return 0;
			}
		} else {
			int var4;
			try {
				this.context = new ServerFunctionManager.ExecutionContext(traceCallbacks);
				var4 = this.context.runTopCommand(commandFunction, commandSourceStack);
			} finally {
				this.context = null;
			}

			return var4;
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

	class ExecutionContext {
		private int depth;
		@Nullable
		private final ServerFunctionManager.TraceCallbacks tracer;
		private final Deque<ServerFunctionManager.QueuedCommand> commandQueue = Queues.<ServerFunctionManager.QueuedCommand>newArrayDeque();
		private final List<ServerFunctionManager.QueuedCommand> nestedCalls = Lists.<ServerFunctionManager.QueuedCommand>newArrayList();

		ExecutionContext(@Nullable ServerFunctionManager.TraceCallbacks traceCallbacks) {
			this.tracer = traceCallbacks;
		}

		void delayFunctionCall(CommandFunction commandFunction, CommandSourceStack commandSourceStack) {
			int i = ServerFunctionManager.this.getCommandLimit();
			if (this.commandQueue.size() + this.nestedCalls.size() < i) {
				this.nestedCalls.add(new ServerFunctionManager.QueuedCommand(commandSourceStack, this.depth, new CommandFunction.FunctionEntry(commandFunction)));
			}
		}

		int runTopCommand(CommandFunction commandFunction, CommandSourceStack commandSourceStack) {
			int i = ServerFunctionManager.this.getCommandLimit();
			int j = 0;
			CommandFunction.Entry[] entrys = commandFunction.getEntries();

			for (int k = entrys.length - 1; k >= 0; k--) {
				this.commandQueue.push(new ServerFunctionManager.QueuedCommand(commandSourceStack, 0, entrys[k]));
			}

			while (!this.commandQueue.isEmpty()) {
				try {
					ServerFunctionManager.QueuedCommand queuedCommand = (ServerFunctionManager.QueuedCommand)this.commandQueue.removeFirst();
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

				if (++j >= i) {
					return j;
				}
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
		final int depth;
		private final CommandFunction.Entry entry;

		public QueuedCommand(CommandSourceStack commandSourceStack, int i, CommandFunction.Entry entry) {
			this.sender = commandSourceStack;
			this.depth = i;
			this.entry = entry;
		}

		public void execute(
			ServerFunctionManager serverFunctionManager,
			Deque<ServerFunctionManager.QueuedCommand> deque,
			int i,
			@Nullable ServerFunctionManager.TraceCallbacks traceCallbacks
		) {
			try {
				this.entry.execute(serverFunctionManager, this.sender, deque, i, this.depth, traceCallbacks);
			} catch (CommandSyntaxException var6) {
				if (traceCallbacks != null) {
					traceCallbacks.onError(this.depth, var6.getRawMessage().getString());
				}
			} catch (Exception var7) {
				if (traceCallbacks != null) {
					traceCallbacks.onError(this.depth, var7.getMessage());
				}
			}
		}

		public String toString() {
			return this.entry.toString();
		}
	}

	public interface TraceCallbacks {
		void onCommand(int i, String string);

		void onReturn(int i, String string, int j);

		void onError(int i, String string);

		void onCall(int i, ResourceLocation resourceLocation, int j);
	}
}
