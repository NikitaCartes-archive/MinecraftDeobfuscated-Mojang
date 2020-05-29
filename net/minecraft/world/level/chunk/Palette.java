/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public interface Palette<T> {
    public int idFor(T var1);

    public boolean maybeHas(Predicate<T> var1);

    @Nullable
    public T valueFor(int var1);

    @Environment(value=EnvType.CLIENT)
    public void read(FriendlyByteBuf var1);

    public void write(FriendlyByteBuf var1);

    public int getSerializedSize();

    public void read(ListTag var1);
}

