/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.gametest.framework;

import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.TestReporter;
import org.slf4j.Logger;

public class LogTestReporter
implements TestReporter {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onTestFailed(GameTestInfo gameTestInfo) {
        if (gameTestInfo.isRequired()) {
            LOGGER.error("{} failed! {}", (Object)gameTestInfo.getTestName(), (Object)Util.describeError(gameTestInfo.getError()));
        } else {
            LOGGER.warn("(optional) {} failed. {}", (Object)gameTestInfo.getTestName(), (Object)Util.describeError(gameTestInfo.getError()));
        }
    }

    @Override
    public void onTestSuccess(GameTestInfo gameTestInfo) {
    }
}

