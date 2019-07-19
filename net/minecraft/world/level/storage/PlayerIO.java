/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface PlayerIO {
    public void save(Player var1);

    @Nullable
    public CompoundTag load(Player var1);
}

