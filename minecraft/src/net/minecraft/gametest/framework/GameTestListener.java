package net.minecraft.gametest.framework;

public interface GameTestListener {
	void testStructureLoaded(GameTestInfo gameTestInfo);

	void testPassed(GameTestInfo gameTestInfo);

	void testFailed(GameTestInfo gameTestInfo);
}
