package net.minecraft.world.level.gameevent;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;

public class DynamicGameEventListener<T extends GameEventListener> {
	private T listener;
	@Nullable
	private SectionPos lastSection;

	public DynamicGameEventListener(T gameEventListener) {
		this.listener = gameEventListener;
	}

	public void add(ServerLevel serverLevel) {
		this.move(serverLevel);
	}

	public void updateListener(T gameEventListener, @Nullable Level level) {
		T gameEventListener2 = this.listener;
		if (gameEventListener2 != gameEventListener) {
			if (level instanceof ServerLevel serverLevel) {
				ifChunkExists(serverLevel, this.lastSection, gameEventListenerRegistry -> gameEventListenerRegistry.unregister(gameEventListener2));
				ifChunkExists(serverLevel, this.lastSection, gameEventListenerRegistry -> gameEventListenerRegistry.register(gameEventListener));
			}

			this.listener = gameEventListener;
		}
	}

	public T getListener() {
		return this.listener;
	}

	public void remove(ServerLevel serverLevel) {
		ifChunkExists(serverLevel, this.lastSection, gameEventListenerRegistry -> gameEventListenerRegistry.unregister(this.listener));
	}

	public void move(ServerLevel serverLevel) {
		this.listener.getListenerSource().getPosition(serverLevel).map(SectionPos::of).ifPresent(sectionPos -> {
			if (this.lastSection == null || !this.lastSection.equals(sectionPos)) {
				ifChunkExists(serverLevel, this.lastSection, gameEventListenerRegistry -> gameEventListenerRegistry.unregister(this.listener));
				this.lastSection = sectionPos;
				ifChunkExists(serverLevel, this.lastSection, gameEventListenerRegistry -> gameEventListenerRegistry.register(this.listener));
			}
		});
	}

	private static void ifChunkExists(LevelReader levelReader, @Nullable SectionPos sectionPos, Consumer<GameEventListenerRegistry> consumer) {
		if (sectionPos != null) {
			ChunkAccess chunkAccess = levelReader.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.FULL, false);
			if (chunkAccess != null) {
				consumer.accept(chunkAccess.getListenerRegistry(sectionPos.y()));
			}
		}
	}
}
