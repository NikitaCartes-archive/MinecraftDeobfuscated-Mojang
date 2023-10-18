package net.minecraft.commands.execution.tasks;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.context.ContextChain.Stage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.CustomModifierExecutor;
import net.minecraft.commands.execution.EntryAction;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.network.chat.Component;

public class BuildContexts<T extends ExecutionCommandSource<T>> {
	@VisibleForTesting
	public static final DynamicCommandExceptionType ERROR_FORK_LIMIT_REACHED = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("command.forkLimit", object)
	);
	private final String commandInput;
	private final ContextChain<T> command;

	public BuildContexts(String string, ContextChain<T> contextChain) {
		this.commandInput = string;
		this.command = contextChain;
	}

	protected void execute(List<T> list, ExecutionContext<T> executionContext, int i, boolean bl) {
		ContextChain<T> contextChain = this.command;
		boolean bl2 = bl;
		List<T> list2 = list;
		if (contextChain.getStage() != Stage.EXECUTE) {
			executionContext.profiler().push((Supplier<String>)(() -> "prepare " + this.commandInput));

			try {
				for (int j = executionContext.forkLimit(); contextChain.getStage() != Stage.EXECUTE; contextChain = contextChain.nextStage()) {
					CommandContext<T> commandContext = contextChain.getTopContext();
					bl2 |= commandContext.isForked();
					RedirectModifier<T> redirectModifier = commandContext.getRedirectModifier();
					if (redirectModifier instanceof CustomModifierExecutor<T> customModifierExecutor) {
						customModifierExecutor.apply(list2, contextChain, bl2, createExecutionControl(executionContext, i));
						return;
					}

					if (redirectModifier != null) {
						executionContext.incrementCost();
						List<T> list3 = new ArrayList();

						for (T executionCommandSource : list2) {
							try {
								for (T executionCommandSource2 : ContextChain.runModifier(commandContext, executionCommandSource, ExecutionCommandSource.resultConsumer(), bl2)) {
									list3.add(executionCommandSource2);
									if (list3.size() >= j) {
										executionCommandSource2.handleError(ERROR_FORK_LIMIT_REACHED.create(j), bl2, executionContext.tracer());
										return;
									}
								}
							} catch (CommandSyntaxException var20) {
								executionCommandSource.handleError(var20, bl2, executionContext.tracer());
								if (!bl2) {
									return;
								}
							}
						}

						list2 = list3;
					}
				}
			} finally {
				executionContext.profiler().pop();
			}
		}

		CommandContext<T> commandContext2 = contextChain.getTopContext();
		if (commandContext2.getCommand() instanceof CustomCommandExecutor<T> customCommandExecutor) {
			ExecutionControl<T> executionControl = createExecutionControl(executionContext, i);

			for (T executionCommandSource : list2) {
				customCommandExecutor.run(executionCommandSource, contextChain, bl2, executionControl);
			}
		} else {
			ExecuteCommand<T> executeCommand = new ExecuteCommand<>(this.commandInput, bl2, commandContext2);
			ContinuationTask.schedule(
				executionContext, i, list2, (ix, executionCommandSourcex) -> new CommandQueueEntry<>(ix, executeCommand.bind((T)executionCommandSourcex))
			);
		}
	}

	private static <T extends ExecutionCommandSource<T>> ExecutionControl<T> createExecutionControl(ExecutionContext<T> executionContext, int i) {
		return new ExecutionControl<T>() {
			@Override
			public void queueNext(EntryAction<T> entryAction) {
				executionContext.queueNext(new CommandQueueEntry<>(i, entryAction));
			}

			@Override
			public void discardCurrentDepth() {
				executionContext.discardAtDepthOrHigher(i);
			}

			@Override
			public void tracer(@Nullable TraceCallbacks traceCallbacks) {
				executionContext.tracer(traceCallbacks);
			}

			@Nullable
			@Override
			public TraceCallbacks tracer() {
				return executionContext.tracer();
			}
		};
	}

	protected void traceCommandStart(ExecutionContext<T> executionContext, int i) {
		TraceCallbacks traceCallbacks = executionContext.tracer();
		if (traceCallbacks != null) {
			traceCallbacks.onCommand(i, this.commandInput);
		}
	}

	public String toString() {
		return this.commandInput;
	}

	public static class Continuation<T extends ExecutionCommandSource<T>> extends BuildContexts<T> implements EntryAction<T> {
		private final boolean startForked;
		private final List<T> sources;

		public Continuation(String string, ContextChain<T> contextChain, boolean bl, List<T> list) {
			super(string, contextChain);
			this.startForked = bl;
			this.sources = list;
		}

		@Override
		public void execute(ExecutionContext<T> executionContext, int i) {
			this.execute(this.sources, executionContext, i, this.startForked);
		}
	}

	public static class TopLevel<T extends ExecutionCommandSource<T>> extends BuildContexts<T> implements EntryAction<T> {
		private final T source;

		public TopLevel(String string, ContextChain<T> contextChain, T executionCommandSource) {
			super(string, contextChain);
			this.source = executionCommandSource;
		}

		@Override
		public void execute(ExecutionContext<T> executionContext, int i) {
			this.traceCommandStart(executionContext, i);
			this.execute(List.of(this.source), executionContext, i, false);
		}
	}

	public static class Unbound<T extends ExecutionCommandSource<T>> extends BuildContexts<T> implements UnboundEntryAction<T> {
		public Unbound(String string, ContextChain<T> contextChain) {
			super(string, contextChain);
		}

		public void execute(T executionCommandSource, ExecutionContext<T> executionContext, int i) {
			this.traceCommandStart(executionContext, i);
			this.execute(List.of(executionCommandSource), executionContext, i, false);
		}
	}
}
