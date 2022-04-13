package net.minecraft.world.level.gameevent;

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
		public void post(GameEvent gameEvent, Vec3 vec3, GameEvent.Context context) {
		}
	};

	boolean isEmpty();

	void register(GameEventListener gameEventListener);

	void unregister(GameEventListener gameEventListener);

	void post(GameEvent gameEvent, Vec3 vec3, GameEvent.Context context);
}
