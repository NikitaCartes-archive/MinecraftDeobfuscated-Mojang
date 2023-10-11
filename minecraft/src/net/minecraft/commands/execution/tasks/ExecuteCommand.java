package net.minecraft.commands.execution.tasks;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.function.Supplier;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.UnboundEntryAction;

public class ExecuteCommand<T extends ExecutionCommandSource<T>> implements UnboundEntryAction<T> {
	private final String commandInput;
	private final boolean forkedMode;
	private final CommandContext<T> executionContext;

	public ExecuteCommand(String string, boolean bl, CommandContext<T> commandContext) {
		this.commandInput = string;
		this.forkedMode = bl;
		this.executionContext = commandContext;
	}

	public void execute(T executionCommandSource, ExecutionContext<T> executionContext, int i) throws CommandSyntaxException {
		executionContext.profiler().push((Supplier<String>)(() -> "execute " + this.commandInput));

		try {
			executionContext.incrementCost();
			int j = ContextChain.runExecutable(this.executionContext, executionCommandSource, ExecutionCommandSource.resultConsumer(), this.forkedMode);
			TraceCallbacks traceCallbacks = executionContext.tracer();
			if (traceCallbacks != null) {
				traceCallbacks.onReturn(i, this.commandInput, j);
			}
		} finally {
			executionContext.profiler().pop();
		}
	}
}
