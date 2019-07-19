/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft;

import org.apache.logging.log4j.Logger;

public class DefaultUncaughtExceptionHandler
implements Thread.UncaughtExceptionHandler {
    private final Logger logger;

    public DefaultUncaughtExceptionHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        this.logger.error("Caught previously unhandled exception :", throwable);
    }
}

