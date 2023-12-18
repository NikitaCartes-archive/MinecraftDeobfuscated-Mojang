package net.minecraft.world.level.gameevent;

import net.minecraft.core.Holder;
import net.minecraft.world.phys.Vec3;

public interface GameEventListenerRegistry {
	GameEventListenerRegistry NOOP = new GameEventListenerRegistry() {
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
		public boolean visitInRangeListeners(
			Holder<GameEvent> holder, Vec3 vec3, GameEvent.Context context, GameEventListenerRegistry.ListenerVisitor listenerVisitor
		) {
			return false;
		}
	};

	boolean isEmpty();

	void register(GameEventListener gameEventListener);

	void unregister(GameEventListener gameEventListener);

	boolean visitInRangeListeners(Holder<GameEvent> holder, Vec3 vec3, GameEvent.Context context, GameEventListenerRegistry.ListenerVisitor listenerVisitor);

	@FunctionalInterface
	public interface ListenerVisitor {
		void visit(GameEventListener gameEventListener, Vec3 vec3);
	}
}
