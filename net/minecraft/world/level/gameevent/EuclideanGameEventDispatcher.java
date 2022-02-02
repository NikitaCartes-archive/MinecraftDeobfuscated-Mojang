/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.gameevent;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventDispatcher;
import net.minecraft.world.level.gameevent.GameEventListener;
import org.jetbrains.annotations.Nullable;

public class EuclideanGameEventDispatcher
implements GameEventDispatcher {
    private final List<GameEventListener> listeners = Lists.newArrayList();
    private final Level level;

    public EuclideanGameEventDispatcher(Level level) {
        this.level = level;
    }

    @Override
    public boolean isEmpty() {
        return this.listeners.isEmpty();
    }

    @Override
    public void register(GameEventListener gameEventListener) {
        this.listeners.add(gameEventListener);
        DebugPackets.sendGameEventListenerInfo(this.level, gameEventListener);
    }

    @Override
    public void unregister(GameEventListener gameEventListener) {
        this.listeners.remove(gameEventListener);
    }

    @Override
    public void post(GameEvent gameEvent, @Nullable Entity entity, BlockPos blockPos) {
        boolean bl = false;
        for (GameEventListener gameEventListener : this.listeners) {
            if (!this.postToListener(this.level, gameEvent, entity, blockPos, gameEventListener)) continue;
            bl = true;
        }
        if (bl) {
            DebugPackets.sendGameEventInfo(this.level, gameEvent, blockPos);
        }
    }

    private boolean postToListener(Level level, GameEvent gameEvent, @Nullable Entity entity, BlockPos blockPos, GameEventListener gameEventListener) {
        int i;
        Optional<BlockPos> optional = gameEventListener.getListenerSource().getPosition(level);
        if (!optional.isPresent()) {
            return false;
        }
        double d = optional.get().distSqr(blockPos);
        return d <= (double)(i = gameEventListener.getListenerRadius() * gameEventListener.getListenerRadius()) && gameEventListener.handleGameEvent(level, gameEvent, entity, blockPos);
    }
}

