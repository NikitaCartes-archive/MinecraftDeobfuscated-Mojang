package net.minecraft.commands.execution;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.commands.ExecutionCommandSource;

public interface CustomCommandExecutor<T> {
	void run(T object, ContextChain<T> contextChain, boolean bl, ExecutionControl<T> executionControl);

	public interface CommandAdapter<T> extends Command<T>, CustomCommandExecutor<T> {
		@Override
		default int run(CommandContext<T> commandContext) throws CommandSyntaxException {
			throw new UnsupportedOperationException("This function should not run");
		}
	}

	public abstract static class WithErrorHandling<T extends ExecutionCommandSource<T>> implements CustomCommandExecutor<T> {
		public final void run(T executionCommandSource, ContextChain<T> contextChain, boolean bl, ExecutionControl<T> executionControl) {
			try {
				this.runGuarded(executionCommandSource, contextChain, bl, executionControl);
			} catch (CommandSyntaxException var6) {
				this.onError(var6, executionCommandSource, bl, executionControl.tracer());
				executionCommandSource.storeResults(false, 0);
			}
		}

		protected void onError(CommandSyntaxException commandSyntaxException, T executionCommandSource, boolean bl, @Nullable TraceCallbacks traceCallbacks) {
			executionCommandSource.handleError(commandSyntaxException, bl, traceCallbacks);
		}

		protected abstract void runGuarded(T executionCommandSource, ContextChain<T> contextChain, boolean bl, ExecutionControl<T> executionControl) throws CommandSyntaxException;
	}
}
