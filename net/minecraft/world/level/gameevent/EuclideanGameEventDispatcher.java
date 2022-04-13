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
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventDispatcher;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class EuclideanGameEventDispatcher
implements GameEventDispatcher {
    private final List<GameEventListener> listeners = Lists.newArrayList();
    private final Set<GameEventListener> listenersToRemove = Sets.newHashSet();
    private final List<GameEventListener> listenersToAdd = Lists.newArrayList();
    private boolean processing;
    private final ServerLevel level;

    public EuclideanGameEventDispatcher(ServerLevel serverLevel) {
        this.level = serverLevel;
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
    public void post(GameEvent gameEvent, Vec3 vec3, @Nullable GameEvent.Context context) {
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
                if (!EuclideanGameEventDispatcher.postToListener(this.level, gameEvent, context, vec3, gameEventListener)) continue;
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
            DebugPackets.sendGameEventInfo(this.level, gameEvent, vec3);
        }
    }

    private static boolean postToListener(ServerLevel serverLevel, GameEvent gameEvent, GameEvent.Context context, Vec3 vec3, GameEventListener gameEventListener) {
        int i;
        Optional<Vec3> optional = gameEventListener.getListenerSource().getPosition(serverLevel);
        if (optional.isEmpty()) {
            return false;
        }
        double d = optional.get().distanceToSqr(vec3);
        return d <= (double)(i = gameEventListener.getListenerRadius() * gameEventListener.getListenerRadius()) && gameEventListener.handleGameEvent(serverLevel, gameEvent, context, vec3);
    }
}

