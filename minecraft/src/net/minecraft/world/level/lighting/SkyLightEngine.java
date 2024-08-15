package net.minecraft.world.level.lighting;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import org.jetbrains.annotations.VisibleForTesting;

public final class SkyLightEngine extends LightEngine<SkyLightSectionStorage.SkyDataLayerStorageMap, SkyLightSectionStorage> {
	private static final long REMOVE_TOP_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.decreaseAllDirections(15);
	private static final long REMOVE_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.decreaseSkipOneDirection(15, Direction.UP);
	private static final long ADD_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.increaseSkipOneDirection(15, false, Direction.UP);
	private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
	private final ChunkSkyLightSources emptyChunkSources;

	public SkyLightEngine(LightChunkGetter lightChunkGetter) {
		this(lightChunkGetter, new SkyLightSectionStorage(lightChunkGetter));
	}

	@VisibleForTesting
	protected SkyLightEngine(LightChunkGetter lightChunkGetter, SkyLightSectionStorage skyLightSectionStorage) {
		super(lightChunkGetter, skyLightSectionStorage);
		this.emptyChunkSources = new ChunkSkyLightSources(lightChunkGetter.getLevel());
	}

	private static boolean isSourceLevel(int i) {
		return i == 15;
	}

	private int getLowestSourceY(int i, int j, int k) {
		ChunkSkyLightSources chunkSkyLightSources = this.getChunkSources(SectionPos.blockToSectionCoord(i), SectionPos.blockToSectionCoord(j));
		return chunkSkyLightSources == null ? k : chunkSkyLightSources.getLowestSourceY(SectionPos.sectionRelative(i), SectionPos.sectionRelative(j));
	}

	@Nullable
	private ChunkSkyLightSources getChunkSources(int i, int j) {
		LightChunk lightChunk = this.chunkSource.getChunkForLighting(i, j);
		return lightChunk != null ? lightChunk.getSkyLightSources() : null;
	}

	@Override
	protected void checkNode(long l) {
		int i = BlockPos.getX(l);
		int j = BlockPos.getY(l);
		int k = BlockPos.getZ(l);
		long m = SectionPos.blockToSection(l);
		int n = this.storage.lightOnInSection(m) ? this.getLowestSourceY(i, k, Integer.MAX_VALUE) : Integer.MAX_VALUE;
		if (n != Integer.MAX_VALUE) {
			this.updateSourcesInColumn(i, k, n);
		}

		if (this.storage.storingLightForSection(m)) {
			boolean bl = j >= n;
			if (bl) {
				this.enqueueDecrease(l, REMOVE_SKY_SOURCE_ENTRY);
				this.enqueueIncrease(l, ADD_SKY_SOURCE_ENTRY);
			} else {
				int o = this.storage.getStoredLevel(l);
				if (o > 0) {
					this.storage.setStoredLevel(l, 0);
					this.enqueueDecrease(l, LightEngine.QueueEntry.decreaseAllDirections(o));
				} else {
					this.enqueueDecrease(l, PULL_LIGHT_IN_ENTRY);
				}
			}
		}
	}

	private void updateSourcesInColumn(int i, int j, int k) {
		int l = SectionPos.sectionToBlockCoord(this.storage.getBottomSectionY());
		this.removeSourcesBelow(i, j, k, l);
		this.addSourcesAbove(i, j, k, l);
	}

	private void removeSourcesBelow(int i, int j, int k, int l) {
		if (k > l) {
			int m = SectionPos.blockToSectionCoord(i);
			int n = SectionPos.blockToSectionCoord(j);
			int o = k - 1;

			for (int p = SectionPos.blockToSectionCoord(o); this.storage.hasLightDataAtOrBelow(p); p--) {
				if (this.storage.storingLightForSection(SectionPos.asLong(m, p, n))) {
					int q = SectionPos.sectionToBlockCoord(p);
					int r = q + 15;

					for (int s = Math.min(r, o); s >= q; s--) {
						long t = BlockPos.asLong(i, s, j);
						if (!isSourceLevel(this.storage.getStoredLevel(t))) {
							return;
						}

						this.storage.setStoredLevel(t, 0);
						this.enqueueDecrease(t, s == k - 1 ? REMOVE_TOP_SKY_SOURCE_ENTRY : REMOVE_SKY_SOURCE_ENTRY);
					}
				}
			}
		}
	}

	private void addSourcesAbove(int i, int j, int k, int l) {
		int m = SectionPos.blockToSectionCoord(i);
		int n = SectionPos.blockToSectionCoord(j);
		int o = Math.max(
			Math.max(this.getLowestSourceY(i - 1, j, Integer.MIN_VALUE), this.getLowestSourceY(i + 1, j, Integer.MIN_VALUE)),
			Math.max(this.getLowestSourceY(i, j - 1, Integer.MIN_VALUE), this.getLowestSourceY(i, j + 1, Integer.MIN_VALUE))
		);
		int p = Math.max(k, l);

		for (long q = SectionPos.asLong(m, SectionPos.blockToSectionCoord(p), n); !this.storage.isAboveData(q); q = SectionPos.offset(q, Direction.UP)) {
			if (this.storage.storingLightForSection(q)) {
				int r = SectionPos.sectionToBlockCoord(SectionPos.y(q));
				int s = r + 15;

				for (int t = Math.max(r, p); t <= s; t++) {
					long u = BlockPos.asLong(i, t, j);
					if (isSourceLevel(this.storage.getStoredLevel(u))) {
						return;
					}

					this.storage.setStoredLevel(u, 15);
					if (t < o || t == k) {
						this.enqueueIncrease(u, ADD_SKY_SOURCE_ENTRY);
					}
				}
			}
		}
	}

	@Override
	protected void propagateIncrease(long l, long m, int i) {
		BlockState blockState = null;
		int j = this.countEmptySectionsBelowIfAtBorder(l);

		for (Direction direction : PROPAGATION_DIRECTIONS) {
			if (LightEngine.QueueEntry.shouldPropagateInDirection(m, direction)) {
				long n = BlockPos.offset(l, direction);
				if (this.storage.storingLightForSection(SectionPos.blockToSection(n))) {
					int k = this.storage.getStoredLevel(n);
					int o = i - 1;
					if (o > k) {
						this.mutablePos.set(n);
						BlockState blockState2 = this.getState(this.mutablePos);
						int p = i - this.getOpacity(blockState2);
						if (p > k) {
							if (blockState == null) {
								blockState = LightEngine.QueueEntry.isFromEmptyShape(m) ? Blocks.AIR.defaultBlockState() : this.getState(this.mutablePos.set(l));
							}

							if (!this.shapeOccludes(blockState, blockState2, direction)) {
								this.storage.setStoredLevel(n, p);
								if (p > 1) {
									this.enqueueIncrease(n, LightEngine.QueueEntry.increaseSkipOneDirection(p, isEmptyShape(blockState2), direction.getOpposite()));
								}

								this.propagateFromEmptySections(n, direction, p, true, j);
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected void propagateDecrease(long l, long m) {
		int i = this.countEmptySectionsBelowIfAtBorder(l);
		int j = LightEngine.QueueEntry.getFromLevel(m);

		for (Direction direction : PROPAGATION_DIRECTIONS) {
			if (LightEngine.QueueEntry.shouldPropagateInDirection(m, direction)) {
				long n = BlockPos.offset(l, direction);
				if (this.storage.storingLightForSection(SectionPos.blockToSection(n))) {
					int k = this.storage.getStoredLevel(n);
					if (k != 0) {
						if (k <= j - 1) {
							this.storage.setStoredLevel(n, 0);
							this.enqueueDecrease(n, LightEngine.QueueEntry.decreaseSkipOneDirection(k, direction.getOpposite()));
							this.propagateFromEmptySections(n, direction, k, false, i);
						} else {
							this.enqueueIncrease(n, LightEngine.QueueEntry.increaseOnlyOneDirection(k, false, direction.getOpposite()));
						}
					}
				}
			}
		}
	}

	private int countEmptySectionsBelowIfAtBorder(long l) {
		int i = BlockPos.getY(l);
		int j = SectionPos.sectionRelative(i);
		if (j != 0) {
			return 0;
		} else {
			int k = BlockPos.getX(l);
			int m = BlockPos.getZ(l);
			int n = SectionPos.sectionRelative(k);
			int o = SectionPos.sectionRelative(m);
			if (n != 0 && n != 15 && o != 0 && o != 15) {
				return 0;
			} else {
				int p = SectionPos.blockToSectionCoord(k);
				int q = SectionPos.blockToSectionCoord(i);
				int r = SectionPos.blockToSectionCoord(m);
				int s = 0;

				while (!this.storage.storingLightForSection(SectionPos.asLong(p, q - s - 1, r)) && this.storage.hasLightDataAtOrBelow(q - s - 1)) {
					s++;
				}

				return s;
			}
		}
	}

	private void propagateFromEmptySections(long l, Direction direction, int i, boolean bl, int j) {
		if (j != 0) {
			int k = BlockPos.getX(l);
			int m = BlockPos.getZ(l);
			if (crossedSectionEdge(direction, SectionPos.sectionRelative(k), SectionPos.sectionRelative(m))) {
				int n = BlockPos.getY(l);
				int o = SectionPos.blockToSectionCoord(k);
				int p = SectionPos.blockToSectionCoord(m);
				int q = SectionPos.blockToSectionCoord(n) - 1;
				int r = q - j + 1;

				while (q >= r) {
					if (!this.storage.storingLightForSection(SectionPos.asLong(o, q, p))) {
						q--;
					} else {
						int s = SectionPos.sectionToBlockCoord(q);

						for (int t = 15; t >= 0; t--) {
							long u = BlockPos.asLong(k, s + t, m);
							if (bl) {
								this.storage.setStoredLevel(u, i);
								if (i > 1) {
									this.enqueueIncrease(u, LightEngine.QueueEntry.increaseSkipOneDirection(i, true, direction.getOpposite()));
								}
							} else {
								this.storage.setStoredLevel(u, 0);
								this.enqueueDecrease(u, LightEngine.QueueEntry.decreaseSkipOneDirection(i, direction.getOpposite()));
							}
						}

						q--;
					}
				}
			}
		}
	}

	private static boolean crossedSectionEdge(Direction direction, int i, int j) {
		return switch (direction) {
			case NORTH -> j == 15;
			case SOUTH -> j == 0;
			case WEST -> i == 15;
			case EAST -> i == 0;
			default -> false;
		};
	}

	@Override
	public void setLightEnabled(ChunkPos chunkPos, boolean bl) {
		super.setLightEnabled(chunkPos, bl);
		if (bl) {
			ChunkSkyLightSources chunkSkyLightSources = (ChunkSkyLightSources)Objects.requireNonNullElse(
				this.getChunkSources(chunkPos.x, chunkPos.z), this.emptyChunkSources
			);
			int i = chunkSkyLightSources.getHighestLowestSourceY() - 1;
			int j = SectionPos.blockToSectionCoord(i) + 1;
			long l = SectionPos.getZeroNode(chunkPos.x, chunkPos.z);
			int k = this.storage.getTopSectionY(l);
			int m = Math.max(this.storage.getBottomSectionY(), j);

			for (int n = k - 1; n >= m; n--) {
				DataLayer dataLayer = this.storage.getDataLayerToWrite(SectionPos.asLong(chunkPos.x, n, chunkPos.z));
				if (dataLayer != null && dataLayer.isEmpty()) {
					dataLayer.fill(15);
				}
			}
		}
	}

	@Override
	public void propagateLightSources(ChunkPos chunkPos) {
		long l = SectionPos.getZeroNode(chunkPos.x, chunkPos.z);
		this.storage.setLightEnabled(l, true);
		ChunkSkyLightSources chunkSkyLightSources = (ChunkSkyLightSources)Objects.requireNonNullElse(
			this.getChunkSources(chunkPos.x, chunkPos.z), this.emptyChunkSources
		);
		ChunkSkyLightSources chunkSkyLightSources2 = (ChunkSkyLightSources)Objects.requireNonNullElse(
			this.getChunkSources(chunkPos.x, chunkPos.z - 1), this.emptyChunkSources
		);
		ChunkSkyLightSources chunkSkyLightSources3 = (ChunkSkyLightSources)Objects.requireNonNullElse(
			this.getChunkSources(chunkPos.x, chunkPos.z + 1), this.emptyChunkSources
		);
		ChunkSkyLightSources chunkSkyLightSources4 = (ChunkSkyLightSources)Objects.requireNonNullElse(
			this.getChunkSources(chunkPos.x - 1, chunkPos.z), this.emptyChunkSources
		);
		ChunkSkyLightSources chunkSkyLightSources5 = (ChunkSkyLightSources)Objects.requireNonNullElse(
			this.getChunkSources(chunkPos.x + 1, chunkPos.z), this.emptyChunkSources
		);
		int i = this.storage.getTopSectionY(l);
		int j = this.storage.getBottomSectionY();
		int k = SectionPos.sectionToBlockCoord(chunkPos.x);
		int m = SectionPos.sectionToBlockCoord(chunkPos.z);

		for (int n = i - 1; n >= j; n--) {
			long o = SectionPos.asLong(chunkPos.x, n, chunkPos.z);
			DataLayer dataLayer = this.storage.getDataLayerToWrite(o);
			if (dataLayer != null) {
				int p = SectionPos.sectionToBlockCoord(n);
				int q = p + 15;
				boolean bl = false;

				for (int r = 0; r < 16; r++) {
					for (int s = 0; s < 16; s++) {
						int t = chunkSkyLightSources.getLowestSourceY(s, r);
						if (t <= q) {
							int u = r == 0 ? chunkSkyLightSources2.getLowestSourceY(s, 15) : chunkSkyLightSources.getLowestSourceY(s, r - 1);
							int v = r == 15 ? chunkSkyLightSources3.getLowestSourceY(s, 0) : chunkSkyLightSources.getLowestSourceY(s, r + 1);
							int w = s == 0 ? chunkSkyLightSources4.getLowestSourceY(15, r) : chunkSkyLightSources.getLowestSourceY(s - 1, r);
							int x = s == 15 ? chunkSkyLightSources5.getLowestSourceY(0, r) : chunkSkyLightSources.getLowestSourceY(s + 1, r);
							int y = Math.max(Math.max(u, v), Math.max(w, x));

							for (int z = q; z >= Math.max(p, t); z--) {
								dataLayer.set(s, SectionPos.sectionRelative(z), r, 15);
								if (z == t || z < y) {
									long aa = BlockPos.asLong(k + s, z, m + r);
									this.enqueueIncrease(aa, LightEngine.QueueEntry.increaseSkySourceInDirections(z == t, z < u, z < v, z < w, z < x));
								}
							}

							if (t < p) {
								bl = true;
							}
						}
					}
				}

				if (!bl) {
					break;
				}
			}
		}
	}
}
