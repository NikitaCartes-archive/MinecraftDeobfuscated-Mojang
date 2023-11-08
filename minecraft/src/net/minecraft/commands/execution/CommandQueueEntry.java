package net.minecraft.commands.execution;

public record CommandQueueEntry<T>(Frame frame, EntryAction<T> action) {
	public void execute(ExecutionContext<T> executionContext) {
		this.action.execute(executionContext, this.frame);
	}
}
