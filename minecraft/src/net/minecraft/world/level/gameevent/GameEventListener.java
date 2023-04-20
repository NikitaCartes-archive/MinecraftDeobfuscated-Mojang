package net.minecraft.world.level.gameevent;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public interface GameEventListener {
	PositionSource getListenerSource();

	int getListenerRadius();

	boolean handleGameEvent(ServerLevel serverLevel, GameEvent gameEvent, GameEvent.Context context, Vec3 vec3);

	default GameEventListener.DeliveryMode getDeliveryMode() {
		return GameEventListener.DeliveryMode.UNSPECIFIED;
	}

	public static enum DeliveryMode {
		UNSPECIFIED,
		BY_DISTANCE;
	}

	public interface Holder<T extends GameEventListener> {
		T getListener();
	}
}
