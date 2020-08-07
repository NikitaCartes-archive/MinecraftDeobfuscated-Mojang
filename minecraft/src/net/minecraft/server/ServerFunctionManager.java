package net.minecraft.server;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.GameRules;

public class ServerFunctionManager {
	private static final ResourceLocation TICK_FUNCTION_TAG = new ResourceLocation("tick");
	private static final ResourceLocation LOAD_FUNCTION_TAG = new ResourceLocation("load");
	private final MinecraftServer server;
	private boolean isInFunction;
	private final ArrayDeque<ServerFunctionManager.QueuedCommand> commandQueue = new ArrayDeque();
	private final List<ServerFunctionManager.QueuedCommand> nestedCalls = Lists.<ServerFunctionManager.QueuedCommand>newArrayList();
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
