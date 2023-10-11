package net.minecraft.commands.execution;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

public record CommandQueueEntry<T>(int depth, EntryAction<T> action) {
	public void execute(ExecutionContext<T> executionContext) {
		TraceCallbacks traceCallbacks = executionContext.tracer();

		try {
			this.action.execute(executionContext, this.depth);
		} catch (CommandSyntaxException var4) {
			if (traceCallbacks != null) {
				traceCallbacks.onError(this.depth, var4.getRawMessage().getString());
			}
		} catch (Exception var5) {
			if (traceCallbacks != null) {
				traceCallbacks.onError(this.depth, var5.getMessage());
			}
		}
	}
}
