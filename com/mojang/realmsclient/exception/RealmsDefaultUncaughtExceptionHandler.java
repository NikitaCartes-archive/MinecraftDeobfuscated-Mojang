/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.exception;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsDefaultUncaughtExceptionHandler
implements Thread.UncaughtExceptionHandler {
    private final Logger logger;

    public RealmsDefaultUncaughtExceptionHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        this.logger.error("Caught previously unhandled exception :");
        this.logger.error(throwable);
    }
}

