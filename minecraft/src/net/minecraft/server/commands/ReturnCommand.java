package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.ContextChain;
import java.util.List;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.CustomModifierExecutor;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.tasks.BuildContexts;
import net.minecraft.commands.execution.tasks.FallthroughTask;

public class ReturnCommand {
	public static <T extends ExecutionCommandSource<T>> void register(CommandDispatcher<T> commandDispatcher) {
		commandDispatcher.register(
			((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("return").requires(executionCommandSource -> executionCommandSource.hasPermission(2)))
				.then(RequiredArgumentBuilder.<T, Integer>argument("value", IntegerArgumentType.integer()).executes(new ReturnCommand.ReturnValueCustomExecutor<>()))
				.then(LiteralArgumentBuilder.<T>literal("fail").executes(new ReturnCommand.ReturnFailCustomExecutor<>()))
				.then(LiteralArgumentBuilder.<T>literal("run").forward(commandDispatcher.getRoot(), new ReturnCommand.ReturnFromCommandCustomModifier<>(), false))
		);
	}

	static class ReturnFailCustomExecutor<T extends ExecutionCommandSource<T>> implements CustomCommandExecutor.CommandAdapter<T> {
		public void run(T executionCommandSource, ContextChain<T> contextChain, ChainModifiers chainModifiers, ExecutionControl<T> executionControl) {
			executionCommandSource.callback().onFailure();
			Frame frame = executionControl.currentFrame();
			frame.returnFailure();
			frame.discard();
		}
	}

	static class ReturnFromCommandCustomModifier<T extends ExecutionCommandSource<T>> implements CustomModifierExecutor.ModifierAdapter<T> {
		public void apply(T executionCommandSource, List<T> list, ContextChain<T> contextChain, ChainModifiers chainModifiers, ExecutionControl<T> executionControl) {
			if (list.isEmpty()) {
				if (chainModifiers.isReturn()) {
					executionControl.queueNext(FallthroughTask.instance());
				}
			} else {
				executionControl.currentFrame().discard();
				ContextChain<T> contextChain2 = contextChain.nextStage();
				String string = contextChain2.getTopContext().getInput();
				executionControl.queueNext(new BuildContexts.Continuation<>(string, contextChain2, chainModifiers.setReturn(), executionCommandSource, list));
			}
		}
	}

	static class ReturnValueCustomExecutor<T extends ExecutionCommandSource<T>> implements CustomCommandExecutor.CommandAdapter<T> {
		public void run(T executionCommandSource, ContextChain<T> contextChain, ChainModifiers chainModifiers, ExecutionControl<T> executionControl) {
			int i = IntegerArgumentType.getInteger(contextChain.getTopContext(), "value");
			executionCommandSource.callback().onSuccess(i);
			Frame frame = executionControl.currentFrame();
			frame.returnSuccess(i);
			frame.discard();
		}
	}
}
