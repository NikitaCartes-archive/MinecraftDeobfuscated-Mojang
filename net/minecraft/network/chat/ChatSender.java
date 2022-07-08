/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.jetbrains.annotations.Nullable;

public record ChatSender(UUID profileId, @Nullable ProfilePublicKey profilePublicKey) {
    public static final ChatSender SYSTEM = new ChatSender(Util.NIL_UUID, null);

    public boolean isSystem() {
        return SYSTEM.equals(this);
    }

    @Nullable
    public ProfilePublicKey profilePublicKey() {
        return this.profilePublicKey;
    }
}

