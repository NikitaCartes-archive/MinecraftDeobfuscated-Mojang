package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ParkLaneSurfaceFeature extends Feature<NoneFeatureConfiguration> {
	public ParkLaneSurfaceFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
		BlockPos blockPos = featurePlaceContext.origin();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		ChunkPos chunkPos = new ChunkPos(blockPos);
		blockPos = chunkPos.getBlockAt(7, 0, 7);
		blockPos = blockPos.atY(worldGenLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPos).getY()).below();
		if (blockPos.getY() >= 8 && worldGenLevel.getBiome(blockPos).is(Biomes.ARBORETUM)) {
			Direction[] directions = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
			boolean[] bls = new boolean[]{false, false, false, false};
			int i = 0;

			for (int j = 0; j < 4; j++) {
				if (this.connectsTo(worldGenLevel, chunkPos, blockPos, directions[j])) {
					i++;
					bls[j] = !this.isGeneratedTowardsORShouldNOTGenerateFromThisChunk(worldGenLevel, blockPos, directions[j]);
				}
			}

			if (i == 0) {
				return false;
			} else {
				int jx = i == 4 ? 1 : 0;
				int k = worldGenLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPos).getY();

				for (int l = -1 - jx; l <= 2 + jx; l++) {
					for (int m = -1 - jx; m <= 2 + jx; m++) {
						BlockPos blockPos2 = blockPos.offset(l, 0, m);
						BlockPos blockPos3 = worldGenLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPos2).below();
						if (!this.isUntouchedTerrain(worldGenLevel, blockPos3)) {
							return false;
						}
					}
				}

				for (int l = -1 - jx; l <= 2 + jx; l++) {
					for (int mx = -1 - jx; mx <= 2 + jx; mx++) {
						BlockPos blockPos2 = blockPos.offset(l, 0, mx);
						this.placePathOrPlanksIfFlying(worldGenLevel, blockPos2.atY(k - 1), null, false);
					}
				}

				for (int l = 0; l < 4; l++) {
					if (bls[l]) {
						this.generatePath(worldGenLevel, blockPos, directions[l], jx == 1);
					}
				}

				return true;
			}
		} else {
			return false;
		}
	}

	private boolean isUntouchedTerrain(WorldGenLevel worldGenLevel, BlockPos blockPos) {
		BlockState blockState = worldGenLevel.getBlockState(blockPos);
		return blockState.is(Blocks.PEELGRASS_BLOCK)
			|| blockState.is(Blocks.POISON_PATH)
			|| blockState.is(Blocks.POTATO_PLANKS)
			|| blockState.is(Blocks.LANTERN)
			|| blockState.is(Blocks.POTATO_FENCE);
	}

	private boolean connectsTo(WorldGenLevel worldGenLevel, ChunkPos chunkPos, BlockPos blockPos, Direction direction) {
		BlockPos blockPos2 = blockPos.relative(direction, 16);
		if (!worldGenLevel.getBiome(blockPos2).is(Biomes.ARBORETUM)) {
			return false;
		} else {
			int i = worldGenLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPos).getY();
			int j = worldGenLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPos2).getY();
			if (Math.abs(i - j) > 10) {
				return false;
			} else {
				long l = worldGenLevel.getSeed();
				ChunkPos chunkPos2 = chunkPos;
				blockPos.relative(Direction.NORTH);
				if (direction == Direction.NORTH || direction == Direction.EAST) {
					chunkPos2 = new ChunkPos(chunkPos.x + direction.getStepX(), chunkPos.z + direction.getStepZ());
				}

				l += (long)chunkPos2.hashCode();
				Random random = new Random(l);
				if (direction == Direction.NORTH || direction == Direction.SOUTH) {
					random.nextFloat();
				}

				boolean bl = random.nextFloat() < 0.7F;
				if (!bl) {
					return false;
				} else {
					for (int k = -1; k <= 2; k++) {
						for (int m = -2; m < 18; m++) {
							BlockPos blockPos3 = this.getPositionAtLane(worldGenLevel, blockPos, direction, k, m);
							if (!this.isUntouchedTerrain(worldGenLevel, blockPos3)) {
								return false;
							}
						}
					}

					return true;
				}
			}
		}
	}

	private boolean isGeneratedTowardsORShouldNOTGenerateFromThisChunk(WorldGenLevel worldGenLevel, BlockPos blockPos, Direction direction) {
		BlockPos blockPos2 = blockPos.relative(direction, 16);
		int i = worldGenLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPos).getY();
		int j = worldGenLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPos2).getY();
		BlockPos blockPos3 = blockPos.relative(direction, 4);
		blockPos3 = worldGenLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPos3).below();
		return !worldGenLevel.getBlockState(blockPos3).is(Blocks.POISON_PATH) && !worldGenLevel.getBlockState(blockPos3).is(Blocks.POTATO_PLANKS) ? j < i : true;
	}

	private void generatePath(WorldGenLevel worldGenLevel, BlockPos blockPos, Direction direction, boolean bl) {
		if (this.canConformToTerrain(worldGenLevel, blockPos, direction)) {
			for (int i = -1; i <= 2; i++) {
				Direction direction2 = null;
				if (i == -1 || i == 2) {
					direction2 = this.getFencingDirection(direction, i < 0);
				}

				boolean bl2 = false;

				for (int j = 2; j < 14; j++) {
					if (bl2 && (double)worldGenLevel.getRandom().nextFloat() < 0.3) {
						bl2 = false;
					}

					BlockPos blockPos2 = this.getPositionAtLane(worldGenLevel, blockPos, direction, i, j);
					bl2 = this.placePathOrPlanksIfFlying(worldGenLevel, blockPos2, (!bl || j != 2) && j != 13 ? direction2 : null, bl2);
				}
			}
		} else {
			int i = blockPos.getY();
			int k = worldGenLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPos.relative(direction, 16)).getY() - 1;

			for (int l = -1; l <= 2; l++) {
				Direction direction3 = null;
				if (l == -1 || l == 2) {
					direction3 = this.getFencingDirection(direction, l < 0);
				}

				boolean bl3 = false;

				for (int m = 2; m < 14; m++) {
					if (bl3 && (double)worldGenLevel.getRandom().nextFloat() < 0.3) {
						bl3 = false;
					}

					int n = Math.round(Mth.lerp(((float)m - 2.0F) / 12.0F, (float)i, (float)k));
					BlockPos blockPos3 = this.getPositionAtLane(worldGenLevel, blockPos, direction, l, m).atY(n);
					bl3 = this.placePathOrPlanksIfFlying(worldGenLevel, blockPos3, (!bl || m != 2) && m != 13 ? direction3 : null, bl3);
				}
			}
		}
	}

	@Nullable
	private Direction getFencingDirection(Direction direction, boolean bl) {
		if (direction.getAxis() == Direction.NORTH.getAxis()) {
			return bl ? Direction.WEST : Direction.EAST;
		} else if (direction.getAxis() == Direction.EAST.getAxis()) {
			return bl ? Direction.NORTH : Direction.SOUTH;
		} else {
			return null;
		}
	}

	private BlockPos getPositionAtLane(WorldGenLevel worldGenLevel, BlockPos blockPos, Direction direction, int i, int j) {
		Vec3i vec3i = direction.getNormal();
		BlockPos blockPos2 = blockPos.offset(
			(vec3i.getX() > 0 ? 1 : 0) + vec3i.getX() * j + Math.abs(vec3i.getZ()) * i, 0, (vec3i.getZ() > 0 ? 1 : 0) + vec3i.getZ() * j + Math.abs(vec3i.getX()) * i
		);
		return worldGenLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPos2).below();
	}

	private boolean canConformToTerrain(WorldGenLevel worldGenLevel, BlockPos blockPos, Direction direction) {
		int i = blockPos.getY();

		for (int j = -1; j <= 2; j++) {
			int k = i;

			for (int l = 2; l < 14; l++) {
				int m = this.getPositionAtLane(worldGenLevel, blockPos, direction, j, l).getY();
				if (Math.abs(k - m) > 1) {
					return false;
				}

				k = m;
			}
		}

		return true;
	}

	private boolean placePathOrPlanksIfFlying(WorldGenLevel worldGenLevel, BlockPos blockPos, @Nullable Direction direction, boolean bl) {
		int i = worldGenLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPos).getY() - 1;
		int j = Math.min(i, blockPos.getY() + 10);

		for (int k = blockPos.getY() + 1; k <= j; k++) {
			worldGenLevel.setBlock(blockPos.atY(k), Blocks.AIR.defaultBlockState(), 3);
		}

		boolean bl2 = worldGenLevel.getBlockState(blockPos.below()).isAir();
		worldGenLevel.setBlock(blockPos, (bl2 ? Blocks.POTATO_PLANKS : Blocks.POISON_PATH).defaultBlockState(), 3);
		if (direction != null) {
			BlockPos blockPos2 = blockPos.above();
			BlockPos blockPos3 = blockPos2.relative(direction);
			if (bl2 && worldGenLevel.getBlockState(blockPos3).isAir()) {
				worldGenLevel.setBlock(blockPos2, Blocks.POTATO_FENCE.defaultBlockState(), 3);
				worldGenLevel.getChunk(blockPos2).markPosForPostprocessing(blockPos2);
				return true;
			}

			if (worldGenLevel.getBlockState(blockPos3).isAir()
				&& this.isUntouchedTerrain(worldGenLevel, blockPos.relative(direction))
				&& (bl || worldGenLevel.getRandom().nextFloat() < 0.6F)) {
				worldGenLevel.setBlock(blockPos3, Blocks.POTATO_FENCE.defaultBlockState(), 3);
				worldGenLevel.getChunk(blockPos3).markPosForPostprocessing(blockPos3);
				if (worldGenLevel.getRandom().nextFloat() < 0.1F) {
					worldGenLevel.setBlock(blockPos3.above(), Blocks.LANTERN.defaultBlockState(), 3);
				}

				return true;
			}
		}

		return false;
	}
}
