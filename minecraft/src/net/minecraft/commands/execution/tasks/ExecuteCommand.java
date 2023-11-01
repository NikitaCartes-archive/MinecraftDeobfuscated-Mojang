package net.minecraft.commands.execution.tasks;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.function.Supplier;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.UnboundEntryAction;

public class ExecuteCommand<T extends ExecutionCommandSource<T>> implements UnboundEntryAction<T> {
	private final String commandInput;
	private final ChainModifiers modifiers;
	private final CommandContext<T> executionContext;

	public ExecuteCommand(String string, ChainModifiers chainModifiers, CommandContext<T> commandContext) {
		this.commandInput = string;
		this.modifiers = chainModifiers;
		this.executionContext = commandContext;
	}

	public void execute(T executionCommandSource, ExecutionContext<T> executionContext, Frame frame) {
		executionContext.profiler().push((Supplier<String>)(() -> "execute " + this.commandInput));

		try {
			executionContext.incrementCost();
			int i = ContextChain.runExecutable(this.executionContext, executionCommandSource, ExecutionCommandSource.resultConsumer(), this.modifiers.isForked());
			TraceCallbacks traceCallbacks = executionContext.tracer();
			if (traceCallbacks != null) {
				traceCallbacks.onReturn(frame.depth(), this.commandInput, i);
			}
		} catch (CommandSyntaxException var9) {
			executionCommandSource.handleError(var9, this.modifiers.isForked(), executionContext.tracer());
		} finally {
			executionContext.profiler().pop();
		}
	}
}
