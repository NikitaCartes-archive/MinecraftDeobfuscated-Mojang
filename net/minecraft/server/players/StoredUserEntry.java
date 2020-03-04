/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.players;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public abstract class StoredUserEntry<T> {
    @Nullable
    private final T user;

    public StoredUserEntry(@Nullable T object) {
        this.user = object;
    }

    @Nullable
    T getUser() {
        return this.user;
    }

    boolean hasExpired() {
        return false;
    }

    protected abstract void serialize(JsonObject var1);
}

