package net.minecraft.world.level.gameevent;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface GameEventDispatcher {
	GameEventDispatcher NOOP = new GameEventDispatcher() {
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

	boolean isEmpty();

	void register(GameEventListener gameEventListener);

	void unregister(GameEventListener gameEventListener);

	void post(GameEvent gameEvent, @Nullable Entity entity, Vec3 vec3);
}
