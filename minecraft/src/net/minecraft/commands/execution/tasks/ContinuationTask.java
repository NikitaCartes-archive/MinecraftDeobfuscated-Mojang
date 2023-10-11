package net.minecraft.commands.execution.tasks;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.EntryAction;
import net.minecraft.commands.execution.ExecutionContext;

public class ContinuationTask<T, P> implements EntryAction<T> {
	private final ContinuationTask.TaskProvider<T, P> taskFactory;
	private final List<P> arguments;
	private final CommandQueueEntry<T> selfEntry;
	private int index;

	private ContinuationTask(ContinuationTask.TaskProvider<T, P> taskProvider, List<P> list, int i) {
		this.taskFactory = taskProvider;
		this.arguments = list;
		this.selfEntry = new CommandQueueEntry<>(i, this);
	}

	@Override
	public void execute(ExecutionContext<T> executionContext, int i) throws CommandSyntaxException {
		P object = (P)this.arguments.get(this.index);
		executionContext.queueNext(this.taskFactory.create(i, object));
		if (++this.index < this.arguments.size()) {
			executionContext.queueNext(this.selfEntry);
		}
	}

	public static <T, P> void schedule(ExecutionContext<T> executionContext, int i, List<P> list, ContinuationTask.TaskProvider<T, P> taskProvider) {
		int j = list.size();
		if (j != 0) {
			if (j == 1) {
				executionContext.queueNext(taskProvider.create(i, (P)list.get(0)));
			} else if (j == 2) {
				executionContext.queueNext(taskProvider.create(i, (P)list.get(0)));
				executionContext.queueNext(taskProvider.create(i, (P)list.get(1)));
			} else {
				executionContext.queueNext((new ContinuationTask<>(taskProvider, list, i)).selfEntry);
			}
		}
	}

	@FunctionalInterface
	public interface TaskProvider<T, P> {
		CommandQueueEntry<T> create(int i, P object);
	}
}
