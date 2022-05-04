package net.minecraft.world.level.gameevent;

import java.util.function.BiConsumer;
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
		public boolean walkListeners(GameEvent gameEvent, Vec3 vec3, GameEvent.Context context, BiConsumer<GameEventListener, Vec3> biConsumer) {
			return false;
		}
	};

	boolean isEmpty();

	void register(GameEventListener gameEventListener);

	void unregister(GameEventListener gameEventListener);

	boolean walkListeners(GameEvent gameEvent, Vec3 vec3, GameEvent.Context context, BiConsumer<GameEventListener, Vec3> biConsumer);
}
