package net.minecraft.commands.execution;

import com.google.common.collect.Queues;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Deque;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.tasks.BuildContexts;
import net.minecraft.commands.execution.tasks.CallFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public class ExecutionContext<T> implements AutoCloseable {
	private static final int MAX_QUEUE_DEPTH = 10000000;
	private static final Logger LOGGER = LogUtils.getLogger();
	private final int commandLimit;
	private final int forkLimit;
	private final ProfilerFiller profiler;
	@Nullable
	private TraceCallbacks tracer;
	private int commandQuota;
	private boolean queueOverflow;
	private final Deque<CommandQueueEntry<T>> commandQueue = Queues.<CommandQueueEntry<T>>newArrayDeque();
	private final List<CommandQueueEntry<T>> newTopCommands = new ObjectArrayList<>();
	private int currentFrameDepth;

	public ExecutionContext(int i, int j, ProfilerFiller profilerFiller) {
		this.commandLimit = i;
		this.forkLimit = j;
		this.profiler = profilerFiller;
		this.commandQuota = i;
	}

	private static <T extends ExecutionCommandSource<T>> Frame createTopFrame(ExecutionContext<T> executionContext, CommandResultCallback commandResultCallback) {
		if (executionContext.currentFrameDepth == 0) {
			return new Frame(0, commandResultCallback, executionContext.commandQueue::clear);
		} else {
			int i = executionContext.currentFrameDepth + 1;
			return new Frame(i, commandResultCallback, executionContext.frameControlForDepth(i));
		}
	}

	public static <T extends ExecutionCommandSource<T>> void queueInitialFunctionCall(
		ExecutionContext<T> executionContext, InstantiatedFunction<T> instantiatedFunction, T executionCommandSource, CommandResultCallback commandResultCallback
	) {
		executionContext.queueNext(
			new CommandQueueEntry<>(
				createTopFrame(executionContext, commandResultCallback),
				new CallFunction<>(instantiatedFunction, executionCommandSource.callback(), false).bind(executionCommandSource)
			)
		);
	}

	public static <T extends ExecutionCommandSource<T>> void queueInitialCommandExecution(
		ExecutionContext<T> executionContext, String string, ContextChain<T> contextChain, T executionCommandSource, CommandResultCallback commandResultCallback
	) {
		executionContext.queueNext(
			new CommandQueueEntry<>(createTopFrame(executionContext, commandResultCallback), new BuildContexts.TopLevel<>(string, contextChain, executionCommandSource))
		);
	}

	private void handleQueueOverflow() {
		this.queueOverflow = true;
		this.newTopCommands.clear();
		this.commandQueue.clear();
	}

	public void queueNext(CommandQueueEntry<T> commandQueueEntry) {
		if (this.newTopCommands.size() + this.commandQueue.size() > 10000000) {
			this.handleQueueOverflow();
		}

		if (!this.queueOverflow) {
			this.newTopCommands.add(commandQueueEntry);
		}
	}

	public void discardAtDepthOrHigher(int i) {
		while (!this.commandQueue.isEmpty() && ((CommandQueueEntry)this.commandQueue.peek()).frame().depth() >= i) {
			this.commandQueue.removeFirst();
		}
	}

	public Frame.FrameControl frameControlForDepth(int i) {
		return () -> this.discardAtDepthOrHigher(i);
	}

	public void runCommandQueue() {
		this.pushNewCommands();

		while (true) {
			if (this.commandQuota <= 0) {
				LOGGER.info("Command execution stopped due to limit (executed {} commands)", this.commandLimit);
				break;
			}

			CommandQueueEntry<T> commandQueueEntry = (CommandQueueEntry<T>)this.commandQueue.pollFirst();
			if (commandQueueEntry == null) {
				return;
			}

			this.currentFrameDepth = commandQueueEntry.frame().depth();
			commandQueueEntry.execute(this);
			if (this.queueOverflow) {
				LOGGER.error("Command execution stopped due to command queue overflow (max {})", 10000000);
				break;
			}

			this.pushNewCommands();
		}

		this.currentFrameDepth = 0;
	}

	private void pushNewCommands() {
		for (int i = this.newTopCommands.size() - 1; i >= 0; i--) {
			this.commandQueue.addFirst((CommandQueueEntry)this.newTopCommands.get(i));
		}

		this.newTopCommands.clear();
	}

	public void tracer(@Nullable TraceCallbacks traceCallbacks) {
		this.tracer = traceCallbacks;
	}

	@Nullable
	public TraceCallbacks tracer() {
		return this.tracer;
	}

	public ProfilerFiller profiler() {
		return this.profiler;
	}

	public int forkLimit() {
		return this.forkLimit;
	}

	public void incrementCost() {
		this.commandQuota--;
	}

	public void close() {
		if (this.tracer != null) {
			this.tracer.close();
		}
	}
}
