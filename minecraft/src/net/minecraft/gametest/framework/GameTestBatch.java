package net.minecraft.gametest.framework;

import java.util.Collection;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;

public class GameTestBatch {
	private final String name;
	private final Collection<TestFunction> testFunctions;
	@Nullable
	private final Consumer<ServerLevel> beforeBatchFunction;

	public GameTestBatch(String string, Collection<TestFunction> collection, @Nullable Consumer<ServerLevel> consumer) {
		if (collection.isEmpty()) {
			throw new IllegalArgumentException("A GameTestBatch must include at least one TestFunction!");
		} else {
			this.name = string;
			this.testFunctions = collection;
			this.beforeBatchFunction = consumer;
		}
	}

	public String getName() {
		return this.name;
	}

	public Collection<TestFunction> getTestFunctions() {
		return this.testFunctions;
	}

	public void runBeforeBatchFunction(ServerLevel serverLevel) {
		if (this.beforeBatchFunction != null) {
			this.beforeBatchFunction.accept(serverLevel);
		}
	}
}
