package net.minecraft.commands.execution;

@FunctionalInterface
public interface UnboundEntryAction<T> {
	void execute(T object, ExecutionContext<T> executionContext, Frame frame);

	default EntryAction<T> bind(T object) {
		return (executionContext, frame) -> this.execute(object, executionContext, frame);
	}
}
