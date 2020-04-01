package net.minecraft.world.level.dimension.special;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.WallTorchBlock;
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

public class G35 extends NormalDimension {
	public G35(Level level, DimensionType dimensionType) {
		super(level, dimensionType);
	}

	@Override
	public ChunkGenerator<? extends ChunkGeneratorSettings> createRandomLevelGenerator() {
		return new G35.Generator(this.level, SpecialDimensionBase.normalBiomes(this.level.getSeed()), ChunkGeneratorType.SURFACE.createSettings());
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
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
			BlockState blockState = Blocks.BRICKS.defaultBlockState();
			BlockState blockState2 = Blocks.WALL_TORCH.defaultBlockState();
			boolean bl = Math.floorMod(i, 4) == 2;
			boolean bl2 = Math.floorMod(j, 4) == 2;

			for (int k = 0; k < 16; k++) {
				for (int l = 0; l < 16; l++) {
					if (l != 0 || k != 0 || !bl || !bl2) {
						worldGenRegion.setBlock(mutableBlockPos.set(16 * i + l, 255, 16 * j + k), blockState, 4);
					}
				}
			}

			if (i % 4 == 0) {
				for (int k = 0; k < 16; k++) {
					for (int lx = 0; lx < 256; lx++) {
						worldGenRegion.setBlock(mutableBlockPos.set(16 * i, lx, 16 * j + k), blockState, 4);
					}
				}

				if (bl2) {
					BlockPos blockPos = worldGenRegion.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, new BlockPos(16 * i, 0, 16 * j));
					BlockState blockState3 = Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.EAST);
					worldGenRegion.setBlock(blockPos, blockState3.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), 4);
					BlockPos blockPos2 = blockPos.above();
					worldGenRegion.setBlock(blockPos2, blockState3.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 4);
					BlockPos blockPos3 = blockPos2.above();
					worldGenRegion.setBlock(blockPos3.east(), blockState2.setValue(WallTorchBlock.FACING, Direction.EAST), 4);
					worldGenRegion.setBlock(blockPos3.west(), blockState2.setValue(WallTorchBlock.FACING, Direction.WEST), 4);
				}
			}

			if (j % 4 == 0) {
				for (int k = 0; k < 16; k++) {
					for (int lx = 0; lx < 256; lx++) {
						worldGenRegion.setBlock(mutableBlockPos.set(16 * i + k, lx, 16 * j), blockState, 4);
					}
				}

				if (bl) {
					BlockPos blockPos = worldGenRegion.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, new BlockPos(16 * i, 0, 16 * j));
					BlockState blockState3 = Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.SOUTH);
					worldGenRegion.setBlock(blockPos, blockState3.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), 4);
					BlockPos blockPos2 = blockPos.above();
					worldGenRegion.setBlock(blockPos2, blockState3.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 4);
					BlockPos blockPos3 = blockPos2.above();
					worldGenRegion.setBlock(blockPos3.north(), blockState2.setValue(WallTorchBlock.FACING, Direction.NORTH), 4);
					worldGenRegion.setBlock(blockPos3.south(), blockState2.setValue(WallTorchBlock.FACING, Direction.SOUTH), 4);
				}
			}
		}

		@Override
		public ChunkGeneratorType<?, ?> getType() {
			return ChunkGeneratorType.T30;
		}
	}
}
