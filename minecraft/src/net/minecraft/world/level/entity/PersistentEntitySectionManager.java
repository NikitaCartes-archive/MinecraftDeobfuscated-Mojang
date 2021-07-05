package net.minecraft.world.level.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PersistentEntitySectionManager<T extends EntityAccess> implements AutoCloseable {
	static final Logger LOGGER = LogManager.getLogger();
	final Set<UUID> knownUuids = Sets.<UUID>newHashSet();
	final LevelCallback<T> callbacks;
	private final EntityPersistentStorage<T> permanentStorage;
	private final EntityLookup<T> visibleEntityStorage;
	final EntitySectionStorage<T> sectionStorage;
	private final LevelEntityGetter<T> entityGetter;
	private final Long2ObjectMap<Visibility> chunkVisibility = new Long2ObjectOpenHashMap<>();
	private final Long2ObjectMap<PersistentEntitySectionManager.ChunkLoadStatus> chunkLoadStatuses = new Long2ObjectOpenHashMap<>();
	private final LongSet chunksToUnload = new LongOpenHashSet();
	private final Queue<ChunkEntities<T>> loadingInbox = Queues.<ChunkEntities<T>>newConcurrentLinkedQueue();

	public PersistentEntitySectionManager(Class<T> class_, LevelCallback<T> levelCallback, EntityPersistentStorage<T> entityPersistentStorage) {
		this.visibleEntityStorage = new EntityLookup<>();
		this.sectionStorage = new EntitySectionStorage<>(class_, this.chunkVisibility);
		this.chunkVisibility.defaultReturnValue(Visibility.HIDDEN);
		this.chunkLoadStatuses.defaultReturnValue(PersistentEntitySectionManager.ChunkLoadStatus.FRESH);
		this.callbacks = levelCallback;
		this.permanentStorage = entityPersistentStorage;
		this.entityGetter = new LevelEntityGetterAdapter<>(this.visibleEntityStorage, this.sectionStorage);
	}

	void removeSectionIfEmpty(long l, EntitySection<T> entitySection) {
		if (entitySection.isEmpty()) {
			this.sectionStorage.remove(l);
		}
	}

	private boolean addEntityUuid(T entityAccess) {
		if (!this.knownUuids.add(entityAccess.getUUID())) {
			LOGGER.warn("UUID of added entity already exists: {}", entityAccess);
			return false;
		} else {
			return true;
		}
	}

	public boolean addNewEntity(T entityAccess) {
		return this.addEntity(entityAccess, false);
	}

	private boolean addEntity(T entityAccess, boolean bl) {
		if (!this.addEntityUuid(entityAccess)) {
			return false;
		} else {
			long l = SectionPos.asLong(entityAccess.blockPosition());
			EntitySection<T> entitySection = this.sectionStorage.getOrCreateSection(l);
			entitySection.add(entityAccess);
			entityAccess.setLevelCallback(new PersistentEntitySectionManager.Callback(entityAccess, l, entitySection));
			if (!bl) {
				this.callbacks.onCreated(entityAccess);
			}

			Visibility visibility = getEffectiveStatus(entityAccess, entitySection.getStatus());
			if (visibility.isAccessible()) {
				this.startTracking(entityAccess);
			}

			if (visibility.isTicking()) {
				this.startTicking(entityAccess);
			}

			return true;
		}
	}

	static <T extends EntityAccess> Visibility getEffectiveStatus(T entityAccess, Visibility visibility) {
		return entityAccess.isAlwaysTicking() ? Visibility.TICKING : visibility;
	}

	public void addLegacyChunkEntities(Stream<T> stream) {
		stream.forEach(entityAccess -> this.addEntity((T)entityAccess, true));
	}

	public void addWorldGenChunkEntities(Stream<T> stream) {
		stream.forEach(entityAccess -> this.addEntity((T)entityAccess, false));
	}

	void startTicking(T entityAccess) {
		this.callbacks.onTickingStart(entityAccess);
	}

	void stopTicking(T entityAccess) {
		this.callbacks.onTickingEnd(entityAccess);
	}

	void startTracking(T entityAccess) {
		this.visibleEntityStorage.add(entityAccess);
		this.callbacks.onTrackingStart(entityAccess);
	}

	void stopTracking(T entityAccess) {
		this.callbacks.onTrackingEnd(entityAccess);
		this.visibleEntityStorage.remove(entityAccess);
	}

	public void updateChunkStatus(ChunkPos chunkPos, ChunkHolder.FullChunkStatus fullChunkStatus) {
		Visibility visibility = Visibility.fromFullChunkStatus(fullChunkStatus);
		this.updateChunkStatus(chunkPos, visibility);
	}

	public void updateChunkStatus(ChunkPos chunkPos, Visibility visibility) {
		long l = chunkPos.toLong();
		if (visibility == Visibility.HIDDEN) {
			this.chunkVisibility.remove(l);
			this.chunksToUnload.add(l);
		} else {
			this.chunkVisibility.put(l, visibility);
			this.chunksToUnload.remove(l);
			this.ensureChunkQueuedForLoad(l);
		}

		this.sectionStorage.getExistingSectionsInChunk(l).forEach(entitySection -> {
			Visibility visibility2 = entitySection.updateChunkStatus(visibility);
			boolean bl = visibility2.isAccessible();
			boolean bl2 = visibility.isAccessible();
			boolean bl3 = visibility2.isTicking();
			boolean bl4 = visibility.isTicking();
			if (bl3 && !bl4) {
				entitySection.getEntities().filter(entityAccess -> !entityAccess.isAlwaysTicking()).forEach(this::stopTicking);
			}

			if (bl && !bl2) {
				entitySection.getEntities().filter(entityAccess -> !entityAccess.isAlwaysTicking()).forEach(this::stopTracking);
			} else if (!bl && bl2) {
				entitySection.getEntities().filter(entityAccess -> !entityAccess.isAlwaysTicking()).forEach(this::startTracking);
			}

			if (!bl3 && bl4) {
				entitySection.getEntities().filter(entityAccess -> !entityAccess.isAlwaysTicking()).forEach(this::startTicking);
			}
		});
	}

	private void ensureChunkQueuedForLoad(long l) {
		PersistentEntitySectionManager.ChunkLoadStatus chunkLoadStatus = this.chunkLoadStatuses.get(l);
		if (chunkLoadStatus == PersistentEntitySectionManager.ChunkLoadStatus.FRESH) {
			this.requestChunkLoad(l);
		}
	}

	private boolean storeChunkSections(long l, Consumer<T> consumer) {
		PersistentEntitySectionManager.ChunkLoadStatus chunkLoadStatus = this.chunkLoadStatuses.get(l);
		if (chunkLoadStatus == PersistentEntitySectionManager.ChunkLoadStatus.PENDING) {
			return false;
		} else {
			List<T> list = (List<T>)this.sectionStorage
				.getExistingSectionsInChunk(l)
				.flatMap(entitySection -> entitySection.getEntities().filter(EntityAccess::shouldBeSaved))
				.collect(Collectors.toList());
			if (list.isEmpty()) {
				if (chunkLoadStatus == PersistentEntitySectionManager.ChunkLoadStatus.LOADED) {
					this.permanentStorage.storeEntities(new ChunkEntities<>(new ChunkPos(l), ImmutableList.of()));
				}

				return true;
			} else if (chunkLoadStatus == PersistentEntitySectionManager.ChunkLoadStatus.FRESH) {
				this.requestChunkLoad(l);
				return false;
			} else {
				this.permanentStorage.storeEntities(new ChunkEntities<>(new ChunkPos(l), list));
				list.forEach(consumer);
				return true;
			}
		}
	}

	private void requestChunkLoad(long l) {
		this.chunkLoadStatuses.put(l, PersistentEntitySectionManager.ChunkLoadStatus.PENDING);
		ChunkPos chunkPos = new ChunkPos(l);
		this.permanentStorage.loadEntities(chunkPos).thenAccept(this.loadingInbox::add).exceptionally(throwable -> {
			LOGGER.error("Failed to read chunk {}", chunkPos, throwable);
			return null;
		});
	}

	private boolean processChunkUnload(long l) {
		boolean bl = this.storeChunkSections(l, entityAccess -> entityAccess.getPassengersAndSelf().forEach(this::unloadEntity));
		if (!bl) {
			return false;
		} else {
			this.chunkLoadStatuses.remove(l);
			return true;
		}
	}

	private void unloadEntity(EntityAccess entityAccess) {
		entityAccess.setRemoved(Entity.RemovalReason.UNLOADED_TO_CHUNK);
		entityAccess.setLevelCallback(EntityInLevelCallback.NULL);
	}

	private void processUnloads() {
		this.chunksToUnload.removeIf(l -> this.chunkVisibility.get(l) != Visibility.HIDDEN ? true : this.processChunkUnload(l));
	}

	private void processPendingLoads() {
		ChunkEntities<T> chunkEntities;
		while ((chunkEntities = (ChunkEntities<T>)this.loadingInbox.poll()) != null) {
			chunkEntities.getEntities().forEach(entityAccess -> this.addEntity((T)entityAccess, true));
			this.chunkLoadStatuses.put(chunkEntities.getPos().toLong(), PersistentEntitySectionManager.ChunkLoadStatus.LOADED);
		}
	}

	public void tick() {
		this.processPendingLoads();
		this.processUnloads();
	}

	private LongSet getAllChunksToSave() {
		LongSet longSet = this.sectionStorage.getAllChunksWithExistingSections();

		for (Entry<PersistentEntitySectionManager.ChunkLoadStatus> entry : Long2ObjectMaps.fastIterable(this.chunkLoadStatuses)) {
			if (entry.getValue() == PersistentEntitySectionManager.ChunkLoadStatus.LOADED) {
				longSet.add(entry.getLongKey());
			}
		}

		return longSet;
	}

	public void autoSave() {
		this.getAllChunksToSave().forEach(l -> {
			boolean bl = this.chunkVisibility.get(l) == Visibility.HIDDEN;
			if (bl) {
				this.processChunkUnload(l);
			} else {
				this.storeChunkSections(l, entityAccess -> {
				});
			}
		});
	}

	public void saveAll() {
		LongSet longSet = this.getAllChunksToSave();

		while (!longSet.isEmpty()) {
			this.permanentStorage.flush(false);
			this.processPendingLoads();
			longSet.removeIf(l -> {
				boolean bl = this.chunkVisibility.get(l) == Visibility.HIDDEN;
				return bl ? this.processChunkUnload(l) : this.storeChunkSections(l, entityAccess -> {
				});
			});
		}

		this.permanentStorage.flush(true);
	}

	public void close() throws IOException {
		this.saveAll();
		this.permanentStorage.close();
	}

	public boolean isLoaded(UUID uUID) {
		return this.knownUuids.contains(uUID);
	}

	public LevelEntityGetter<T> getEntityGetter() {
		return this.entityGetter;
	}

	public boolean isPositionTicking(BlockPos blockPos) {
		return this.chunkVisibility.get(ChunkPos.asLong(blockPos)).isTicking();
	}

	public boolean isPositionTicking(ChunkPos chunkPos) {
		return this.chunkVisibility.get(chunkPos.toLong()).isTicking();
	}

	public boolean areEntitiesLoaded(long l) {
		return this.chunkLoadStatuses.get(l) == PersistentEntitySectionManager.ChunkLoadStatus.LOADED;
	}

	public void dumpSections(Writer writer) throws IOException {
		CsvOutput csvOutput = CsvOutput.builder()
			.addColumn("x")
			.addColumn("y")
			.addColumn("z")
			.addColumn("visibility")
			.addColumn("load_status")
			.addColumn("entity_count")
			.build(writer);
		this.sectionStorage.getAllChunksWithExistingSections().forEach(l -> {
			PersistentEntitySectionManager.ChunkLoadStatus chunkLoadStatus = this.chunkLoadStatuses.get(l);
			this.sectionStorage.getExistingSectionPositionsInChunk(l).forEach(lx -> {
				EntitySection<T> entitySection = this.sectionStorage.getSection(lx);
				if (entitySection != null) {
					try {
						csvOutput.writeRow(SectionPos.x(lx), SectionPos.y(lx), SectionPos.z(lx), entitySection.getStatus(), chunkLoadStatus, entitySection.size());
					} catch (IOException var7) {
						throw new UncheckedIOException(var7);
					}
				}
			});
		});
	}

	@VisibleForDebug
	public String gatherStats() {
		return this.knownUuids.size()
			+ ","
			+ this.visibleEntityStorage.count()
			+ ","
			+ this.sectionStorage.count()
			+ ","
			+ this.chunkLoadStatuses.size()
			+ ","
			+ this.chunkVisibility.size()
			+ ","
			+ this.loadingInbox.size()
			+ ","
			+ this.chunksToUnload.size();
	}

	class Callback implements EntityInLevelCallback {
		private final T entity;
		private long currentSectionKey;
		private EntitySection<T> currentSection;

		Callback(T entityAccess, long l, EntitySection<T> entitySection) {
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
					PersistentEntitySectionManager.LOGGER.warn("Entity {} wasn't found in section {} (moving to {})", this.entity, SectionPos.of(this.currentSectionKey), l);
				}

				PersistentEntitySectionManager.this.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
				EntitySection<T> entitySection = PersistentEntitySectionManager.this.sectionStorage.getOrCreateSection(l);
				entitySection.add(this.entity);
				this.currentSection = entitySection;
				this.currentSectionKey = l;
				this.updateStatus(visibility, entitySection.getStatus());
			}
		}

		private void updateStatus(Visibility visibility, Visibility visibility2) {
			Visibility visibility3 = PersistentEntitySectionManager.getEffectiveStatus(this.entity, visibility);
			Visibility visibility4 = PersistentEntitySectionManager.getEffectiveStatus(this.entity, visibility2);
			if (visibility3 != visibility4) {
				boolean bl = visibility3.isAccessible();
				boolean bl2 = visibility4.isAccessible();
				if (bl && !bl2) {
					PersistentEntitySectionManager.this.stopTracking(this.entity);
				} else if (!bl && bl2) {
					PersistentEntitySectionManager.this.startTracking(this.entity);
				}

				boolean bl3 = visibility3.isTicking();
				boolean bl4 = visibility4.isTicking();
				if (bl3 && !bl4) {
					PersistentEntitySectionManager.this.stopTicking(this.entity);
				} else if (!bl3 && bl4) {
					PersistentEntitySectionManager.this.startTicking(this.entity);
				}
			}
		}

		@Override
		public void onRemove(Entity.RemovalReason removalReason) {
			if (!this.currentSection.remove(this.entity)) {
				PersistentEntitySectionManager.LOGGER
					.warn("Entity {} wasn't found in section {} (destroying due to {})", this.entity, SectionPos.of(this.currentSectionKey), removalReason);
			}

			Visibility visibility = PersistentEntitySectionManager.getEffectiveStatus(this.entity, this.currentSection.getStatus());
			if (visibility.isTicking()) {
				PersistentEntitySectionManager.this.stopTicking(this.entity);
			}

			if (visibility.isAccessible()) {
				PersistentEntitySectionManager.this.stopTracking(this.entity);
			}

			if (removalReason.shouldDestroy()) {
				PersistentEntitySectionManager.this.callbacks.onDestroyed(this.entity);
			}

			PersistentEntitySectionManager.this.knownUuids.remove(this.entity.getUUID());
			this.entity.setLevelCallback(NULL);
			PersistentEntitySectionManager.this.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
		}
	}

	static enum ChunkLoadStatus {
		FRESH,
		PENDING,
		LOADED;
	}
}
