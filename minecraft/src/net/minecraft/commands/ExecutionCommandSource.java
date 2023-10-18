package net.minecraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.function.IntConsumer;
import javax.annotation.Nullable;
import net.minecraft.commands.execution.TraceCallbacks;

public interface ExecutionCommandSource<T extends ExecutionCommandSource<T>> {
	boolean hasPermission(int i);

	void storeResults(boolean bl, int i);

	void storeReturnValue(int i);

	T withReturnValueConsumer(IntConsumer intConsumer);

	T withCallback(CommandResultConsumer<T> commandResultConsumer);

	T clearCallbacks();

	CommandDispatcher<T> dispatcher();

	void handleError(CommandExceptionType commandExceptionType, Message message, boolean bl, @Nullable TraceCallbacks traceCallbacks);

	default void handleError(CommandSyntaxException commandSyntaxException, boolean bl, @Nullable TraceCallbacks traceCallbacks) {
		this.handleError(commandSyntaxException.getType(), commandSyntaxException.getRawMessage(), bl, traceCallbacks);
	}

	static <T extends ExecutionCommandSource<T>> ResultConsumer<T> resultConsumer() {
		return (commandContext, bl, i) -> commandContext.getSource().storeResults(bl, i);
	}
}
