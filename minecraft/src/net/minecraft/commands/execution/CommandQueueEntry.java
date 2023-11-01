package net.minecraft.commands.execution;

public record CommandQueueEntry<T>(Frame frame, EntryAction<T> action) {
	public void execute(ExecutionContext<T> executionContext) {
		TraceCallbacks traceCallbacks = executionContext.tracer();

		try {
			this.action.execute(executionContext, this.frame);
		} catch (Exception var4) {
			if (traceCallbacks != null) {
				traceCallbacks.onError(var4.getMessage());
			}
		}
	}
}
