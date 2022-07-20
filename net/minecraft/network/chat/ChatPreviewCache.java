/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class ChatPreviewCache {
    @Nullable
    private Result result;

    public void set(String string, Component component) {
        this.result = new Result(string, component);
    }

    @Nullable
    public Component pull(String string) {
        Result result = this.result;
        if (result != null && result.matches(string)) {
            this.result = null;
            return result.preview();
        }
        return null;
    }

    record Result(String query, Component preview) {
        public boolean matches(String string) {
            return this.query.equals(string);
        }
    }
}

