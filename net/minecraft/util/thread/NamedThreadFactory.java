/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NamedThreadFactory
implements ThreadFactory {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public NamedThreadFactory(String string) {
        SecurityManager securityManager = System.getSecurityManager();
        this.group = securityManager != null ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.namePrefix = string + "-";
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread2 = new Thread(this.group, runnable, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
        thread2.setUncaughtExceptionHandler((thread, throwable) -> {
            LOGGER.error("Caught exception in thread {} from {}", (Object)thread, (Object)runnable);
            LOGGER.error("", throwable);
        });
        if (thread2.getPriority() != 5) {
            thread2.setPriority(5);
        }
        return thread2;
    }
}

