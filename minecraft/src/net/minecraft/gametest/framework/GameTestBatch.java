package net.minecraft.gametest.framework;

import java.util.Collection;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;

public record GameTestBatch(
	String name, Collection<GameTestInfo> gameTestInfos, Consumer<ServerLevel> beforeBatchFunction, Consumer<ServerLevel> afterBatchFunction
) {
	public static final String DEFAULT_BATCH_NAME = "defaultBatch";

	public GameTestBatch(String string, Collection<GameTestInfo> collection, Consumer<ServerLevel> consumer, Consumer<ServerLevel> consumer2) {
		if (collection.isEmpty()) {
			throw new IllegalArgumentException("A GameTestBatch must include at least one GameTestInfo!");
		} else {
			this.name = string;
			this.gameTestInfos = collection;
			this.beforeBatchFunction = consumer;
			this.afterBatchFunction = consumer2;
		}
	}
}
