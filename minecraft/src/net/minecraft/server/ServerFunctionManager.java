package net.minecraft.server;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public class ServerFunctionManager {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final ResourceLocation TICK_FUNCTION_TAG = ResourceLocation.withDefaultNamespace("tick");
	private static final ResourceLocation LOAD_FUNCTION_TAG = ResourceLocation.withDefaultNamespace("load");
	private final MinecraftServer server;
	private List<CommandFunction<CommandSourceStack>> ticking = ImmutableList.of();
	private boolean postReload;
	private ServerFunctionLibrary library;

	public ServerFunctionManager(MinecraftServer minecraftServer, ServerFunctionLibrary serverFunctionLibrary) {
		this.server = minecraftServer;
		this.library = serverFunctionLibrary;
		this.postReload(serverFunctionLibrary);
	}

	public CommandDispatcher<CommandSourceStack> getDispatcher() {
		return this.server.getCommands().getDispatcher();
	}

	public void tick() {
		if (this.server.tickRateManager().runsNormally()) {
			if (this.postReload) {
				this.postReload = false;
				Collection<CommandFunction<CommandSourceStack>> collection = this.library.getTag(LOAD_FUNCTION_TAG);
				this.executeTagFunctions(collection, LOAD_FUNCTION_TAG);
			}

			this.executeTagFunctions(this.ticking, TICK_FUNCTION_TAG);
		}
	}

	private void executeTagFunctions(Collection<CommandFunction<CommandSourceStack>> collection, ResourceLocation resourceLocation) {
		Profiler.get().push(resourceLocation::toString);

		for (CommandFunction<CommandSourceStack> commandFunction : collection) {
			this.execute(commandFunction, this.getGameLoopSender());
		}

		Profiler.get().pop();
	}

	public void execute(CommandFunction<CommandSourceStack> commandFunction, CommandSourceStack commandSourceStack) {
		ProfilerFiller profilerFiller = Profiler.get();
		profilerFiller.push((Supplier<String>)(() -> "function " + commandFunction.id()));

		try {
			InstantiatedFunction<CommandSourceStack> instantiatedFunction = commandFunction.instantiate(null, this.getDispatcher());
			Commands.executeCommandInContext(
				commandSourceStack,
				executionContext -> ExecutionContext.queueInitialFunctionCall(executionContext, instantiatedFunction, commandSourceStack, CommandResultCallback.EMPTY)
			);
		} catch (FunctionInstantiationException var9) {
		} catch (Exception var10) {
			LOGGER.warn("Failed to execute function {}", commandFunction.id(), var10);
		} finally {
			profilerFiller.pop();
		}
	}

	public void replaceLibrary(ServerFunctionLibrary serverFunctionLibrary) {
		this.library = serverFunctionLibrary;
		this.postReload(serverFunctionLibrary);
	}

	private void postReload(ServerFunctionLibrary serverFunctionLibrary) {
		this.ticking = List.copyOf(serverFunctionLibrary.getTag(TICK_FUNCTION_TAG));
		this.postReload = true;
	}

	public CommandSourceStack getGameLoopSender() {
		return this.server.createCommandSourceStack().withPermission(2).withSuppressedOutput();
	}

	public Optional<CommandFunction<CommandSourceStack>> get(ResourceLocation resourceLocation) {
		return this.library.getFunction(resourceLocation);
	}

	public List<CommandFunction<CommandSourceStack>> getTag(ResourceLocation resourceLocation) {
		return this.library.getTag(resourceLocation);
	}

	public Iterable<ResourceLocation> getFunctionNames() {
		return this.library.getFunctions().keySet();
	}

	public Iterable<ResourceLocation> getTagNames() {
		return this.library.getAvailableTags();
	}
}
