package net.minecraft.gametest.framework;

public interface TestReporter {
	void onTestFailed(GameTestInfo gameTestInfo);

	void onTestSuccess(GameTestInfo gameTestInfo);

	default void finish() {
	}
}
