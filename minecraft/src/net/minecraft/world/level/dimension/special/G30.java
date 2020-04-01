package net.minecraft.world.level.dimension.special;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.NormalDimension;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;
import net.minecraft.world.level.levelgen.OverworldLevelSource;

public class G30 extends NormalDimension {
	public G30(Level level, DimensionType dimensionType) {
		super(level, dimensionType);
	}

	@Override
	public ChunkGenerator<? extends ChunkGeneratorSettings> createRandomLevelGenerator() {
		return new G30.Generator(this.level, SpecialDimensionBase.normalBiomes(this.level.getSeed()), ChunkGeneratorType.SURFACE.createSettings());
	}

	public static class Generator extends OverworldLevelSource {
		public Generator(LevelAccessor levelAccessor, BiomeSource biomeSource, OverworldGeneratorSettings overworldGeneratorSettings) {
			super(levelAccessor, biomeSource, overworldGeneratorSettings);
		}

		@Override
		public void applyBiomeDecoration(WorldGenRegion worldGenRegion) {
			super.applyBiomeDecoration(worldGenRegion);
			BlockState blockState = Blocks.SLIME_BLOCK.defaultBlockState();
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int i = 0; i < 16; i++) {
				for (int j = 0; j < 16; j++) {
					int k = worldGenRegion.getCenterX() * 16 + i;
					int l = worldGenRegion.getCenterZ() * 16 + j;
					int m = worldGenRegion.getHeight(Heightmap.Types.MOTION_BLOCKING, k, l);

					for (int n = 0; n < 10; n++) {
						worldGenRegion.setBlock(mutableBlockPos.set(k, m + n, l), blockState, 4);
					}
				}
			}
		}

		@Override
		public ChunkGeneratorType<?, ?> getType() {
			return ChunkGeneratorType.T25;
		}
	}
}
