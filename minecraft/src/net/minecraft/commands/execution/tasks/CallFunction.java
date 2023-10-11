package net.minecraft.commands.execution.tasks;

import java.util.List;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.functions.InstantiatedFunction;

public class CallFunction<T> implements UnboundEntryAction<T> {
	private final InstantiatedFunction<T> function;

	public CallFunction(InstantiatedFunction<T> instantiatedFunction) {
		this.function = instantiatedFunction;
	}

	@Override
	public void execute(T object, ExecutionContext<T> executionContext, int i) {
		executionContext.incrementCost();
		List<UnboundEntryAction<T>> list = this.function.entries();
		TraceCallbacks traceCallbacks = executionContext.tracer();
		if (traceCallbacks != null) {
			traceCallbacks.onCall(i, this.function.id(), this.function.entries().size());
		}

		ContinuationTask.schedule(executionContext, i + 1, list, (ix, unboundEntryAction) -> new CommandQueueEntry<>(ix, unboundEntryAction.bind(object)));
	}
}
