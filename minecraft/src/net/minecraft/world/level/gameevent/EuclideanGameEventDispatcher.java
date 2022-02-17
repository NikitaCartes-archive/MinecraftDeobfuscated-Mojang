package net.minecraft.world.level.gameevent;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EuclideanGameEventDispatcher implements GameEventDispatcher {
	private final List<GameEventListener> listeners = Lists.<GameEventListener>newArrayList();
	private final Set<GameEventListener> listenersToRemove = Sets.<GameEventListener>newHashSet();
	private final List<GameEventListener> listenersToAdd = Lists.<GameEventListener>newArrayList();
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
		(this.processing ? this.listenersToAdd : this.listeners).add(gameEventListener);
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

	@Override
	public void post(GameEvent gameEvent, @Nullable Entity entity, Vec3 vec3) {
		boolean bl = false;
		this.processing = true;

		try {
			Iterator<GameEventListener> iterator = this.listeners.iterator();

			while (iterator.hasNext()) {
				GameEventListener gameEventListener = (GameEventListener)iterator.next();
				if (this.listenersToRemove.contains(gameEventListener)) {
					this.listenersToRemove.remove(gameEventListener);
					iterator.remove();
				} else {
					bl |= postToListener(this.level, gameEvent, entity, vec3, gameEventListener);
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

		if (bl) {
			DebugPackets.sendGameEventInfo(this.level, gameEvent, vec3);
		}
	}

	private static boolean postToListener(Level level, GameEvent gameEvent, @Nullable Entity entity, Vec3 vec3, GameEventListener gameEventListener) {
		Optional<Vec3> optional = gameEventListener.getListenerSource().getPosition(level);
		if (optional.isEmpty()) {
			return false;
		} else {
			return ((Vec3)optional.get()).distanceToSqr(vec3) <= (double)(gameEventListener.getListenerRadius() * gameEventListener.getListenerRadius())
				? gameEventListener.handleGameEvent(level, gameEvent, entity, vec3)
				: false;
		}
	}
}
