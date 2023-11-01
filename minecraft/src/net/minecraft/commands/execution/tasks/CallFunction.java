package net.minecraft.commands.execution.tasks;

import java.util.List;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.functions.InstantiatedFunction;

public class CallFunction<T extends ExecutionCommandSource<T>> implements UnboundEntryAction<T> {
	private final InstantiatedFunction<T> function;
	private final CommandResultCallback resultCallback;
	private final boolean returnParentFrame;

	public CallFunction(InstantiatedFunction<T> instantiatedFunction, CommandResultCallback commandResultCallback, boolean bl) {
		this.function = instantiatedFunction;
		this.resultCallback = commandResultCallback;
		this.returnParentFrame = bl;
	}

	public void execute(T executionCommandSource, ExecutionContext<T> executionContext, Frame frame) {
		executionContext.incrementCost();
		List<UnboundEntryAction<T>> list = this.function.entries();
		TraceCallbacks traceCallbacks = executionContext.tracer();
		if (traceCallbacks != null) {
			traceCallbacks.onCall(frame.depth(), this.function.id(), this.function.entries().size());
		}

		int i = frame.depth() + 1;
		Frame.FrameControl frameControl = this.returnParentFrame ? frame.frameControl() : executionContext.frameControlForDepth(i);
		Frame frame2 = new Frame(i, this.resultCallback, frameControl);
		ContinuationTask.schedule(
			executionContext, frame2, list, (framex, unboundEntryAction) -> new CommandQueueEntry<>(framex, unboundEntryAction.bind(executionCommandSource))
		);
	}
}
