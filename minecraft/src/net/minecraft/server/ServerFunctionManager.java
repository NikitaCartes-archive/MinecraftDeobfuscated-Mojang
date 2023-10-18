package net.minecraft.server;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;

public class ServerFunctionManager {
	private static final ResourceLocation TICK_FUNCTION_TAG = new ResourceLocation("tick");
	private static final ResourceLocation LOAD_FUNCTION_TAG = new ResourceLocation("load");
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
		if (this.postReload) {
			this.postReload = false;
			Collection<CommandFunction<CommandSourceStack>> collection = this.library.getTag(LOAD_FUNCTION_TAG);
			this.executeTagFunctions(collection, LOAD_FUNCTION_TAG);
		}

		this.executeTagFunctions(this.ticking, TICK_FUNCTION_TAG);
	}

	private void executeTagFunctions(Collection<CommandFunction<CommandSourceStack>> collection, ResourceLocation resourceLocation) {
		this.server.getProfiler().push(resourceLocation::toString);

		for (CommandFunction<CommandSourceStack> commandFunction : collection) {
			this.execute(commandFunction, this.getGameLoopSender());
		}

		this.server.getProfiler().pop();
	}

	public void execute(CommandFunction<CommandSourceStack> commandFunction, CommandSourceStack commandSourceStack) {
		ProfilerFiller profilerFiller = this.server.getProfiler();
		profilerFiller.push((Supplier<String>)(() -> "function " + commandFunction.id()));

		try {
			InstantiatedFunction<CommandSourceStack> instantiatedFunction = commandFunction.instantiate(null, this.getDispatcher(), commandSourceStack);
			Commands.executeCommandInContext(commandSourceStack, executionContext -> executionContext.queueInitialFunctionCall(instantiatedFunction, commandSourceStack));
		} catch (FunctionInstantiationException var8) {
		} finally {
			profilerFiller.pop();
		}
	}

	public void replaceLibrary(ServerFunctionLibrary serverFunctionLibrary) {
		this.library = serverFunctionLibrary;
		this.postReload(serverFunctionLibrary);
	}

	private void postReload(ServerFunctionLibrary serverFunctionLibrary) {
		this.ticking = ImmutableList.copyOf(serverFunctionLibrary.getTag(TICK_FUNCTION_TAG));
		this.postReload = true;
	}

	public CommandSourceStack getGameLoopSender() {
		return this.server.createCommandSourceStack().withPermission(2).withSuppressedOutput();
	}

	public Optional<CommandFunction<CommandSourceStack>> get(ResourceLocation resourceLocation) {
		return this.library.getFunction(resourceLocation);
	}

	public Collection<CommandFunction<CommandSourceStack>> getTag(ResourceLocation resourceLocation) {
		return this.library.getTag(resourceLocation);
	}

	public Iterable<ResourceLocation> getFunctionNames() {
		return this.library.getFunctions().keySet();
	}

	public Iterable<ResourceLocation> getTagNames() {
		return this.library.getAvailableTags();
	}
}
