package net.minecraft.world.level.lighting;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;

public final class BlockLightEngine extends LightEngine<BlockLightSectionStorage.BlockDataLayerStorageMap, BlockLightSectionStorage> {
	private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

	public BlockLightEngine(LightChunkGetter lightChunkGetter) {
		this(lightChunkGetter, new BlockLightSectionStorage(lightChunkGetter));
	}

	@VisibleForTesting
	public BlockLightEngine(LightChunkGetter lightChunkGetter, BlockLightSectionStorage blockLightSectionStorage) {
		super(lightChunkGetter, blockLightSectionStorage);
	}

	@Override
	protected void checkNode(long l) {
		long m = SectionPos.blockToSection(l);
		if (this.storage.storingLightForSection(m)) {
			BlockState blockState = this.getState(this.mutablePos.set(l));
			int i = this.getEmission(l, blockState);
			int j = this.storage.getStoredLevel(l);
			if (i < j) {
				this.storage.setStoredLevel(l, 0);
				this.enqueueDecrease(l, LightEngine.QueueEntry.decreaseAllDirections(j));
			} else {
				this.enqueueDecrease(l, PULL_LIGHT_IN_ENTRY);
			}

			if (i > 0) {
				this.enqueueIncrease(l, LightEngine.QueueEntry.increaseLightFromEmission(i, isEmptyShape(blockState)));
			}
		}
	}

	@Override
	protected void propagateIncrease(long l, long m, int i) {
		BlockState blockState = null;

		for (Direction direction : PROPAGATION_DIRECTIONS) {
			if (LightEngine.QueueEntry.shouldPropagateInDirection(m, direction)) {
				long n = BlockPos.offset(l, direction);
				if (this.storage.storingLightForSection(SectionPos.blockToSection(n))) {
					int j = this.storage.getStoredLevel(n);
					int k = i - 1;
					if (k > j) {
						this.mutablePos.set(n);
						BlockState blockState2 = this.getState(this.mutablePos);
						int o = i - this.getOpacity(blockState2);
						if (o > j) {
							if (blockState == null) {
								blockState = LightEngine.QueueEntry.isFromEmptyShape(m) ? Blocks.AIR.defaultBlockState() : this.getState(this.mutablePos.set(l));
							}

							if (!this.shapeOccludes(blockState, blockState2, direction)) {
								this.storage.setStoredLevel(n, o);
								if (o > 1) {
									this.enqueueIncrease(n, LightEngine.QueueEntry.increaseSkipOneDirection(o, isEmptyShape(blockState2), direction.getOpposite()));
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected void propagateDecrease(long l, long m) {
		int i = LightEngine.QueueEntry.getFromLevel(m);

		for (Direction direction : PROPAGATION_DIRECTIONS) {
			if (LightEngine.QueueEntry.shouldPropagateInDirection(m, direction)) {
				long n = BlockPos.offset(l, direction);
				if (this.storage.storingLightForSection(SectionPos.blockToSection(n))) {
					int j = this.storage.getStoredLevel(n);
					if (j != 0) {
						if (j <= i - 1) {
							BlockState blockState = this.getState(this.mutablePos.set(n));
							int k = this.getEmission(n, blockState);
							this.storage.setStoredLevel(n, 0);
							if (k < j) {
								this.enqueueDecrease(n, LightEngine.QueueEntry.decreaseSkipOneDirection(j, direction.getOpposite()));
							}

							if (k > 0) {
								this.enqueueIncrease(n, LightEngine.QueueEntry.increaseLightFromEmission(k, isEmptyShape(blockState)));
							}
						} else {
							this.enqueueIncrease(n, LightEngine.QueueEntry.increaseOnlyOneDirection(j, false, direction.getOpposite()));
						}
					}
				}
			}
		}
	}

	private int getEmission(long l, BlockState blockState) {
		int i = blockState.getLightEmission();
		return i > 0 && this.storage.lightOnInSection(SectionPos.blockToSection(l)) ? i : 0;
	}

	@Override
	public void propagateLightSources(ChunkPos chunkPos) {
		this.setLightEnabled(chunkPos, true);
		LightChunk lightChunk = this.chunkSource.getChunkForLighting(chunkPos.x, chunkPos.z);
		if (lightChunk != null) {
			lightChunk.findBlockLightSources((blockPos, blockState) -> {
				int i = blockState.getLightEmission();
				this.enqueueIncrease(blockPos.asLong(), LightEngine.QueueEntry.increaseLightFromEmission(i, isEmptyShape(blockState)));
			});
		}
	}
}
