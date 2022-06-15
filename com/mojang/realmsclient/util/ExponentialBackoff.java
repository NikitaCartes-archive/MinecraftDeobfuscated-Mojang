/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.util;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ExponentialBackoff
implements Runnable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Task task;
    private final int maxSkips;
    private int toSkip;
    private int currentSkips;

    public ExponentialBackoff(Task task, int i) {
        this.task = task;
        this.maxSkips = i;
    }

    @Override
    public void run() {
        if (this.toSkip > this.currentSkips) {
            ++this.currentSkips;
            return;
        }
        this.currentSkips = 0;
        try {
            this.task.run();
            this.toSkip = 0;
        } catch (Exception exception) {
            this.toSkip = this.toSkip == 0 ? 1 : Math.min(2 * this.toSkip, this.maxSkips);
            LOGGER.info("Skipping next {}", (Object)this.toSkip);
        }
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface Task {
        public void run() throws Exception;
    }
}

