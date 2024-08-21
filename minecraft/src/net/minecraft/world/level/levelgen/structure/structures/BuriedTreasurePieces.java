package net.minecraft.world.level.levelgen.structure.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class BuriedTreasurePieces {
	public static class BuriedTreasurePiece extends StructurePiece {
		public BuriedTreasurePiece(BlockPos blockPos) {
			super(StructurePieceType.BURIED_TREASURE_PIECE, 0, new BoundingBox(blockPos));
		}

		public BuriedTreasurePiece(CompoundTag compoundTag) {
			super(StructurePieceType.BURIED_TREASURE_PIECE, compoundTag);
		}

		@Override
		protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
		}

		@Override
		public void postProcess(
			WorldGenLevel worldGenLevel,
			StructureManager structureManager,
			ChunkGenerator chunkGenerator,
			RandomSource randomSource,
			BoundingBox boundingBox,
			ChunkPos chunkPos,
			BlockPos blockPos
		) {
			int i = worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, this.boundingBox.minX(), this.boundingBox.minZ());
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(this.boundingBox.minX(), i, this.boundingBox.minZ());

			while (mutableBlockPos.getY() > worldGenLevel.getMinY()) {
				BlockState blockState = worldGenLevel.getBlockState(mutableBlockPos);
				BlockState blockState2 = worldGenLevel.getBlockState(mutableBlockPos.below());
				if (blockState2 == Blocks.SANDSTONE.defaultBlockState()
					|| blockState2 == Blocks.STONE.defaultBlockState()
					|| blockState2 == Blocks.ANDESITE.defaultBlockState()
					|| blockState2 == Blocks.GRANITE.defaultBlockState()
					|| blockState2 == Blocks.DIORITE.defaultBlockState()) {
					BlockState blockState3 = !blockState.isAir() && !this.isLiquid(blockState) ? blockState : Blocks.SAND.defaultBlockState();

					for (Direction direction : Direction.values()) {
						BlockPos blockPos2 = mutableBlockPos.relative(direction);
						BlockState blockState4 = worldGenLevel.getBlockState(blockPos2);
						if (blockState4.isAir() || this.isLiquid(blockState4)) {
							BlockPos blockPos3 = blockPos2.below();
							BlockState blockState5 = worldGenLevel.getBlockState(blockPos3);
							if ((blockState5.isAir() || this.isLiquid(blockState5)) && direction != Direction.UP) {
								worldGenLevel.setBlock(blockPos2, blockState2, 3);
							} else {
								worldGenLevel.setBlock(blockPos2, blockState3, 3);
							}
						}
					}

					this.boundingBox = new BoundingBox(mutableBlockPos);
					this.createChest(worldGenLevel, boundingBox, randomSource, mutableBlockPos, BuiltInLootTables.BURIED_TREASURE, null);
					return;
				}

				mutableBlockPos.move(0, -1, 0);
			}
		}

		private boolean isLiquid(BlockState blockState) {
			return blockState == Blocks.WATER.defaultBlockState() || blockState == Blocks.LAVA.defaultBlockState();
		}
	}
}
