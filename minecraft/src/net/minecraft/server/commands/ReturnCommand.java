package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.ContextChain;
import java.util.List;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.CustomModifierExecutor;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.tasks.BuildContexts;

public class ReturnCommand {
	public static <T extends ExecutionCommandSource<T>> void register(CommandDispatcher<T> commandDispatcher) {
		commandDispatcher.register(
			((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("return").requires(executionCommandSource -> executionCommandSource.hasPermission(2)))
				.then(RequiredArgumentBuilder.<T, Integer>argument("value", IntegerArgumentType.integer()).executes(new ReturnCommand.ReturnValueCustomExecutor<>()))
				.then(LiteralArgumentBuilder.<T>literal("run").forward(commandDispatcher.getRoot(), new ReturnCommand.ReturnFromCommandCustomModifier<>(), false))
		);
	}

	static class ReturnFromCommandCustomModifier<T extends ExecutionCommandSource<T>> implements CustomModifierExecutor.ModifierAdapter<T> {
		@Override
		public void apply(List<T> list, ContextChain<T> contextChain, boolean bl, ExecutionControl<T> executionControl) {
			if (!list.isEmpty()) {
				ContextChain<T> contextChain2 = contextChain.nextStage();
				String string = contextChain2.getTopContext().getInput();
				List<T> list2 = list.stream().map(executionCommandSource -> executionCommandSource.withCallback((executionCommandSourcex, blx, i) -> {
						executionControl.discardCurrentDepth();
						executionCommandSourcex.storeReturnValue(i);
					})).toList();
				executionControl.queueNext(new BuildContexts.Continuation<>(string, contextChain2, bl, list2));
			}
		}
	}

	static class ReturnValueCustomExecutor<T extends ExecutionCommandSource<T>> implements CustomCommandExecutor.CommandAdapter<T> {
		public void run(T executionCommandSource, ContextChain<T> contextChain, boolean bl, ExecutionControl<T> executionControl) {
			executionControl.discardCurrentDepth();
			int i = IntegerArgumentType.getInteger(contextChain.getTopContext(), "value");
			executionCommandSource.storeReturnValue(i);
		}
	}
}
