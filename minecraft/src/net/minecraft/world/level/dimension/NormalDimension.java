package net.minecraft.world.level.dimension;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class NormalDimension extends Dimension {
	public NormalDimension(Level level, DimensionType dimensionType) {
		super(level, dimensionType, 0.0F);
	}

	@Override
	public DimensionType getType() {
		return DimensionType.OVERWORLD;
	}

	@Override
	public ChunkGenerator<? extends ChunkGeneratorSettings> createRandomLevelGenerator() {
		return (ChunkGenerator<? extends ChunkGeneratorSettings>)this.level.getLevelData().getGeneratorProvider().create(this.level);
	}

	@Nullable
	@Override
	public BlockPos getSpawnPosInChunk(ChunkPos chunkPos, boolean bl) {
		for (int i = chunkPos.getMinBlockX(); i <= chunkPos.getMaxBlockX(); i++) {
			for (int j = chunkPos.getMinBlockZ(); j <= chunkPos.getMaxBlockZ(); j++) {
				BlockPos blockPos = this.getValidSpawnPosition(i, j, bl);
				if (blockPos != null) {
					return blockPos;
				}
			}
		}

		return null;
	}

	@Nullable
	@Override
	public BlockPos getValidSpawnPosition(int i, int j, boolean bl) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, 0, j);
		Biome biome = this.level.getBiome(mutableBlockPos);
		BlockState blockState = biome.getSurfaceBuilderConfig().getTopMaterial();
		if (bl && !blockState.getBlock().is(BlockTags.VALID_SPAWN)) {
			return null;
		} else {
			LevelChunk levelChunk = this.level.getChunk(i >> 4, j >> 4);
			int k = levelChunk.getHeight(Heightmap.Types.MOTION_BLOCKING, i & 15, j & 15);
			if (k < 0) {
				return null;
			} else if (levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, i & 15, j & 15) > levelChunk.getHeight(Heightmap.Types.OCEAN_FLOOR, i & 15, j & 15)) {
				return null;
			} else {
				for (int l = k + 1; l >= 0; l--) {
					mutableBlockPos.set(i, l, j);
					BlockState blockState2 = this.level.getBlockState(mutableBlockPos);
					if (!blockState2.getFluidState().isEmpty()) {
						break;
					}

					if (blockState2.equals(blockState)) {
						return mutableBlockPos.above().immutable();
					}
				}

				return null;
			}
		}
	}

	@Override
	public float getTimeOfDay(long l, float f) {
		double d = Mth.frac((double)l / 24000.0 - 0.25);
		double e = 0.5 - Math.cos(d * Math.PI) / 2.0;
		return (float)(d * 2.0 + e) / 3.0F;
	}

	@Override
	public boolean isNaturalDimension() {
		return true;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f) {
		return vec3.multiply((double)(f * 0.94F + 0.06F), (double)(f * 0.94F + 0.06F), (double)(f * 0.91F + 0.09F));
	}

	@Override
	public boolean mayRespawn() {
		return true;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean isFoggyAt(int i, int j) {
		return false;
	}
}
