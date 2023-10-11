package net.minecraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ResultConsumer;
import java.util.function.IntConsumer;

public interface ExecutionCommandSource<T extends ExecutionCommandSource<T>> {
	boolean hasPermission(int i);

	void storeResults(boolean bl, int i);

	void storeReturnValue(int i);

	T withReturnValueConsumer(IntConsumer intConsumer);

	T withCallback(CommandResultConsumer<T> commandResultConsumer);

	T clearCallbacks();

	CommandDispatcher<T> dispatcher();

	static <T extends ExecutionCommandSource<T>> ResultConsumer<T> resultConsumer() {
		return (commandContext, bl, i) -> commandContext.getSource().storeResults(bl, i);
	}
}
