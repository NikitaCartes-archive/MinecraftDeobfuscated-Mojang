package net.minecraft.gametest.framework;

class ExhaustedAttemptsException extends Throwable {
	public ExhaustedAttemptsException(int i, int j, GameTestInfo gameTestInfo) {
		super(
			"Not enough successes: "
				+ j
				+ " out of "
				+ i
				+ " attempts. Required successes: "
				+ gameTestInfo.requiredSuccesses()
				+ ". max attempts: "
				+ gameTestInfo.maxAttempts()
				+ ".",
			gameTestInfo.getError()
		);
	}
}
