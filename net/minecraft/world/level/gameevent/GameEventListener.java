/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.gameevent;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface GameEventListener {
    public PositionSource getListenerSource();

    public int getListenerRadius();

    public boolean handleGameEvent(ServerLevel var1, GameEvent var2, @Nullable Entity var3, Vec3 var4);
}

