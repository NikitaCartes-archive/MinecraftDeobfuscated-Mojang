package net.minecraft.world.level.gameevent;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class EuclideanGameEventDispatcher implements GameEventDispatcher {
	private final List<GameEventListener> listeners = Lists.<GameEventListener>newArrayList();
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
		this.listeners.add(gameEventListener);
		DebugPackets.sendGameEventListenerInfo(this.level, gameEventListener);
	}

	@Override
	public void unregister(GameEventListener gameEventListener) {
		this.listeners.remove(gameEventListener);
	}

	@Override
	public void post(GameEvent gameEvent, @Nullable Entity entity, BlockPos blockPos) {
		boolean bl = this.listeners.stream().filter(gameEventListener -> this.postToListener(this.level, gameEvent, entity, blockPos, gameEventListener)).count()
			> 0L;
		if (bl) {
			DebugPackets.sendGameEventInfo(this.level, gameEvent, blockPos);
		}
	}

	private boolean postToListener(Level level, GameEvent gameEvent, @Nullable Entity entity, BlockPos blockPos, GameEventListener gameEventListener) {
		Optional<BlockPos> optional = gameEventListener.getListenerSource().getPosition(level);
		if (!optional.isPresent()) {
			return false;
		} else {
			double d = ((BlockPos)optional.get()).distSqr(blockPos, false);
			int i = gameEventListener.getListenerRadius() * gameEventListener.getListenerRadius();
			return d <= (double)i && gameEventListener.handleGameEvent(level, gameEvent, entity, blockPos);
		}
	}
}
