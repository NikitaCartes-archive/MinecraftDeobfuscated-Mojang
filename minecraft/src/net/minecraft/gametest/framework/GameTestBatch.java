package net.minecraft.gametest.framework;

import java.util.Collection;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;

public class GameTestBatch {
	public static final String DEFAULT_BATCH_NAME = "defaultBatch";
	private final String name;
	private final Collection<TestFunction> testFunctions;
	@Nullable
	private final Consumer<ServerLevel> beforeBatchFunction;
	@Nullable
	private final Consumer<ServerLevel> afterBatchFunction;

	public GameTestBatch(String string, Collection<TestFunction> collection, @Nullable Consumer<ServerLevel> consumer, @Nullable Consumer<ServerLevel> consumer2) {
		if (collection.isEmpty()) {
			throw new IllegalArgumentException("A GameTestBatch must include at least one TestFunction!");
		} else {
			this.name = string;
			this.testFunctions = collection;
			this.beforeBatchFunction = consumer;
			this.afterBatchFunction = consumer2;
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

	public void runAfterBatchFunction(ServerLevel serverLevel) {
		if (this.afterBatchFunction != null) {
			this.afterBatchFunction.accept(serverLevel);
		}
	}
}
