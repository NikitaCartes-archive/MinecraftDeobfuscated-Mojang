package net.minecraft.world.level.gameevent;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;

public class DynamicGameEventListener {
	private final GameEventListener listener;
	@Nullable
	private SectionPos lastSection;

	public DynamicGameEventListener(GameEventListener gameEventListener) {
		this.listener = gameEventListener;
	}

	public void add(ServerLevel serverLevel) {
		this.move(serverLevel);
	}

	public void remove(ServerLevel serverLevel) {
		ifChunkExists(serverLevel, this.lastSection, gameEventDispatcher -> gameEventDispatcher.unregister(this.listener));
	}

	public void move(ServerLevel serverLevel) {
		this.listener.getListenerSource().getPosition(serverLevel).map(SectionPos::of).ifPresent(sectionPos -> {
			if (this.lastSection == null || !this.lastSection.equals(sectionPos)) {
				ifChunkExists(serverLevel, this.lastSection, gameEventDispatcher -> gameEventDispatcher.unregister(this.listener));
				this.lastSection = sectionPos;
				ifChunkExists(serverLevel, this.lastSection, gameEventDispatcher -> gameEventDispatcher.register(this.listener));
			}
		});
	}

	private static void ifChunkExists(LevelReader levelReader, @Nullable SectionPos sectionPos, Consumer<GameEventDispatcher> consumer) {
		if (sectionPos != null) {
			ChunkAccess chunkAccess = levelReader.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.FULL, false);
			if (chunkAccess != null) {
				consumer.accept(chunkAccess.getEventDispatcher(sectionPos.y()));
			}
		}
	}
}
