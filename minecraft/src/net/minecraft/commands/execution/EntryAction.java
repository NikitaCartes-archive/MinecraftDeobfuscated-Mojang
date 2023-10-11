package net.minecraft.commands.execution;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

@FunctionalInterface
public interface EntryAction<T> {
	void execute(ExecutionContext<T> executionContext, int i) throws CommandSyntaxException;
}
