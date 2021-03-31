package net.minecraft.gametest.framework;

public class GlobalTestReporter {
	private static TestReporter DELEGATE = new LogTestReporter();

	public static void replaceWith(TestReporter testReporter) {
		DELEGATE = testReporter;
	}

	public static void onTestFailed(GameTestInfo gameTestInfo) {
		DELEGATE.onTestFailed(gameTestInfo);
	}

	public static void onTestSuccess(GameTestInfo gameTestInfo) {
		DELEGATE.onTestSuccess(gameTestInfo);
	}

	public static void finish() {
		DELEGATE.finish();
	}
}
