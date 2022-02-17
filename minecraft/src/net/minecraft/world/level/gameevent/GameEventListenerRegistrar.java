package net.minecraft.world.level.gameevent;

import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;

public class GameEventListenerRegistrar {
	private final GameEventListener listener;
	@Nullable
	private SectionPos sectionPos;

	public GameEventListenerRegistrar(GameEventListener gameEventListener) {
		this.listener = gameEventListener;
	}

	public void onListenerRemoved(Level level) {
		this.ifEventDispatcherExists(level, this.sectionPos, gameEventDispatcher -> gameEventDispatcher.unregister(this.listener));
	}

	public void onListenerMove(Level level) {
		Optional<BlockPos> optional = this.listener.getListenerSource().getPosition(level).map(BlockPos::new);
		if (optional.isPresent()) {
			long l = SectionPos.blockToSection(((BlockPos)optional.get()).asLong());
			if (this.sectionPos == null || this.sectionPos.asLong() != l) {
				SectionPos sectionPos = this.sectionPos;
				this.sectionPos = SectionPos.of(l);
				this.ifEventDispatcherExists(level, sectionPos, gameEventDispatcher -> gameEventDispatcher.unregister(this.listener));
				this.ifEventDispatcherExists(level, this.sectionPos, gameEventDispatcher -> gameEventDispatcher.register(this.listener));
			}
		}
	}

	private void ifEventDispatcherExists(Level level, @Nullable SectionPos sectionPos, Consumer<GameEventDispatcher> consumer) {
		if (sectionPos != null) {
			ChunkAccess chunkAccess = level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.FULL, false);
			if (chunkAccess != null) {
				consumer.accept(chunkAccess.getEventDispatcher(sectionPos.y()));
			}
		}
	}
}
