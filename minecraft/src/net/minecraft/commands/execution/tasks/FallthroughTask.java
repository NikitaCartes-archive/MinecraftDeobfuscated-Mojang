package net.minecraft.commands.execution.tasks;

import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.EntryAction;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;

public class FallthroughTask<T extends ExecutionCommandSource<T>> implements EntryAction<T> {
	private static final FallthroughTask<? extends ExecutionCommandSource<?>> INSTANCE = (FallthroughTask<? extends ExecutionCommandSource<?>>)(new FallthroughTask<>());

	public static <T extends ExecutionCommandSource<T>> EntryAction<T> instance() {
		return (EntryAction<T>)INSTANCE;
	}

	@Override
	public void execute(ExecutionContext<T> executionContext, Frame frame) {
		frame.returnFailure();
		frame.discard();
	}
}
