/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.multiplayer.chat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.chat.LoggedChatEvent;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChatLog {
    private final LoggedChatEvent[] buffer;
    private int nextId;

    public ChatLog(int i) {
        this.buffer = new LoggedChatEvent[i];
    }

    public void push(LoggedChatEvent loggedChatEvent) {
        this.buffer[this.index((int)this.nextId++)] = loggedChatEvent;
    }

    @Nullable
    public LoggedChatEvent lookup(int i) {
        return i >= this.start() && i <= this.end() ? this.buffer[this.index(i)] : null;
    }

    private int index(int i) {
        return i % this.buffer.length;
    }

    public int start() {
        return Math.max(this.nextId - this.buffer.length, 0);
    }

    public int end() {
        return this.nextId - 1;
    }
}

