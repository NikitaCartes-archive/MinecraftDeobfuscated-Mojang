package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.SectionTracker;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public abstract class LayerLightSectionStorage<M extends DataLayerStorageMap<M>> extends SectionTracker {
	protected static final int LIGHT_AND_DATA = 0;
	protected static final int LIGHT_ONLY = 1;
	protected static final int EMPTY = 2;
	protected static final DataLayer EMPTY_DATA = new DataLayer();
	private static final Direction[] DIRECTIONS = Direction.values();
	private final LightLayer layer;
	private final LightChunkGetter chunkSource;
	protected final LongSet dataSectionSet = new LongOpenHashSet();
	protected final LongSet toMarkNoData = new LongOpenHashSet();
	protected final LongSet toMarkData = new LongOpenHashSet();
	protected volatile M visibleSectionData;
	protected final M updatingSectionData;
	protected final LongSet changedSections = new LongOpenHashSet();
	protected final LongSet sectionsAffectedByLightUpdates = new LongOpenHashSet();
	protected final Long2ObjectMap<DataLayer> queuedSections = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());
	private final LongSet untrustedSections = new LongOpenHashSet();
	private final LongSet columnsToRetainQueuedDataFor = new LongOpenHashSet();
	private final LongSet toRemove = new LongOpenHashSet();
	protected volatile boolean hasToRemove;

	protected LayerLightSectionStorage(LightLayer lightLayer, LightChunkGetter lightChunkGetter, M dataLayerStorageMap) {
		super(3, 16, 256);
		this.layer = lightLayer;
		this.chunkSource = lightChunkGetter;
		this.updatingSectionData = dataLayerStorageMap;
		this.visibleSectionData = dataLayerStorageMap.copy();
		this.visibleSectionData.disableCache();
	}

	protected boolean storingLightForSection(long l) {
		return this.getDataLayer(l, true) != null;
	}

	@Nullable
	protected DataLayer getDataLayer(long l, boolean bl) {
		return this.getDataLayer(bl ? this.updatingSectionData : this.visibleSectionData, l);
	}

	@Nullable
	protected DataLayer getDataLayer(M dataLayerStorageMap, long l) {
		return dataLayerStorageMap.getLayer(l);
	}

	@Nullable
	public DataLayer getDataLayerData(long l) {
		DataLayer dataLayer = this.queuedSections.get(l);
		return dataLayer != null ? dataLayer : this.getDataLayer(l, false);
	}

	protected abstract int getLightValue(long l);

	protected int getStoredLevel(long l) {
		long m = SectionPos.blockToSection(l);
		DataLayer dataLayer = this.getDataLayer(m, true);
		return dataLayer.get(SectionPos.sectionRelative(BlockPos.getX(l)), SectionPos.sectionRelative(BlockPos.getY(l)), SectionPos.sectionRelative(BlockPos.getZ(l)));
	}

	protected void setStoredLevel(long l, int i) {
		long m = SectionPos.blockToSection(l);
		if (this.changedSections.add(m)) {
			this.updatingSectionData.copyDataLayer(m);
		}

		DataLayer dataLayer = this.getDataLayer(m, true);
		dataLayer.set(SectionPos.sectionRelative(BlockPos.getX(l)), SectionPos.sectionRelative(BlockPos.getY(l)), SectionPos.sectionRelative(BlockPos.getZ(l)), i);
		SectionPos.aroundAndAtBlockPos(l, this.sectionsAffectedByLightUpdates::add);
	}

	@Override
	protected int getLevel(long l) {
		if (l == Long.MAX_VALUE) {
			return 2;
		} else if (this.dataSectionSet.contains(l)) {
			return 0;
		} else {
			return !this.toRemove.contains(l) && this.updatingSectionData.hasLayer(l) ? 1 : 2;
		}
	}

	@Override
	protected int getLevelFromSource(long l) {
		if (this.toMarkNoData.contains(l)) {
			return 2;
		} else {
			return !this.dataSectionSet.contains(l) && !this.toMarkData.contains(l) ? 2 : 0;
		}
	}

	@Override
	protected void setLevel(long l, int i) {
		int j = this.getLevel(l);
		if (j != 0 && i == 0) {
			this.dataSectionSet.add(l);
			this.toMarkData.remove(l);
		}

		if (j == 0 && i != 0) {
			this.dataSectionSet.remove(l);
			this.toMarkNoData.remove(l);
		}

		if (j >= 2 && i != 2) {
			if (this.toRemove.contains(l)) {
				this.toRemove.remove(l);
			} else {
				this.updatingSectionData.setLayer(l, this.createDataLayer(l));
				this.changedSections.add(l);
				this.onNodeAdded(l);
				int k = SectionPos.x(l);
				int m = SectionPos.y(l);
				int n = SectionPos.z(l);

				for (int o = -1; o <= 1; o++) {
					for (int p = -1; p <= 1; p++) {
						for (int q = -1; q <= 1; q++) {
							this.sectionsAffectedByLightUpdates.add(SectionPos.asLong(k + p, m + q, n + o));
						}
					}
				}
			}
		}

		if (j != 2 && i >= 2) {
			this.toRemove.add(l);
		}

		this.hasToRemove = !this.toRemove.isEmpty();
	}

	protected DataLayer createDataLayer(long l) {
		DataLayer dataLayer = this.queuedSections.get(l);
		return dataLayer != null ? dataLayer : new DataLayer();
	}

	protected void clearQueuedSectionBlocks(LayerLightEngine<?, ?> layerLightEngine, long l) {
		if (layerLightEngine.getQueueSize() != 0) {
			if (layerLightEngine.getQueueSize() < 8192) {
				layerLightEngine.removeIf(mx -> SectionPos.blockToSection(mx) == l);
			} else {
				int i = SectionPos.sectionToBlockCoord(SectionPos.x(l));
				int j = SectionPos.sectionToBlockCoord(SectionPos.y(l));
				int k = SectionPos.sectionToBlockCoord(SectionPos.z(l));

				for (int m = 0; m < 16; m++) {
					for (int n = 0; n < 16; n++) {
						for (int o = 0; o < 16; o++) {
							long p = BlockPos.asLong(i + m, j + n, k + o);
							layerLightEngine.removeFromQueue(p);
						}
					}
				}
			}
		}
	}

	protected boolean hasInconsistencies() {
		return this.hasToRemove;
	}

	protected void markNewInconsistencies(LayerLightEngine<M, ?> layerLightEngine, boolean bl, boolean bl2) {
		if (this.hasInconsistencies() || !this.queuedSections.isEmpty()) {
			LongIterator objectIterator = this.toRemove.iterator();

			while (objectIterator.hasNext()) {
				long l = (Long)objectIterator.next();
				this.clearQueuedSectionBlocks(layerLightEngine, l);
				DataLayer dataLayer = this.queuedSections.remove(l);
				DataLayer dataLayer2 = this.updatingSectionData.removeLayer(l);
				if (this.columnsToRetainQueuedDataFor.contains(SectionPos.getZeroNode(l))) {
					if (dataLayer != null) {
						this.queuedSections.put(l, dataLayer);
					} else if (dataLayer2 != null) {
						this.queuedSections.put(l, dataLayer2);
					}
				}
			}

			this.updatingSectionData.clearCache();
			objectIterator = this.toRemove.iterator();

			while (objectIterator.hasNext()) {
				long l = (Long)objectIterator.next();
				this.onNodeRemoved(l);
			}

			this.toRemove.clear();
			this.hasToRemove = false;

			for (Entry<DataLayer> entry : this.queuedSections.long2ObjectEntrySet()) {
				long m = entry.getLongKey();
				if (this.storingLightForSection(m)) {
					DataLayer dataLayer2 = (DataLayer)entry.getValue();
					if (this.updatingSectionData.getLayer(m) != dataLayer2) {
						this.clearQueuedSectionBlocks(layerLightEngine, m);
						this.updatingSectionData.setLayer(m, dataLayer2);
						this.changedSections.add(m);
					}
				}
			}

			this.updatingSectionData.clearCache();
			if (!bl2) {
				objectIterator = this.queuedSections.keySet().iterator();

				while (objectIterator.hasNext()) {
					long l = (Long)objectIterator.next();
					this.checkEdgesForSection(layerLightEngine, l);
				}
			} else {
				objectIterator = this.untrustedSections.iterator();

				while (objectIterator.hasNext()) {
					long l = (Long)objectIterator.next();
					this.checkEdgesForSection(layerLightEngine, l);
				}
			}

			this.untrustedSections.clear();
			ObjectIterator<Entry<DataLayer>> objectIteratorx = this.queuedSections.long2ObjectEntrySet().iterator();

			while (objectIteratorx.hasNext()) {
				Entry<DataLayer> entryx = (Entry<DataLayer>)objectIteratorx.next();
				long m = entryx.getLongKey();
				if (this.storingLightForSection(m)) {
					objectIteratorx.remove();
				}
			}
		}
	}

	private void checkEdgesForSection(LayerLightEngine<M, ?> layerLightEngine, long l) {
		if (this.storingLightForSection(l)) {
			int i = SectionPos.sectionToBlockCoord(SectionPos.x(l));
			int j = SectionPos.sectionToBlockCoord(SectionPos.y(l));
			int k = SectionPos.sectionToBlockCoord(SectionPos.z(l));

			for (Direction direction : DIRECTIONS) {
				long m = SectionPos.offset(l, direction);
				if (!this.queuedSections.containsKey(m) && this.storingLightForSection(m)) {
					for (int n = 0; n < 16; n++) {
						for (int o = 0; o < 16; o++) {
							long p;
							long q;
							switch (direction) {
								case DOWN:
									p = BlockPos.asLong(i + o, j, k + n);
									q = BlockPos.asLong(i + o, j - 1, k + n);
									break;
								case UP:
									p = BlockPos.asLong(i + o, j + 16 - 1, k + n);
									q = BlockPos.asLong(i + o, j + 16, k + n);
									break;
								case NORTH:
									p = BlockPos.asLong(i + n, j + o, k);
									q = BlockPos.asLong(i + n, j + o, k - 1);
									break;
								case SOUTH:
									p = BlockPos.asLong(i + n, j + o, k + 16 - 1);
									q = BlockPos.asLong(i + n, j + o, k + 16);
									break;
								case WEST:
									p = BlockPos.asLong(i, j + n, k + o);
									q = BlockPos.asLong(i - 1, j + n, k + o);
									break;
								default:
									p = BlockPos.asLong(i + 16 - 1, j + n, k + o);
									q = BlockPos.asLong(i + 16, j + n, k + o);
							}

							layerLightEngine.checkEdge(p, q, layerLightEngine.computeLevelFromNeighbor(p, q, layerLightEngine.getLevel(p)), false);
							layerLightEngine.checkEdge(q, p, layerLightEngine.computeLevelFromNeighbor(q, p, layerLightEngine.getLevel(q)), false);
						}
					}
				}
			}
		}
	}

	protected void onNodeAdded(long l) {
	}

	protected void onNodeRemoved(long l) {
	}

	protected void enableLightSources(long l, boolean bl) {
	}

	public void retainData(long l, boolean bl) {
		if (bl) {
			this.columnsToRetainQueuedDataFor.add(l);
		} else {
			this.columnsToRetainQueuedDataFor.remove(l);
		}
	}

	protected void queueSectionData(long l, @Nullable DataLayer dataLayer, boolean bl) {
		if (dataLayer != null) {
			this.queuedSections.put(l, dataLayer);
			if (!bl) {
				this.untrustedSections.add(l);
			}
		} else {
			this.queuedSections.remove(l);
		}
	}

	protected void updateSectionStatus(long l, boolean bl) {
		boolean bl2 = this.dataSectionSet.contains(l);
		if (!bl2 && !bl) {
			this.toMarkData.add(l);
			this.checkEdge(Long.MAX_VALUE, l, 0, true);
		}

		if (bl2 && bl) {
			this.toMarkNoData.add(l);
			this.checkEdge(Long.MAX_VALUE, l, 2, false);
		}
	}

	protected void runAllUpdates() {
		if (this.hasWork()) {
			this.runUpdates(Integer.MAX_VALUE);
		}
	}

	protected void swapSectionMap() {
		if (!this.changedSections.isEmpty()) {
			M dataLayerStorageMap = this.updatingSectionData.copy();
			dataLayerStorageMap.disableCache();
			this.visibleSectionData = dataLayerStorageMap;
			this.changedSections.clear();
		}

		if (!this.sectionsAffectedByLightUpdates.isEmpty()) {
			LongIterator longIterator = this.sectionsAffectedByLightUpdates.iterator();

			while (longIterator.hasNext()) {
				long l = longIterator.nextLong();
				this.chunkSource.onLightUpdate(this.layer, SectionPos.of(l));
			}

			this.sectionsAffectedByLightUpdates.clear();
		}
	}
}
