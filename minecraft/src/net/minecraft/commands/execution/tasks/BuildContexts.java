package net.minecraft.commands.execution.tasks;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.context.ContextChain.Stage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.CustomModifierExecutor;
import net.minecraft.commands.execution.EntryAction;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.Frame;
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

	protected void execute(T executionCommandSource, List<T> list, ExecutionContext<T> executionContext, Frame frame, ChainModifiers chainModifiers) {
		ContextChain<T> contextChain = this.command;
		ChainModifiers chainModifiers2 = chainModifiers;
		List<T> list2 = list;
		if (contextChain.getStage() != Stage.EXECUTE) {
			executionContext.profiler().push((Supplier<String>)(() -> "prepare " + this.commandInput));

			try {
				for (int i = executionContext.forkLimit(); contextChain.getStage() != Stage.EXECUTE; contextChain = contextChain.nextStage()) {
					CommandContext<T> commandContext = contextChain.getTopContext();
					if (commandContext.isForked()) {
						chainModifiers2 = chainModifiers2.setForked();
					}

					RedirectModifier<T> redirectModifier = commandContext.getRedirectModifier();
					if (redirectModifier instanceof CustomModifierExecutor<T> customModifierExecutor) {
						customModifierExecutor.apply(executionCommandSource, list2, contextChain, chainModifiers2, ExecutionControl.create(executionContext, frame));
						return;
					}

					if (redirectModifier != null) {
						executionContext.incrementCost();
						boolean bl = chainModifiers2.isForked();
						List<T> list3 = new ObjectArrayList<>();

						for (T executionCommandSource2 : list2) {
							try {
								Collection<T> collection = ContextChain.runModifier(commandContext, executionCommandSource2, (commandContextx, blx, ix) -> {
								}, bl);
								if (list3.size() + collection.size() >= i) {
									executionCommandSource.handleError(ERROR_FORK_LIMIT_REACHED.create(i), bl, executionContext.tracer());
									return;
								}

								list3.addAll(collection);
							} catch (CommandSyntaxException var20) {
								executionCommandSource2.handleError(var20, bl, executionContext.tracer());
								if (!bl) {
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

		if (list2.isEmpty()) {
			if (chainModifiers2.isReturn()) {
				executionContext.queueNext(new CommandQueueEntry<>(frame, FallthroughTask.instance()));
			}
		} else {
			CommandContext<T> commandContext2 = contextChain.getTopContext();
			if (commandContext2.getCommand() instanceof CustomCommandExecutor<T> customCommandExecutor) {
				ExecutionControl<T> executionControl = ExecutionControl.create(executionContext, frame);

				for (T executionCommandSource3 : list2) {
					customCommandExecutor.run(executionCommandSource3, contextChain, chainModifiers2, executionControl);
				}
			} else {
				if (chainModifiers2.isReturn()) {
					T executionCommandSource4 = (T)list2.get(0);
					executionCommandSource4 = executionCommandSource4.withCallback(
						CommandResultCallback.chain(executionCommandSource4.callback(), frame.returnValueConsumer())
					);
					list2 = List.of(executionCommandSource4);
				}

				ExecuteCommand<T> executeCommand = new ExecuteCommand<>(this.commandInput, chainModifiers2, commandContext2);
				ContinuationTask.schedule(
					executionContext, frame, list2, (framex, executionCommandSourcex) -> new CommandQueueEntry<>(framex, executeCommand.bind((T)executionCommandSourcex))
				);
			}
		}
	}

	protected void traceCommandStart(ExecutionContext<T> executionContext, Frame frame) {
		TraceCallbacks traceCallbacks = executionContext.tracer();
		if (traceCallbacks != null) {
			traceCallbacks.onCommand(frame.depth(), this.commandInput);
		}
	}

	public String toString() {
		return this.commandInput;
	}

	public static class Continuation<T extends ExecutionCommandSource<T>> extends BuildContexts<T> implements EntryAction<T> {
		private final ChainModifiers modifiers;
		private final T originalSource;
		private final List<T> sources;

		public Continuation(String string, ContextChain<T> contextChain, ChainModifiers chainModifiers, T executionCommandSource, List<T> list) {
			super(string, contextChain);
			this.originalSource = executionCommandSource;
			this.sources = list;
			this.modifiers = chainModifiers;
		}

		@Override
		public void execute(ExecutionContext<T> executionContext, Frame frame) {
			this.execute(this.originalSource, this.sources, executionContext, frame, this.modifiers);
		}
	}

	public static class TopLevel<T extends ExecutionCommandSource<T>> extends BuildContexts<T> implements EntryAction<T> {
		private final T source;

		public TopLevel(String string, ContextChain<T> contextChain, T executionCommandSource) {
			super(string, contextChain);
			this.source = executionCommandSource;
		}

		@Override
		public void execute(ExecutionContext<T> executionContext, Frame frame) {
			this.traceCommandStart(executionContext, frame);
			this.execute(this.source, List.of(this.source), executionContext, frame, ChainModifiers.DEFAULT);
		}
	}

	public static class Unbound<T extends ExecutionCommandSource<T>> extends BuildContexts<T> implements UnboundEntryAction<T> {
		public Unbound(String string, ContextChain<T> contextChain) {
			super(string, contextChain);
		}

		public void execute(T executionCommandSource, ExecutionContext<T> executionContext, Frame frame) {
			this.traceCommandStart(executionContext, frame);
			this.execute(executionCommandSource, List.of(executionCommandSource), executionContext, frame, ChainModifiers.DEFAULT);
		}
	}
}
