package net.minecraft.gametest.framework;

import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import org.slf4j.Logger;

public class LogTestReporter implements TestReporter {
	private static final Logger LOGGER = LogUtils.getLogger();

	@Override
	public void onTestFailed(GameTestInfo gameTestInfo) {
		String string = gameTestInfo.getStructureBlockPos().toShortString();
		if (gameTestInfo.isRequired()) {
			LOGGER.error("{} failed at {}! {}", gameTestInfo.getTestName(), string, Util.describeError(gameTestInfo.getError()));
		} else {
			LOGGER.warn("(optional) {} failed at {}. {}", gameTestInfo.getTestName(), string, Util.describeError(gameTestInfo.getError()));
		}
	}

	@Override
	public void onTestSuccess(GameTestInfo gameTestInfo) {
	}
}
