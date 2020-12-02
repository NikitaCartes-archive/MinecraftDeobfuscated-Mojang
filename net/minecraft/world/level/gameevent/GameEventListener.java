/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.gameevent;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import org.jetbrains.annotations.Nullable;

public interface GameEventListener {
    public PositionSource getListenerSource();

    public int getListenerRadius();

    public boolean handleGameEvent(Level var1, GameEvent var2, @Nullable Entity var3, BlockPos var4);
}

