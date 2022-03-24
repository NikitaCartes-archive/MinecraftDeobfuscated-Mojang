/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.gameevent;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface GameEventDispatcher {
    public static final GameEventDispatcher NOOP = new GameEventDispatcher(){

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public void register(GameEventListener gameEventListener) {
        }

        @Override
        public void unregister(GameEventListener gameEventListener) {
        }

        @Override
        public void post(GameEvent gameEvent, @Nullable Entity entity, Vec3 vec3) {
        }
    };

    public boolean isEmpty();

    public void register(GameEventListener var1);

    public void unregister(GameEventListener var1);

    public void post(GameEvent var1, @Nullable Entity var2, Vec3 var3);
}

