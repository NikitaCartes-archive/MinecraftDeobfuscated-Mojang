package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
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
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public abstract class LayerLightSectionStorage<M extends DataLayerStorageMap<M>> {
	private final LightLayer layer;
	protected final LightChunkGetter chunkSource;
	protected final Long2ByteMap sectionStates = new Long2ByteOpenHashMap();
	private final LongSet columnsWithSources = new LongOpenHashSet();
	protected volatile M visibleSectionData;
	protected final M updatingSectionData;
	protected final LongSet changedSections = new LongOpenHashSet();
	protected final LongSet sectionsAffectedByLightUpdates = new LongOpenHashSet();
	protected final Long2ObjectMap<DataLayer> queuedSections = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());
	private final LongSet columnsToRetainQueuedDataFor = new LongOpenHashSet();
	private final LongSet toRemove = new LongOpenHashSet();
	protected volatile boolean hasInconsistencies;

	protected LayerLightSectionStorage(LightLayer lightLayer, LightChunkGetter lightChunkGetter, M dataLayerStorageMap) {
		this.layer = lightLayer;
		this.chunkSource = lightChunkGetter;
		this.updatingSectionData = dataLayerStorageMap;
		this.visibleSectionData = dataLayerStorageMap.copy();
		this.visibleSectionData.disableCache();
		this.sectionStates.defaultReturnValue((byte)0);
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
	protected DataLayer getDataLayerToWrite(long l) {
		DataLayer dataLayer = this.updatingSectionData.getLayer(l);
		if (dataLayer == null) {
			return null;
		} else {
			if (this.changedSections.add(l)) {
				dataLayer = dataLayer.copy();
				this.updatingSectionData.setLayer(l, dataLayer);
				this.updatingSectionData.clearCache();
			}

			return dataLayer;
		}
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
		DataLayer dataLayer;
		if (this.changedSections.add(m)) {
			dataLayer = this.updatingSectionData.copyDataLayer(m);
		} else {
			dataLayer = this.getDataLayer(m, true);
		}

		dataLayer.set(SectionPos.sectionRelative(BlockPos.getX(l)), SectionPos.sectionRelative(BlockPos.getY(l)), SectionPos.sectionRelative(BlockPos.getZ(l)), i);
		SectionPos.aroundAndAtBlockPos(l, this.sectionsAffectedByLightUpdates::add);
	}

	protected void markSectionAndNeighborsAsAffected(long l) {
		int i = SectionPos.x(l);
		int j = SectionPos.y(l);
		int k = SectionPos.z(l);

		for (int m = -1; m <= 1; m++) {
			for (int n = -1; n <= 1; n++) {
				for (int o = -1; o <= 1; o++) {
					this.sectionsAffectedByLightUpdates.add(SectionPos.asLong(i + n, j + o, k + m));
				}
			}
		}
	}

	protected DataLayer createDataLayer(long l) {
		DataLayer dataLayer = this.queuedSections.get(l);
		return dataLayer != null ? dataLayer : new DataLayer();
	}

	protected boolean hasInconsistencies() {
		return this.hasInconsistencies;
	}

	protected void markNewInconsistencies(LightEngine<M, ?> lightEngine) {
		if (this.hasInconsistencies) {
			this.hasInconsistencies = false;
			LongIterator objectIterator = this.toRemove.iterator();

			while (objectIterator.hasNext()) {
				long l = (Long)objectIterator.next();
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
				this.changedSections.add(l);
			}

			this.toRemove.clear();
			ObjectIterator<Entry<DataLayer>> objectIteratorx = Long2ObjectMaps.fastIterator(this.queuedSections);

			while (objectIteratorx.hasNext()) {
				Entry<DataLayer> entry = (Entry<DataLayer>)objectIteratorx.next();
				long m = entry.getLongKey();
				if (this.storingLightForSection(m)) {
					DataLayer dataLayer2 = (DataLayer)entry.getValue();
					if (this.updatingSectionData.getLayer(m) != dataLayer2) {
						this.updatingSectionData.setLayer(m, dataLayer2);
						this.changedSections.add(m);
					}

					objectIteratorx.remove();
				}
			}

			this.updatingSectionData.clearCache();
		}
	}

	protected void onNodeAdded(long l) {
	}

	protected void onNodeRemoved(long l) {
	}

	protected void setLightEnabled(long l, boolean bl) {
		if (bl) {
			this.columnsWithSources.add(l);
		} else {
			this.columnsWithSources.remove(l);
		}
	}

	protected boolean lightOnInSection(long l) {
		long m = SectionPos.getZeroNode(l);
		return this.columnsWithSources.contains(m);
	}

	public void retainData(long l, boolean bl) {
		if (bl) {
			this.columnsToRetainQueuedDataFor.add(l);
		} else {
			this.columnsToRetainQueuedDataFor.remove(l);
		}
	}

	protected void queueSectionData(long l, @Nullable DataLayer dataLayer) {
		if (dataLayer != null) {
			this.queuedSections.put(l, dataLayer);
			this.hasInconsistencies = true;
		} else {
			this.queuedSections.remove(l);
		}
	}

	protected void updateSectionStatus(long l, boolean bl) {
		byte b = this.sectionStates.get(l);
		byte c = LayerLightSectionStorage.SectionState.hasData(b, !bl);
		if (b != c) {
			this.putSectionState(l, c);
			int i = bl ? -1 : 1;

			for (int j = -1; j <= 1; j++) {
				for (int k = -1; k <= 1; k++) {
					for (int m = -1; m <= 1; m++) {
						if (j != 0 || k != 0 || m != 0) {
							long n = SectionPos.offset(l, j, k, m);
							byte d = this.sectionStates.get(n);
							this.putSectionState(n, LayerLightSectionStorage.SectionState.neighborCount(d, LayerLightSectionStorage.SectionState.neighborCount(d) + i));
						}
					}
				}
			}
		}
	}

	protected void putSectionState(long l, byte b) {
		if (b != 0) {
			if (this.sectionStates.put(l, b) == 0) {
				this.initializeSection(l);
			}
		} else if (this.sectionStates.remove(l) != 0) {
			this.removeSection(l);
		}
	}

	private void initializeSection(long l) {
		if (!this.toRemove.remove(l)) {
			this.updatingSectionData.setLayer(l, this.createDataLayer(l));
			this.changedSections.add(l);
			this.onNodeAdded(l);
			this.markSectionAndNeighborsAsAffected(l);
			this.hasInconsistencies = true;
		}
	}

	private void removeSection(long l) {
		this.toRemove.add(l);
		this.hasInconsistencies = true;
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

	public LayerLightSectionStorage.SectionType getDebugSectionType(long l) {
		return LayerLightSectionStorage.SectionState.type(this.sectionStates.get(l));
	}

	protected static class SectionState {
		public static final byte EMPTY = 0;
		private static final int MIN_NEIGHBORS = 0;
		private static final int MAX_NEIGHBORS = 26;
		private static final byte HAS_DATA_BIT = 32;
		private static final byte NEIGHBOR_COUNT_BITS = 31;

		public static byte hasData(byte b, boolean bl) {
			return (byte)(bl ? b | 32 : b & -33);
		}

		public static byte neighborCount(byte b, int i) {
			if (i >= 0 && i <= 26) {
				return (byte)(b & -32 | i & 31);
			} else {
				throw new IllegalArgumentException("Neighbor count was not within range [0; 26]");
			}
		}

		public static boolean hasData(byte b) {
			return (b & 32) != 0;
		}

		public static int neighborCount(byte b) {
			return b & 31;
		}

		public static LayerLightSectionStorage.SectionType type(byte b) {
			if (b == 0) {
				return LayerLightSectionStorage.SectionType.EMPTY;
			} else {
				return hasData(b) ? LayerLightSectionStorage.SectionType.LIGHT_AND_DATA : LayerLightSectionStorage.SectionType.LIGHT_ONLY;
			}
		}
	}

	public static enum SectionType {
		EMPTY("2"),
		LIGHT_ONLY("1"),
		LIGHT_AND_DATA("0");

		private final String display;

		private SectionType(final String string2) {
			this.display = string2;
		}

		public String display() {
			return this.display;
		}
	}
}
