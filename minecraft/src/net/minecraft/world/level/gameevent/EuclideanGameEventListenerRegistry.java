package net.minecraft.world.level.gameevent;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class EuclideanGameEventListenerRegistry implements GameEventListenerRegistry {
	private final List<GameEventListener> listeners = Lists.<GameEventListener>newArrayList();
	private final Set<GameEventListener> listenersToRemove = Sets.<GameEventListener>newHashSet();
	private final List<GameEventListener> listenersToAdd = Lists.<GameEventListener>newArrayList();
	private boolean processing;
	private final ServerLevel level;
	private final int sectionY;
	private final EuclideanGameEventListenerRegistry.OnEmptyAction onEmptyAction;

	public EuclideanGameEventListenerRegistry(ServerLevel serverLevel, int i, EuclideanGameEventListenerRegistry.OnEmptyAction onEmptyAction) {
		this.level = serverLevel;
		this.sectionY = i;
		this.onEmptyAction = onEmptyAction;
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

		if (this.listeners.isEmpty()) {
			this.onEmptyAction.apply(this.sectionY);
		}
	}

	@Override
	public boolean visitInRangeListeners(GameEvent gameEvent, Vec3 vec3, GameEvent.Context context, GameEventListenerRegistry.ListenerVisitor listenerVisitor) {
		this.processing = true;
		boolean bl = false;

		try {
			Iterator<GameEventListener> iterator = this.listeners.iterator();

			while (iterator.hasNext()) {
				GameEventListener gameEventListener = (GameEventListener)iterator.next();
				if (this.listenersToRemove.remove(gameEventListener)) {
					iterator.remove();
				} else {
					Optional<Vec3> optional = getPostableListenerPosition(this.level, vec3, gameEventListener);
					if (optional.isPresent()) {
						listenerVisitor.visit(gameEventListener, (Vec3)optional.get());
						bl = true;
					}
				}
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

		return bl;
	}

	private static Optional<Vec3> getPostableListenerPosition(ServerLevel serverLevel, Vec3 vec3, GameEventListener gameEventListener) {
		Optional<Vec3> optional = gameEventListener.getListenerSource().getPosition(serverLevel);
		if (optional.isEmpty()) {
			return Optional.empty();
		} else {
			double d = ((Vec3)optional.get()).distanceToSqr(vec3);
			int i = gameEventListener.getListenerRadius() * gameEventListener.getListenerRadius();
			return d > (double)i ? Optional.empty() : optional;
		}
	}

	@FunctionalInterface
	public interface OnEmptyAction {
		void apply(int i);
	}
}
