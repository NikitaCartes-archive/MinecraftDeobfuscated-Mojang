/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.gametest.framework;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.TestReporter;
import org.slf4j.Logger;

public class TeamcityTestReporter
implements TestReporter {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Escaper ESCAPER = Escapers.builder().addEscape('\'', "|'").addEscape('\n', "|n").addEscape('\r', "|r").addEscape('|', "||").addEscape('[', "|[").addEscape(']', "|]").build();

    @Override
    public void onTestFailed(GameTestInfo gameTestInfo) {
        String string = ESCAPER.escape(gameTestInfo.getTestName());
        String string2 = ESCAPER.escape(gameTestInfo.getError().getMessage());
        String string3 = ESCAPER.escape(Util.describeError(gameTestInfo.getError()));
        LOGGER.info("##teamcity[testStarted name='{}']", (Object)string);
        if (gameTestInfo.isRequired()) {
            LOGGER.info("##teamcity[testFailed name='{}' message='{}' details='{}']", string, string2, string3);
        } else {
            LOGGER.info("##teamcity[testIgnored name='{}' message='{}' details='{}']", string, string2, string3);
        }
        LOGGER.info("##teamcity[testFinished name='{}' duration='{}']", (Object)string, (Object)gameTestInfo.getRunTime());
    }

    @Override
    public void onTestSuccess(GameTestInfo gameTestInfo) {
        String string = ESCAPER.escape(gameTestInfo.getTestName());
        LOGGER.info("##teamcity[testStarted name='{}']", (Object)string);
        LOGGER.info("##teamcity[testFinished name='{}' duration='{}']", (Object)string, (Object)gameTestInfo.getRunTime());
    }
}

