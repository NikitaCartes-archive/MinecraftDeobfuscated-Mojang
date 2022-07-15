/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.chat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChatPreviewAnimator {
    private static final long FADE_DURATION = 200L;
    @Nullable
    private Component residualPreview;
    private long fadeTime;
    private long lastTime;

    public void reset(long l) {
        this.residualPreview = null;
        this.fadeTime = 0L;
        this.lastTime = l;
    }

    public State get(long l, @Nullable Component component) {
        long m = l - this.lastTime;
        this.lastTime = l;
        if (component != null) {
            return this.getEnabled(m, component);
        }
        return this.getDisabled(m);
    }

    private State getEnabled(long l, Component component) {
        this.residualPreview = component;
        if (this.fadeTime < 200L) {
            this.fadeTime = Math.min(this.fadeTime + l, 200L);
        }
        return new State(component, ChatPreviewAnimator.alpha(this.fadeTime));
    }

    private State getDisabled(long l) {
        if (this.fadeTime > 0L) {
            this.fadeTime = Math.max(this.fadeTime - l, 0L);
        }
        return this.fadeTime > 0L ? new State(this.residualPreview, ChatPreviewAnimator.alpha(this.fadeTime)) : State.DISABLED;
    }

    private static float alpha(long l) {
        return (float)l / 200.0f;
    }

    @Environment(value=EnvType.CLIENT)
    public record State(@Nullable Component preview, float alpha) {
        public static final State DISABLED = new State(null, 0.0f);

        @Nullable
        public Component preview() {
            return this.preview;
        }
    }
}

