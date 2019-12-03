package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public class SkyLightSectionStorage extends LayerLightSectionStorage<SkyLightSectionStorage.SkyDataLayerStorageMap> {
	private static final Direction[] HORIZONTALS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
	private final LongSet sectionsWithSources = new LongOpenHashSet();
	private final LongSet sectionsToAddSourcesTo = new LongOpenHashSet();
	private final LongSet sectionsToRemoveSourcesFrom = new LongOpenHashSet();
	private final LongSet columnsWithSkySources = new LongOpenHashSet();
	private volatile boolean hasSourceInconsistencies;

	protected SkyLightSectionStorage(LightChunkGetter lightChunkGetter) {
		super(
			LightLayer.SKY,
			lightChunkGetter,
			new SkyLightSectionStorage.SkyDataLayerStorageMap(new Long2ObjectOpenHashMap<>(), new Long2IntOpenHashMap(), Integer.MAX_VALUE)
		);
	}

	@Override
	protected int getLightValue(long l) {
		long m = SectionPos.blockToSection(l);
		int i = SectionPos.y(m);
		SkyLightSectionStorage.SkyDataLayerStorageMap skyDataLayerStorageMap = this.visibleSectionData;
		int j = skyDataLayerStorageMap.topSections.get(SectionPos.getZeroNode(m));
		if (j != skyDataLayerStorageMap.currentLowestY && i < j) {
			DataLayer dataLayer = this.getDataLayer(skyDataLayerStorageMap, m);
			if (dataLayer == null) {
				for (l = BlockPos.getFlatIndex(l); dataLayer == null; dataLayer = this.getDataLayer(skyDataLayerStorageMap, m)) {
					m = SectionPos.offset(m, Direction.UP);
					if (++i >= j) {
						return 15;
					}

					l = BlockPos.offset(l, 0, 16, 0);
				}
			}

			return dataLayer.get(
				SectionPos.sectionRelative(BlockPos.getX(l)), SectionPos.sectionRelative(BlockPos.getY(l)), SectionPos.sectionRelative(BlockPos.getZ(l))
			);
		} else {
			return 15;
		}
	}

	@Override
	protected void onNodeAdded(long l) {
		int i = SectionPos.y(l);
		if (this.updatingSectionData.currentLowestY > i) {
			this.updatingSectionData.currentLowestY = i;
			this.updatingSectionData.topSections.defaultReturnValue(this.updatingSectionData.currentLowestY);
		}

		long m = SectionPos.getZeroNode(l);
		int j = this.updatingSectionData.topSections.get(m);
		if (j < i + 1) {
			this.updatingSectionData.topSections.put(m, i + 1);
			if (this.columnsWithSkySources.contains(m)) {
				this.queueAddSource(l);
				if (j > this.updatingSectionData.currentLowestY) {
					long n = SectionPos.asLong(SectionPos.x(l), j - 1, SectionPos.z(l));
					this.queueRemoveSource(n);
				}

				this.recheckInconsistencyFlag();
			}
		}
	}

	private void queueRemoveSource(long l) {
		this.sectionsToRemoveSourcesFrom.add(l);
		this.sectionsToAddSourcesTo.remove(l);
	}

	private void queueAddSource(long l) {
		this.sectionsToAddSourcesTo.add(l);
		this.sectionsToRemoveSourcesFrom.remove(l);
	}

	private void recheckInconsistencyFlag() {
		this.hasSourceInconsistencies = !this.sectionsToAddSourcesTo.isEmpty() || !this.sectionsToRemoveSourcesFrom.isEmpty();
	}

	@Override
	protected void onNodeRemoved(long l) {
		long m = SectionPos.getZeroNode(l);
		boolean bl = this.columnsWithSkySources.contains(m);
		if (bl) {
			this.queueRemoveSource(l);
		}

		int i = SectionPos.y(l);
		if (this.updatingSectionData.topSections.get(m) == i + 1) {
			long n;
			for (n = l; !this.storingLightForSection(n) && this.hasSectionsBelow(i); n = SectionPos.offset(n, Direction.DOWN)) {
				i--;
			}

			if (this.storingLightForSection(n)) {
				this.updatingSectionData.topSections.put(m, i + 1);
				if (bl) {
					this.queueAddSource(n);
				}
			} else {
				this.updatingSectionData.topSections.remove(m);
			}
		}

		if (bl) {
			this.recheckInconsistencyFlag();
		}
	}

	@Override
	protected void enableLightSources(long l, boolean bl) {
		this.runAllUpdates();
		if (bl && this.columnsWithSkySources.add(l)) {
			int i = this.updatingSectionData.topSections.get(l);
			if (i != this.updatingSectionData.currentLowestY) {
				long m = SectionPos.asLong(SectionPos.x(l), i - 1, SectionPos.z(l));
				this.queueAddSource(m);
				this.recheckInconsistencyFlag();
			}
		} else if (!bl) {
			this.columnsWithSkySources.remove(l);
		}
	}

	@Override
	protected boolean hasInconsistencies() {
		return super.hasInconsistencies() || this.hasSourceInconsistencies;
	}

	@Override
	protected DataLayer createDataLayer(long l) {
		DataLayer dataLayer = this.queuedSections.get(l);
		if (dataLayer != null) {
			return dataLayer;
		} else {
			long m = SectionPos.offset(l, Direction.UP);
			int i = this.updatingSectionData.topSections.get(SectionPos.getZeroNode(l));
			if (i != this.updatingSectionData.currentLowestY && SectionPos.y(m) < i) {
				DataLayer dataLayer2;
				while ((dataLayer2 = this.getDataLayer(m, true)) == null) {
					m = SectionPos.offset(m, Direction.UP);
				}

				return new DataLayer(new FlatDataLayer(dataLayer2, 0).getData());
			} else {
				return new DataLayer();
			}
		}
	}

	@Override
	protected void markNewInconsistencies(LayerLightEngine<SkyLightSectionStorage.SkyDataLayerStorageMap, ?> layerLightEngine, boolean bl, boolean bl2) {
		super.markNewInconsistencies(layerLightEngine, bl, bl2);
		if (bl) {
			if (!this.sectionsToAddSourcesTo.isEmpty()) {
				LongIterator var4 = this.sectionsToAddSourcesTo.iterator();

				while (var4.hasNext()) {
					long l = (Long)var4.next();
					int i = this.getLevel(l);
					if (i != 2 && !this.sectionsToRemoveSourcesFrom.contains(l) && this.sectionsWithSources.add(l)) {
						if (i == 1) {
							this.clearQueuedSectionBlocks(layerLightEngine, l);
							if (this.changedSections.add(l)) {
								this.updatingSectionData.copyDataLayer(l);
							}

							Arrays.fill(this.getDataLayer(l, true).getData(), (byte)-1);
							int j = SectionPos.sectionToBlockCoord(SectionPos.x(l));
							int k = SectionPos.sectionToBlockCoord(SectionPos.y(l));
							int m = SectionPos.sectionToBlockCoord(SectionPos.z(l));

							for (Direction direction : HORIZONTALS) {
								long n = SectionPos.offset(l, direction);
								if ((this.sectionsToRemoveSourcesFrom.contains(n) || !this.sectionsWithSources.contains(n) && !this.sectionsToAddSourcesTo.contains(n))
									&& this.storingLightForSection(n)) {
									for (int o = 0; o < 16; o++) {
										for (int p = 0; p < 16; p++) {
											long q;
											long r;
											switch (direction) {
												case NORTH:
													q = BlockPos.asLong(j + o, k + p, m);
													r = BlockPos.asLong(j + o, k + p, m - 1);
													break;
												case SOUTH:
													q = BlockPos.asLong(j + o, k + p, m + 16 - 1);
													r = BlockPos.asLong(j + o, k + p, m + 16);
													break;
												case WEST:
													q = BlockPos.asLong(j, k + o, m + p);
													r = BlockPos.asLong(j - 1, k + o, m + p);
													break;
												default:
													q = BlockPos.asLong(j + 16 - 1, k + o, m + p);
													r = BlockPos.asLong(j + 16, k + o, m + p);
											}

											layerLightEngine.checkEdge(q, r, layerLightEngine.computeLevelFromNeighbor(q, r, 0), true);
										}
									}
								}
							}

							for (int s = 0; s < 16; s++) {
								for (int t = 0; t < 16; t++) {
									long u = BlockPos.asLong(
										SectionPos.sectionToBlockCoord(SectionPos.x(l)) + s,
										SectionPos.sectionToBlockCoord(SectionPos.y(l)),
										SectionPos.sectionToBlockCoord(SectionPos.z(l)) + t
									);
									long n = BlockPos.asLong(
										SectionPos.sectionToBlockCoord(SectionPos.x(l)) + s,
										SectionPos.sectionToBlockCoord(SectionPos.y(l)) - 1,
										SectionPos.sectionToBlockCoord(SectionPos.z(l)) + t
									);
									layerLightEngine.checkEdge(u, n, layerLightEngine.computeLevelFromNeighbor(u, n, 0), true);
								}
							}
						} else {
							for (int j = 0; j < 16; j++) {
								for (int k = 0; k < 16; k++) {
									long v = BlockPos.asLong(
										SectionPos.sectionToBlockCoord(SectionPos.x(l)) + j,
										SectionPos.sectionToBlockCoord(SectionPos.y(l)) + 16 - 1,
										SectionPos.sectionToBlockCoord(SectionPos.z(l)) + k
									);
									layerLightEngine.checkEdge(Long.MAX_VALUE, v, 0, true);
								}
							}
						}
					}
				}
			}

			this.sectionsToAddSourcesTo.clear();
			if (!this.sectionsToRemoveSourcesFrom.isEmpty()) {
				LongIterator var23 = this.sectionsToRemoveSourcesFrom.iterator();

				while (var23.hasNext()) {
					long l = (Long)var23.next();
					if (this.sectionsWithSources.remove(l) && this.storingLightForSection(l)) {
						for (int i = 0; i < 16; i++) {
							for (int j = 0; j < 16; j++) {
								long w = BlockPos.asLong(
									SectionPos.sectionToBlockCoord(SectionPos.x(l)) + i,
									SectionPos.sectionToBlockCoord(SectionPos.y(l)) + 16 - 1,
									SectionPos.sectionToBlockCoord(SectionPos.z(l)) + j
								);
								layerLightEngine.checkEdge(Long.MAX_VALUE, w, 15, false);
							}
						}
					}
				}
			}

			this.sectionsToRemoveSourcesFrom.clear();
			this.hasSourceInconsistencies = false;
		}
	}

	protected boolean hasSectionsBelow(int i) {
		return i >= this.updatingSectionData.currentLowestY;
	}

	protected boolean hasLightSource(long l) {
		int i = BlockPos.getY(l);
		if ((i & 15) != 15) {
			return false;
		} else {
			long m = SectionPos.blockToSection(l);
			long n = SectionPos.getZeroNode(m);
			if (!this.columnsWithSkySources.contains(n)) {
				return false;
			} else {
				int j = this.updatingSectionData.topSections.get(n);
				return SectionPos.sectionToBlockCoord(j) == i + 16;
			}
		}
	}

	protected boolean isAboveData(long l) {
		long m = SectionPos.getZeroNode(l);
		int i = this.updatingSectionData.topSections.get(m);
		return i == this.updatingSectionData.currentLowestY || SectionPos.y(l) >= i;
	}

	protected boolean lightOnInSection(long l) {
		long m = SectionPos.getZeroNode(l);
		return this.columnsWithSkySources.contains(m);
	}

	public static final class SkyDataLayerStorageMap extends DataLayerStorageMap<SkyLightSectionStorage.SkyDataLayerStorageMap> {
		private int currentLowestY;
		private final Long2IntOpenHashMap topSections;

		public SkyDataLayerStorageMap(Long2ObjectOpenHashMap<DataLayer> long2ObjectOpenHashMap, Long2IntOpenHashMap long2IntOpenHashMap, int i) {
			super(long2ObjectOpenHashMap);
			this.topSections = long2IntOpenHashMap;
			long2IntOpenHashMap.defaultReturnValue(i);
			this.currentLowestY = i;
		}

		public SkyLightSectionStorage.SkyDataLayerStorageMap copy() {
			return new SkyLightSectionStorage.SkyDataLayerStorageMap(this.map.clone(), this.topSections.clone(), this.currentLowestY);
		}
	}
}
