package net.minecraft.world.level.dimension.special;

import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.NormalDimension;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;
import net.minecraft.world.level.levelgen.OverworldLevelSource;

public class G25 extends NormalDimension {
	private static final Vector3f RED = new Vector3f(1.0F, 0.0F, 0.0F);
	private static final Vector3f BLUE = new Vector3f(0.0F, 0.0F, 1.0F);

	public G25(Level level, DimensionType dimensionType) {
		super(level, dimensionType);
	}

	@Override
	public ChunkGenerator<? extends ChunkGeneratorSettings> createRandomLevelGenerator() {
		return new G25.Generator(this.level, SpecialDimensionBase.normalBiomes(this.level.getSeed()), ChunkGeneratorType.SURFACE.createSettings());
	}

	@Environment(EnvType.CLIENT)
	private static Vector3f getHalfColor(BlockPos blockPos) {
		if (blockPos.getX() > 0) {
			return RED;
		} else {
			return blockPos.getX() < 0 ? BLUE : NO_CHANGE;
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public Vector3f getExtraTint(BlockState blockState, BlockPos blockPos) {
		return getHalfColor(blockPos);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public <T extends LivingEntity> Vector3f getEntityExtraTint(T livingEntity) {
		return getHalfColor(livingEntity.blockPosition());
	}

	public static class Generator extends OverworldLevelSource {
		public Generator(LevelAccessor levelAccessor, BiomeSource biomeSource, OverworldGeneratorSettings overworldGeneratorSettings) {
			super(levelAccessor, biomeSource, overworldGeneratorSettings);
		}

		@Override
		public void applyBiomeDecoration(WorldGenRegion worldGenRegion) {
			super.applyBiomeDecoration(worldGenRegion);
			int i = worldGenRegion.getCenterX();
			int j = worldGenRegion.getCenterZ();
			if (i == 0) {
				BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

				for (int k = 0; k < 16; k++) {
					for (int l = 0; l < 256; l++) {
						worldGenRegion.setBlock(mutableBlockPos.set(0, l, 16 * j + k), Blocks.BEDROCK.defaultBlockState(), 4);
					}
				}

				if (j == 0) {
					BlockPos blockPos = worldGenRegion.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, mutableBlockPos.set(0, 0, 0));
					BlockState blockState = Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.EAST);
					worldGenRegion.setBlock(blockPos, blockState.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), 4);
					worldGenRegion.setBlock(blockPos.above(), blockState.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 4);
				}
			}
		}

		@Override
		public ChunkGeneratorType<?, ?> getType() {
			return ChunkGeneratorType.T20;
		}
	}
}
