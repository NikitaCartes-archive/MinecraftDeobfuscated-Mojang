/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public enum ChatType {
    CHAT(0, false),
    SYSTEM(1, true),
    GAME_INFO(2, true);

    private final byte index;
    private final boolean interrupt;

    private ChatType(byte b, boolean bl) {
        this.index = b;
        this.interrupt = bl;
    }

    public byte getIndex() {
        return this.index;
    }

    public static ChatType getForIndex(byte b) {
        for (ChatType chatType : ChatType.values()) {
            if (b != chatType.index) continue;
            return chatType;
        }
        return CHAT;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean shouldInterrupt() {
        return this.interrupt;
    }
}

