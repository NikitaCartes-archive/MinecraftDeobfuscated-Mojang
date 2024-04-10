package net.minecraft.world.level.entity;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;

public class TransientEntitySectionManager<T extends EntityAccess> {
	static final Logger LOGGER = LogUtils.getLogger();
	final LevelCallback<T> callbacks;
	final EntityLookup<T> entityStorage;
	final EntitySectionStorage<T> sectionStorage;
	private final LongSet tickingChunks = new LongOpenHashSet();
	private final LevelEntityGetter<T> entityGetter;

	public TransientEntitySectionManager(Class<T> class_, LevelCallback<T> levelCallback) {
		this.entityStorage = new EntityLookup<>();
		this.sectionStorage = new EntitySectionStorage<>(class_, l -> this.tickingChunks.contains(l) ? Visibility.TICKING : Visibility.TRACKED);
		this.callbacks = levelCallback;
		this.entityGetter = new LevelEntityGetterAdapter<>(this.entityStorage, this.sectionStorage);
	}

	public void startTicking(ChunkPos chunkPos) {
		long l = chunkPos.toLong();
		this.tickingChunks.add(l);
		this.sectionStorage.getExistingSectionsInChunk(l).forEach(entitySection -> {
			Visibility visibility = entitySection.updateChunkStatus(Visibility.TICKING);
			if (!visibility.isTicking()) {
				entitySection.getEntities().filter(entityAccess -> !entityAccess.isAlwaysTicking()).forEach(this.callbacks::onTickingStart);
			}
		});
	}

	public void stopTicking(ChunkPos chunkPos) {
		long l = chunkPos.toLong();
		this.tickingChunks.remove(l);
		this.sectionStorage.getExistingSectionsInChunk(l).forEach(entitySection -> {
			Visibility visibility = entitySection.updateChunkStatus(Visibility.TRACKED);
			if (visibility.isTicking()) {
				entitySection.getEntities().filter(entityAccess -> !entityAccess.isAlwaysTicking()).forEach(this.callbacks::onTickingEnd);
			}
		});
	}

	public LevelEntityGetter<T> getEntityGetter() {
		return this.entityGetter;
	}

	public void addEntity(T entityAccess) {
		this.entityStorage.add(entityAccess);
		long l = SectionPos.asLong(entityAccess.blockPosition());
		EntitySection<T> entitySection = this.sectionStorage.getOrCreateSection(l);
		entitySection.add(entityAccess);
		entityAccess.setLevelCallback(new TransientEntitySectionManager.Callback(entityAccess, l, entitySection));
		this.callbacks.onCreated(entityAccess);
		this.callbacks.onTrackingStart(entityAccess);
		if (entityAccess.isAlwaysTicking() || entitySection.getStatus().isTicking()) {
			this.callbacks.onTickingStart(entityAccess);
		}
	}

	@VisibleForDebug
	public int count() {
		return this.entityStorage.count();
	}

	void removeSectionIfEmpty(long l, EntitySection<T> entitySection) {
		if (entitySection.isEmpty()) {
			this.sectionStorage.remove(l);
		}
	}

	@VisibleForDebug
	public String gatherStats() {
		return this.entityStorage.count() + "," + this.sectionStorage.count() + "," + this.tickingChunks.size();
	}

	class Callback implements EntityInLevelCallback {
		private final T entity;
		private long currentSectionKey;
		private EntitySection<T> currentSection;

		Callback(final T entityAccess, final long l, final EntitySection<T> entitySection) {
			this.entity = entityAccess;
			this.currentSectionKey = l;
			this.currentSection = entitySection;
		}

		@Override
		public void onMove() {
			BlockPos blockPos = this.entity.blockPosition();
			long l = SectionPos.asLong(blockPos);
			if (l != this.currentSectionKey) {
				Visibility visibility = this.currentSection.getStatus();
				if (!this.currentSection.remove(this.entity)) {
					TransientEntitySectionManager.LOGGER.warn("Entity {} wasn't found in section {} (moving to {})", this.entity, SectionPos.of(this.currentSectionKey), l);
				}

				TransientEntitySectionManager.this.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
				EntitySection<T> entitySection = TransientEntitySectionManager.this.sectionStorage.getOrCreateSection(l);
				entitySection.add(this.entity);
				this.currentSection = entitySection;
				this.currentSectionKey = l;
				TransientEntitySectionManager.this.callbacks.onSectionChange(this.entity);
				if (!this.entity.isAlwaysTicking()) {
					boolean bl = visibility.isTicking();
					boolean bl2 = entitySection.getStatus().isTicking();
					if (bl && !bl2) {
						TransientEntitySectionManager.this.callbacks.onTickingEnd(this.entity);
					} else if (!bl && bl2) {
						TransientEntitySectionManager.this.callbacks.onTickingStart(this.entity);
					}
				}
			}
		}

		@Override
		public void onRemove(Entity.RemovalReason removalReason) {
			if (!this.currentSection.remove(this.entity)) {
				TransientEntitySectionManager.LOGGER
					.warn("Entity {} wasn't found in section {} (destroying due to {})", this.entity, SectionPos.of(this.currentSectionKey), removalReason);
			}

			Visibility visibility = this.currentSection.getStatus();
			if (visibility.isTicking() || this.entity.isAlwaysTicking()) {
				TransientEntitySectionManager.this.callbacks.onTickingEnd(this.entity);
			}

			TransientEntitySectionManager.this.callbacks.onTrackingEnd(this.entity);
			TransientEntitySectionManager.this.callbacks.onDestroyed(this.entity);
			TransientEntitySectionManager.this.entityStorage.remove(this.entity);
			this.entity.setLevelCallback(NULL);
			TransientEntitySectionManager.this.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
		}
	}
}
