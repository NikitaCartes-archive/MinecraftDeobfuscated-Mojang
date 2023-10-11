package net.minecraft.commands.execution;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

@FunctionalInterface
public interface UnboundEntryAction<T> {
	void execute(T object, ExecutionContext<T> executionContext, int i) throws CommandSyntaxException;

	default EntryAction<T> bind(T object) {
		return (executionContext, i) -> this.execute(object, executionContext, i);
	}
}
