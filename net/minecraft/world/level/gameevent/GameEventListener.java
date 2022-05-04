/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.gameevent;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;

public interface GameEventListener {
    default public boolean handleEventsImmediately() {
        return false;
    }

    public PositionSource getListenerSource();

    public int getListenerRadius();

    public boolean handleGameEvent(ServerLevel var1, GameEvent.Message var2);
}

