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
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;

@Environment(value=EnvType.CLIENT)
class RepeatedNarrator {
    final Duration repeatDelay;
    private final float permitsPerSecond;
    final AtomicReference<Params> params;

    public RepeatedNarrator(Duration duration) {
        this.repeatDelay = duration;
        this.params = new AtomicReference();
        float f = (float)duration.toMillis() / 1000.0f;
        this.permitsPerSecond = 1.0f / f;
    }

    public void narrate(String string) {
        Params params2 = this.params.updateAndGet(params -> {
            if (params == null || !string.equals(params.narration)) {
                return new Params(string, RateLimiter.create(this.permitsPerSecond));
            }
            return params;
        });
        if (params2.rateLimiter.tryAcquire(1)) {
            NarratorChatListener narratorChatListener = NarratorChatListener.INSTANCE;
            narratorChatListener.handle(ChatType.SYSTEM, new TextComponent(string));
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Params {
        String narration;
        RateLimiter rateLimiter;

        Params(String string, RateLimiter rateLimiter) {
            this.narration = string;
            this.rateLimiter = rateLimiter;
        }
    }
}

