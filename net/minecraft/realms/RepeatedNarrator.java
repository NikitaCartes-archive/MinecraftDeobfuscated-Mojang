/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.realms;

import com.google.common.util.concurrent.RateLimiter;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;

@Environment(value=EnvType.CLIENT)
public class RepeatedNarrator {
    private final float permitsPerSecond;
    private final AtomicReference<Params> params = new AtomicReference();

    public RepeatedNarrator(Duration duration) {
        this.permitsPerSecond = 1000.0f / (float)duration.toMillis();
    }

    public void narrate(String string) {
        Params params2 = this.params.updateAndGet(params -> {
            if (params == null || !string.equals(params.narration)) {
                return new Params(string, RateLimiter.create(this.permitsPerSecond));
            }
            return params;
        });
        if (params2.rateLimiter.tryAcquire(1)) {
            NarratorChatListener.INSTANCE.handle(ChatType.SYSTEM, new TextComponent(string), Util.NIL_UUID);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Params {
        final String narration;
        final RateLimiter rateLimiter;

        Params(String string, RateLimiter rateLimiter) {
            this.narration = string;
            this.rateLimiter = rateLimiter;
        }
    }
}

