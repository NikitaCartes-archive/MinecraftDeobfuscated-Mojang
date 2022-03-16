/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.gameevent;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    private final Set<GameEventListener> listenersToRemove = Sets.newHashSet();
    private final List<GameEventListener> listenersToAdd = Lists.newArrayList();
    private boolean processing = false;
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
        if (this.processing) {
            this.listenersToAdd.add(gameEventListener);
        } else {
            this.listeners.add(gameEventListener);
        }
        DebugPackets.sendGameEventListenerInfo(this.level, gameEventListener);
    }

    @Override
    public void unregister(GameEventListener gameEventListener) {
        if (this.processing) {
            this.listenersToRemove.add(gameEventListener);
        } else {
            this.listeners.remove(gameEventListener);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void post(GameEvent gameEvent, @Nullable Entity entity, BlockPos blockPos) {
        boolean bl = false;
        this.processing = true;
        try {
            Iterator<GameEventListener> iterator = this.listeners.iterator();
            while (iterator.hasNext()) {
                GameEventListener gameEventListener = iterator.next();
                if (this.listenersToRemove.remove(gameEventListener)) {
                    iterator.remove();
                    continue;
                }
                if (!this.postToListener(this.level, gameEvent, entity, blockPos, gameEventListener)) continue;
                bl = true;
            }
        } finally {
            this.processing = false;
        }
        if (!this.listenersToAdd.isEmpty()) {
            this.listeners.addAll(this.listenersToAdd);
            this.listenersToAdd.clear();
        }
        if (!this.listenersToRemove.isEmpty()) {
            this.listeners.removeAll(this.listenersToRemove);
            this.listenersToRemove.clear();
        }
        if (bl) {
            DebugPackets.sendGameEventInfo(this.level, gameEvent, blockPos);
        }
    }

    private boolean postToListener(Level level, GameEvent gameEvent, @Nullable Entity entity, BlockPos blockPos, GameEventListener gameEventListener) {
        int i;
        Optional<BlockPos> optional = gameEventListener.getListenerSource().getPosition(level);
        if (optional.isEmpty()) {
            return false;
        }
        double d = optional.get().distSqr(blockPos);
        return d <= (double)(i = gameEventListener.getListenerRadius() * gameEventListener.getListenerRadius()) && gameEventListener.handleGameEvent(level, gameEvent, entity, blockPos);
    }
}

