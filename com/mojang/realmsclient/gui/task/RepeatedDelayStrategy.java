/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.task;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public interface RepeatedDelayStrategy {
    public static final RepeatedDelayStrategy CONSTANT = new RepeatedDelayStrategy(){

        @Override
        public long delayCyclesAfterSuccess() {
            return 1L;
        }

        @Override
        public long delayCyclesAfterFailure() {
            return 1L;
        }
    };

    public long delayCyclesAfterSuccess();

    public long delayCyclesAfterFailure();

    public static RepeatedDelayStrategy exponentialBackoff(final int i) {
        return new RepeatedDelayStrategy(){
            private static final Logger LOGGER = LogUtils.getLogger();
            private int failureCount;

            @Override
            public long delayCyclesAfterSuccess() {
                this.failureCount = 0;
                return 1L;
            }

            @Override
            public long delayCyclesAfterFailure() {
                ++this.failureCount;
                long l = Math.min(1L << this.failureCount, (long)i);
                LOGGER.debug("Skipping for {} extra cycles", (Object)l);
                return l;
            }
        };
    }
}

