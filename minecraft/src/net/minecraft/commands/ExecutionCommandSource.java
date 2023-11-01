package net.minecraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.commands.execution.TraceCallbacks;

public interface ExecutionCommandSource<T extends ExecutionCommandSource<T>> {
	boolean hasPermission(int i);

	T withCallback(CommandResultCallback commandResultCallback);

	CommandResultCallback callback();

	default T clearCallbacks() {
		return this.withCallback(CommandResultCallback.EMPTY);
	}

	CommandDispatcher<T> dispatcher();

	void handleError(CommandExceptionType commandExceptionType, Message message, boolean bl, @Nullable TraceCallbacks traceCallbacks);

	boolean isSilent();

	default void handleError(CommandSyntaxException commandSyntaxException, boolean bl, @Nullable TraceCallbacks traceCallbacks) {
		this.handleError(commandSyntaxException.getType(), commandSyntaxException.getRawMessage(), bl, traceCallbacks);
	}

	static <T extends ExecutionCommandSource<T>> ResultConsumer<T> resultConsumer() {
		return (commandContext, bl, i) -> commandContext.getSource().callback().onResult(bl, i);
	}
}
