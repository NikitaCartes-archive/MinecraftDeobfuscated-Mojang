package net.minecraft.gametest.framework;

public interface GameTestBatchListener {
	void testBatchStarting(GameTestBatch gameTestBatch);

	void testBatchFinished(GameTestBatch gameTestBatch);
}
