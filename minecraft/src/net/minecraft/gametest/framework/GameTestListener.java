package net.minecraft.gametest.framework;

public interface GameTestListener {
	void testStructureLoaded(GameTestInfo gameTestInfo);

	void testPassed(GameTestInfo gameTestInfo, GameTestRunner gameTestRunner);

	void testFailed(GameTestInfo gameTestInfo, GameTestRunner gameTestRunner);

	void testAddedForRerun(GameTestInfo gameTestInfo, GameTestInfo gameTestInfo2, GameTestRunner gameTestRunner);
}
