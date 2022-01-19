/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.rcon.thread;

import com.mojang.logging.LogUtils;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.DefaultUncaughtExceptionHandlerWithName;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class GenericThread
implements Runnable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    private static final int MAX_STOP_WAIT = 5;
    protected volatile boolean running;
    protected final String name;
    @Nullable
    protected Thread thread;

    protected GenericThread(String string) {
        this.name = string;
    }

    public synchronized boolean start() {
        if (this.running) {
            return true;
        }
        this.running = true;
        this.thread = new Thread((Runnable)this, this.name + " #" + UNIQUE_THREAD_ID.incrementAndGet());
        this.thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandlerWithName(LOGGER));
        this.thread.start();
        LOGGER.info("Thread {} started", (Object)this.name);
        return true;
    }

    public synchronized void stop() {
        this.running = false;
        if (null == this.thread) {
            return;
        }
        int i = 0;
        while (this.thread.isAlive()) {
            try {
                this.thread.join(1000L);
                if (++i >= 5) {
                    LOGGER.warn("Waited {} seconds attempting force stop!", (Object)i);
                    continue;
                }
                if (!this.thread.isAlive()) continue;
                LOGGER.warn("Thread {} ({}) failed to exit after {} second(s)", new Object[]{this, this.thread.getState(), i, new Exception("Stack:")});
                this.thread.interrupt();
            } catch (InterruptedException interruptedException) {}
        }
        LOGGER.info("Thread {} stopped", (Object)this.name);
        this.thread = null;
    }

    public boolean isRunning() {
        return this.running;
    }
}

