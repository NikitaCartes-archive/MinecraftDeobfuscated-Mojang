package net.minecraft.world.level.block.state.pattern;

import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelReader;

public class BlockPattern {
	private final Predicate<BlockInWorld>[][][] pattern;
	private final int depth;
	private final int height;
	private final int width;

	public BlockPattern(Predicate<BlockInWorld>[][][] predicates) {
		this.pattern = predicates;
		this.depth = predicates.length;
		if (this.depth > 0) {
			this.height = predicates[0].length;
			if (this.height > 0) {
				this.width = predicates[0][0].length;
			} else {
				this.width = 0;
			}
		} else {
			this.height = 0;
			this.width = 0;
		}
	}

	public int getDepth() {
		return this.depth;
	}

	public int getHeight() {
		return this.height;
	}

	public int getWidth() {
		return this.width;
	}

	@Nullable
	private BlockPattern.BlockPatternMatch matches(BlockPos blockPos, Direction direction, Direction direction2, LoadingCache<BlockPos, BlockInWorld> loadingCache) {
		for (int i = 0; i < this.width; i++) {
			for (int j = 0; j < this.height; j++) {
				for (int k = 0; k < this.depth; k++) {
					if (!this.pattern[k][j][i].test(loadingCache.getUnchecked(translateAndRotate(blockPos, direction, direction2, i, j, k)))) {
						return null;
					}
				}
			}
		}

		return new BlockPattern.BlockPatternMatch(blockPos, direction, direction2, loadingCache, this.width, this.height, this.depth);
	}

	@Nullable
	public BlockPattern.BlockPatternMatch find(LevelReader levelReader, BlockPos blockPos) {
		LoadingCache<BlockPos, BlockInWorld> loadingCache = createLevelCache(levelReader, false);
		int i = Math.max(Math.max(this.width, this.height), this.depth);

		for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos, blockPos.offset(i - 1, i - 1, i - 1))) {
			for (Direction direction : Direction.values()) {
				for (Direction direction2 : Direction.values()) {
					if (direction2 != direction && direction2 != direction.getOpposite()) {
						BlockPattern.BlockPatternMatch blockPatternMatch = this.matches(blockPos2, direction, direction2, loadingCache);
						if (blockPatternMatch != null) {
							return blockPatternMatch;
						}
					}
				}
			}
		}

		return null;
	}

	public static LoadingCache<BlockPos, BlockInWorld> createLevelCache(LevelReader levelReader, boolean bl) {
		return CacheBuilder.newBuilder().build(new BlockPattern.BlockCacheLoader(levelReader, bl));
	}

	protected static BlockPos translateAndRotate(BlockPos blockPos, Direction direction, Direction direction2, int i, int j, int k) {
		if (direction != direction2 && direction != direction2.getOpposite()) {
			Vec3i vec3i = new Vec3i(direction.getStepX(), direction.getStepY(), direction.getStepZ());
			Vec3i vec3i2 = new Vec3i(direction2.getStepX(), direction2.getStepY(), direction2.getStepZ());
			Vec3i vec3i3 = vec3i.cross(vec3i2);
			return blockPos.offset(
				vec3i2.getX() * -j + vec3i3.getX() * i + vec3i.getX() * k,
				vec3i2.getY() * -j + vec3i3.getY() * i + vec3i.getY() * k,
				vec3i2.getZ() * -j + vec3i3.getZ() * i + vec3i.getZ() * k
			);
		} else {
			throw new IllegalArgumentException("Invalid forwards & up combination");
		}
	}

	static class BlockCacheLoader extends CacheLoader<BlockPos, BlockInWorld> {
		private final LevelReader level;
		private final boolean loadChunks;

		public BlockCacheLoader(LevelReader levelReader, boolean bl) {
			this.level = levelReader;
			this.loadChunks = bl;
		}

		public BlockInWorld load(BlockPos blockPos) {
			return new BlockInWorld(this.level, blockPos, this.loadChunks);
		}
	}

	public static class BlockPatternMatch {
		private final BlockPos frontTopLeft;
		private final Direction forwards;
		private final Direction up;
		private final LoadingCache<BlockPos, BlockInWorld> cache;
		private final int width;
		private final int height;
		private final int depth;

		public BlockPatternMatch(BlockPos blockPos, Direction direction, Direction direction2, LoadingCache<BlockPos, BlockInWorld> loadingCache, int i, int j, int k) {
			this.frontTopLeft = blockPos;
			this.forwards = direction;
			this.up = direction2;
			this.cache = loadingCache;
			this.width = i;
			this.height = j;
			this.depth = k;
		}

		public BlockPos getFrontTopLeft() {
			return this.frontTopLeft;
		}

		public Direction getForwards() {
			return this.forwards;
		}

		public Direction getUp() {
			return this.up;
		}

		public BlockInWorld getBlock(int i, int j, int k) {
			return this.cache.getUnchecked(BlockPattern.translateAndRotate(this.frontTopLeft, this.getForwards(), this.getUp(), i, j, k));
		}

		public String toString() {
			return MoreObjects.toStringHelper(this).add("up", this.up).add("forwards", this.forwards).add("frontTopLeft", this.frontTopLeft).toString();
		}
	}
}
