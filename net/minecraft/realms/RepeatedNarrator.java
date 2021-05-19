/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.realms;

import com.google.common.util.concurrent.RateLimiter;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public class RepeatedNarrator {
    private final float permitsPerSecond;
    private final AtomicReference<Params> params = new AtomicReference();

    public RepeatedNarrator(Duration duration) {
        this.permitsPerSecond = 1000.0f / (float)duration.toMillis();
    }

    public void narrate(Component component) {
        Params params2 = this.params.updateAndGet(params -> {
            if (params == null || !component.equals(params.narration)) {
                return new Params(component, RateLimiter.create(this.permitsPerSecond));
            }
            return params;
        });
        if (params2.rateLimiter.tryAcquire(1)) {
            NarratorChatListener.INSTANCE.sayNow(component);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Params {
        final Component narration;
        final RateLimiter rateLimiter;

        Params(Component component, RateLimiter rateLimiter) {
            this.narration = component;
            this.rateLimiter = rateLimiter;
        }
    }
}

