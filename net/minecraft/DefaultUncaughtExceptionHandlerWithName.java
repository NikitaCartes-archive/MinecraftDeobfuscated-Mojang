/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft;

import org.apache.logging.log4j.Logger;

public class DefaultUncaughtExceptionHandlerWithName
implements Thread.UncaughtExceptionHandler {
    private final Logger logger;

    public DefaultUncaughtExceptionHandlerWithName(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        this.logger.error("Caught previously unhandled exception :");
        this.logger.error(thread.getName(), throwable);
    }
}

