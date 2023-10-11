package net.minecraft.commands.execution;

import javax.annotation.Nullable;

public interface ExecutionControl<T> {
	void queueNext(EntryAction<T> entryAction);

	void discardCurrentDepth();

	void tracer(@Nullable TraceCallbacks traceCallbacks);

	@Nullable
	TraceCallbacks tracer();
}
