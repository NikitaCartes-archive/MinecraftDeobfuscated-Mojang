/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.ticks;

import java.util.function.Function;
import net.minecraft.nbt.Tag;

public interface SerializableTickContainer<T> {
    public Tag save(long var1, Function<T, String> var3);
}

