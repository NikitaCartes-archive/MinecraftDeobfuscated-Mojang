/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.gametest.framework;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import net.minecraft.Util;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.TestReporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TeamcityTestReporter
implements TestReporter {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Escaper ESCAPER = Escapers.builder().addEscape('\'', "|'").addEscape('\n', "|n").addEscape('\r', "|r").addEscape('|', "||").addEscape('[', "|[").addEscape(']', "|]").build();

    @Override
    public void onTestFailed(GameTestInfo gameTestInfo) {
        String string = ESCAPER.escape(gameTestInfo.getTestName());
        String string2 = ESCAPER.escape(gameTestInfo.getError().getMessage());
        String string3 = ESCAPER.escape(Util.describeError(gameTestInfo.getError()));
        LOGGER.info("##teamcity[testStarted name='{}']", (Object)string);
        if (gameTestInfo.isRequired()) {
            LOGGER.info("##teamcity[testFailed name='{}' message='{}' details='{}']", (Object)string, (Object)string2, (Object)string3);
        } else {
            LOGGER.info("##teamcity[testIgnored name='{}' message='{}' details='{}']", (Object)string, (Object)string2, (Object)string3);
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

