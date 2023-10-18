package net.minecraft.commands.execution;

@FunctionalInterface
public interface UnboundEntryAction<T> {
	void execute(T object, ExecutionContext<T> executionContext, int i);

	default EntryAction<T> bind(T object) {
		return (executionContext, i) -> this.execute(object, executionContext, i);
	}
}
