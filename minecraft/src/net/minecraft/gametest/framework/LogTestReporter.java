package net.minecraft.gametest.framework;

import net.minecraft.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogTestReporter implements TestReporter {
	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void onTestFailed(GameTestInfo gameTestInfo) {
		if (gameTestInfo.isRequired()) {
			LOGGER.error("{} failed! {}", gameTestInfo.getTestName(), Util.describeError(gameTestInfo.getError()));
		} else {
			LOGGER.warn("(optional) {} failed. {}", gameTestInfo.getTestName(), Util.describeError(gameTestInfo.getError()));
		}
	}
}
